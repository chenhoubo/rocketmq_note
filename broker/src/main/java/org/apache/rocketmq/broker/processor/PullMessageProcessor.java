/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.broker.processor;

import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.client.ConsumerGroupInfo;
import org.apache.rocketmq.broker.filter.ConsumerFilterData;
import org.apache.rocketmq.broker.filter.ConsumerFilterManager;
import org.apache.rocketmq.broker.filter.ExpressionForRetryMessageFilter;
import org.apache.rocketmq.broker.filter.ExpressionMessageFilter;
import org.apache.rocketmq.broker.mqtrace.AbortProcessException;
import org.apache.rocketmq.broker.mqtrace.ConsumeMessageContext;
import org.apache.rocketmq.broker.mqtrace.ConsumeMessageHook;
import org.apache.rocketmq.broker.plugin.PullMessageResultHandler;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.constant.PermName;
import org.apache.rocketmq.common.filter.ExpressionType;
import org.apache.rocketmq.common.filter.FilterAPI;
import org.apache.rocketmq.common.help.FAQUrl;
import org.apache.rocketmq.common.protocol.ForbiddenType;
import org.apache.rocketmq.common.protocol.NamespaceUtil;
import org.apache.rocketmq.common.protocol.RequestCode;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.header.PullMessageRequestHeader;
import org.apache.rocketmq.common.protocol.header.PullMessageResponseHeader;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.common.rpc.RpcClientUtils;
import org.apache.rocketmq.common.rpc.RpcRequest;
import org.apache.rocketmq.common.rpc.RpcResponse;
import org.apache.rocketmq.common.statictopic.LogicQueueMappingItem;
import org.apache.rocketmq.common.statictopic.TopicQueueMappingContext;
import org.apache.rocketmq.common.statictopic.TopicQueueMappingDetail;
import org.apache.rocketmq.common.statictopic.TopicQueueMappingUtils;
import org.apache.rocketmq.common.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.common.sysflag.PullSysFlag;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.netty.NettyRequestProcessor;
import org.apache.rocketmq.remoting.netty.RequestTask;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.store.GetMessageResult;
import org.apache.rocketmq.store.GetMessageStatus;
import org.apache.rocketmq.store.MessageFilter;
import org.apache.rocketmq.store.config.BrokerRole;
import org.apache.rocketmq.store.stats.BrokerStatsManager;

import static org.apache.rocketmq.remoting.protocol.RemotingCommand.buildErrorResponse;

public class PullMessageProcessor implements NettyRequestProcessor {
    private static final InternalLogger LOGGER = InternalLoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);
    private List<ConsumeMessageHook> consumeMessageHookList;
    private PullMessageResultHandler pullMessageResultHandler;
    private final BrokerController brokerController;

    public PullMessageProcessor(final BrokerController brokerController) {
        this.brokerController = brokerController;
        this.pullMessageResultHandler = new DefaultPullMessageResultHandler(brokerController);
    }

    private RemotingCommand rewriteRequestForStaticTopic(PullMessageRequestHeader requestHeader,
        TopicQueueMappingContext mappingContext) {
        try {
            if (mappingContext.getMappingDetail() == null) {
                return null;
            }
            TopicQueueMappingDetail mappingDetail = mappingContext.getMappingDetail();
            String topic = mappingContext.getTopic();
            Integer globalId = mappingContext.getGlobalId();
            // if the leader? consider the order consumer, which will lock the mq
            if (!mappingContext.isLeader()) {
                return buildErrorResponse(ResponseCode.NOT_LEADER_FOR_QUEUE, String.format("%s-%d cannot find mapping item in request process of current broker %s", topic, globalId, mappingDetail.getBname()));
            }

            Long globalOffset = requestHeader.getQueueOffset();
            LogicQueueMappingItem mappingItem = TopicQueueMappingUtils.findLogicQueueMappingItem(mappingContext.getMappingItemList(), globalOffset, true);
            mappingContext.setCurrentItem(mappingItem);

            if (globalOffset < mappingItem.getLogicOffset()) {
                //handleOffsetMoved
                //If the physical queue is reused, we should handle the PULL_OFFSET_MOVED independently
                //Otherwise, we could just transfer it to the physical process
            }
            //below are physical info
            String bname = mappingItem.getBname();
            Integer phyQueueId = mappingItem.getQueueId();
            Long phyQueueOffset = mappingItem.computePhysicalQueueOffset(globalOffset);
            requestHeader.setQueueId(phyQueueId);
            requestHeader.setQueueOffset(phyQueueOffset);
            if (mappingItem.checkIfEndOffsetDecided()
                && requestHeader.getMaxMsgNums() != null) {
                requestHeader.setMaxMsgNums((int) Math.min(mappingItem.getEndOffset() - mappingItem.getStartOffset(), requestHeader.getMaxMsgNums()));
            }

            if (mappingDetail.getBname().equals(bname)) {
                //just let it go, do the local pull process
                return null;
            }

            int sysFlag = requestHeader.getSysFlag();
            requestHeader.setLo(false);
            requestHeader.setBname(bname);
            sysFlag = PullSysFlag.clearSuspendFlag(sysFlag);
            sysFlag = PullSysFlag.clearCommitOffsetFlag(sysFlag);
            requestHeader.setSysFlag(sysFlag);
            RpcRequest rpcRequest = new RpcRequest(RequestCode.PULL_MESSAGE, requestHeader, null);
            RpcResponse rpcResponse = this.brokerController.getBrokerOuterAPI().getRpcClient().invoke(rpcRequest, this.brokerController.getBrokerConfig().getForwardTimeout()).get();
            if (rpcResponse.getException() != null) {
                throw rpcResponse.getException();
            }

            PullMessageResponseHeader responseHeader = (PullMessageResponseHeader) rpcResponse.getHeader();
            {
                RemotingCommand rewriteResult = rewriteResponseForStaticTopic(requestHeader, responseHeader, mappingContext, rpcResponse.getCode());
                if (rewriteResult != null) {
                    return rewriteResult;
                }
            }
            return RpcClientUtils.createCommandForRpcResponse(rpcResponse);
        } catch (Throwable t) {
            return buildErrorResponse(ResponseCode.SYSTEM_ERROR, t.getMessage());
        }
    }

    private RemotingCommand rewriteResponseForStaticTopic(PullMessageRequestHeader requestHeader,
        PullMessageResponseHeader responseHeader,
        TopicQueueMappingContext mappingContext, final int code) {
        try {
            if (mappingContext.getMappingDetail() == null) {
                return null;
            }
            TopicQueueMappingDetail mappingDetail = mappingContext.getMappingDetail();
            LogicQueueMappingItem leaderItem = mappingContext.getLeaderItem();

            LogicQueueMappingItem currentItem = mappingContext.getCurrentItem();

            LogicQueueMappingItem earlistItem = TopicQueueMappingUtils.findLogicQueueMappingItem(mappingContext.getMappingItemList(), 0L, true);

            assert currentItem.getLogicOffset() >= 0;

            long requestOffset = requestHeader.getQueueOffset();
            long nextBeginOffset = responseHeader.getNextBeginOffset();
            long minOffset = responseHeader.getMinOffset();
            long maxOffset = responseHeader.getMaxOffset();
            int responseCode = code;

            //consider the following situations
            // 1. read from slave, currently not supported
            // 2. the middle queue is truncated because of deleting commitlog
            if (code != ResponseCode.SUCCESS) {
                //note the currentItem maybe both the leader and  the earliest
                boolean isRevised = false;
                if (leaderItem.getGen() == currentItem.getGen()) {
                    //read the leader
                    if (requestOffset > maxOffset) {
                        //actually, we need do nothing, but keep the code structure here
                        if (code == ResponseCode.PULL_OFFSET_MOVED) {
                            responseCode = ResponseCode.PULL_OFFSET_MOVED;
                            nextBeginOffset = maxOffset;
                        } else {
                            //maybe current broker is the slave
                            responseCode = code;
                        }
                    } else if (requestOffset < minOffset) {
                        nextBeginOffset = minOffset;
                        responseCode = ResponseCode.PULL_RETRY_IMMEDIATELY;
                    } else {
                        responseCode = code;
                    }
                }
                //note the currentItem maybe both the leader and  the earliest
                if (earlistItem.getGen() == currentItem.getGen()) {
                    //read the earliest one
                    if (requestOffset < minOffset) {
                        if (code == ResponseCode.PULL_OFFSET_MOVED) {
                            responseCode = ResponseCode.PULL_OFFSET_MOVED;
                            nextBeginOffset = minOffset;
                        } else {
                            //maybe read from slave, but we still set it to moved
                            responseCode = ResponseCode.PULL_OFFSET_MOVED;
                            nextBeginOffset = minOffset;
                        }
                    } else if (requestOffset >= maxOffset) {
                        //just move to another item
                        LogicQueueMappingItem nextItem = TopicQueueMappingUtils.findNext(mappingContext.getMappingItemList(), currentItem, true);
                        if (nextItem != null) {
                            isRevised = true;
                            currentItem = nextItem;
                            nextBeginOffset = currentItem.getStartOffset();
                            minOffset = currentItem.getStartOffset();
                            maxOffset = minOffset;
                            responseCode = ResponseCode.PULL_RETRY_IMMEDIATELY;
                        } else {
                            //maybe the next one's logic offset is -1
                            responseCode = ResponseCode.PULL_NOT_FOUND;
                        }
                    } else {
                        //let it go
                        responseCode = code;
                    }
                }

                //read from the middle item, ignore the PULL_OFFSET_MOVED
                if (!isRevised
                    && leaderItem.getGen() != currentItem.getGen()
                    && earlistItem.getGen() != currentItem.getGen()) {
                    if (requestOffset < minOffset) {
                        nextBeginOffset = minOffset;
                        responseCode = ResponseCode.PULL_RETRY_IMMEDIATELY;
                    } else if (requestOffset >= maxOffset) {
                        //just move to another item
                        LogicQueueMappingItem nextItem = TopicQueueMappingUtils.findNext(mappingContext.getMappingItemList(), currentItem, true);
                        if (nextItem != null) {
                            currentItem = nextItem;
                            nextBeginOffset = currentItem.getStartOffset();
                            minOffset = currentItem.getStartOffset();
                            maxOffset = minOffset;
                            responseCode = ResponseCode.PULL_RETRY_IMMEDIATELY;
                        } else {
                            //maybe the next one's logic offset is -1
                            responseCode = ResponseCode.PULL_NOT_FOUND;
                        }
                    } else {
                        responseCode = code;
                    }
                }
            }

            //handle nextBeginOffset
            //the next begin offset should no more than the end offset
            if (currentItem.checkIfEndOffsetDecided()
                && nextBeginOffset >= currentItem.getEndOffset()) {
                nextBeginOffset = currentItem.getEndOffset();
            }
            responseHeader.setNextBeginOffset(currentItem.computeStaticQueueOffsetStrictly(nextBeginOffset));
            //handle min offset
            responseHeader.setMinOffset(currentItem.computeStaticQueueOffsetStrictly(Math.max(currentItem.getStartOffset(), minOffset)));
            //handle max offset
            responseHeader.setMaxOffset(Math.max(currentItem.computeStaticQueueOffsetStrictly(maxOffset),
                TopicQueueMappingDetail.computeMaxOffsetFromMapping(mappingDetail, mappingContext.getGlobalId())));
            //set the offsetDelta
            responseHeader.setOffsetDelta(currentItem.computeOffsetDelta());

            if (code != ResponseCode.SUCCESS) {
                return RemotingCommand.createResponseCommandWithHeader(responseCode, responseHeader);
            } else {
                return null;
            }
        } catch (Throwable t) {
            return buildErrorResponse(ResponseCode.SYSTEM_ERROR, t.getMessage());
        }
    }

    @Override
    public RemotingCommand processRequest(final ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        return this.processRequest(ctx.channel(), request, true);
    }

    @Override
    public boolean rejectRequest() {
        if (!this.brokerController.getBrokerConfig().isSlaveReadEnable()
            && this.brokerController.getMessageStoreConfig().getBrokerRole() == BrokerRole.SLAVE) {
            return true;
        }
        return false;
    }

    /**
     * @param channel 服务器与客户端netty通道
     * @param request 客户端请求（RemotingCommand）
     * @param brokerAllowSuspend 是否允许服务器端长轮询，
     */
    private RemotingCommand processRequest(final Channel channel, RemotingCommand request, boolean brokerAllowSuspend)
        throws RemotingCommandException {
        // 创建服务器端对本请求的“响应对象”，注意：header 类型 PullMessageResponseHeader
        RemotingCommand response = RemotingCommand.createResponseCommand(PullMessageResponseHeader.class);

        // 获取response对象的 header
        final PullMessageResponseHeader responseHeader = (PullMessageResponseHeader) response.readCustomHeader();

        // 从request请求中，解析出“requestHeader”对象，类型是：PullMessageRequestHeader
        final PullMessageRequestHeader requestHeader =
            (PullMessageRequestHeader) request.decodeCommandCustomHeader(PullMessageRequestHeader.class);


        // 设置响应对象的opaque 为 request请求的 opaque，为什么要设置？
        // 客户端需要根据 response  opaque 找到 ResponseFuture 然后完成后面的事情。
        response.setOpaque(request.getOpaque());

        LOGGER.debug("receive PullMessage request command, {}", request);


        //接下来的代码就是各种校验
        /**
         * 校验服务器端是否可读
         */
        if (!PermName.isReadable(this.brokerController.getBrokerConfig().getBrokerPermission())) {
            response.setCode(ResponseCode.NO_PERMISSION);
            response.setRemark(String.format("the broker[%s] pulling message is forbidden", this.brokerController.getBrokerConfig().getBrokerIP1()));
            return response;
        }

        if (request.getCode() == RequestCode.LITE_PULL_MESSAGE && !this.brokerController.getBrokerConfig().isLitePullMessageEnable()) {
            response.setCode(ResponseCode.NO_PERMISSION);
            response.setRemark(
                "the broker[" + this.brokerController.getBrokerConfig().getBrokerIP1() + "] for lite pull consumer is forbidden");
            return response;
        }

        /**
         * 获取消费者组配置信息，如果为 null，则也是直接返回
         */
        SubscriptionGroupConfig subscriptionGroupConfig =
            this.brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(requestHeader.getConsumerGroup());
        if (null == subscriptionGroupConfig) {
            response.setCode(ResponseCode.SUBSCRIPTION_GROUP_NOT_EXIST);
            response.setRemark(String.format("subscription group [%s] does not exist, %s", requestHeader.getConsumerGroup(), FAQUrl.suggestTodo(FAQUrl.SUBSCRIPTION_GROUP_NOT_EXIST)));
            return response;
        }

        if (!subscriptionGroupConfig.isConsumeEnable()) {
            response.setCode(ResponseCode.NO_PERMISSION);
            responseHeader.setForbiddenType(ForbiddenType.GROUP_FORBIDDEN);
            response.setRemark("subscription group no permission, " + requestHeader.getConsumerGroup());
            return response;
        }

        final boolean hasCommitOffsetFlag = PullSysFlag.hasCommitOffsetFlag(requestHeader.getSysFlag());
        final boolean hasSubscriptionFlag = PullSysFlag.hasSubscriptionFlag(requestHeader.getSysFlag());

        //每个主题都会创建TopicConfig对象
        TopicConfig topicConfig = this.brokerController.getTopicConfigManager().selectTopicConfig(requestHeader.getTopic());
        if (null == topicConfig) {
            LOGGER.error("the topic {} not exist, consumer: {}", requestHeader.getTopic(), RemotingHelper.parseChannelRemoteAddr(channel));
            response.setCode(ResponseCode.TOPIC_NOT_EXIST);
            response.setRemark(String.format("topic[%s] not exist, apply first please! %s", requestHeader.getTopic(), FAQUrl.suggestTodo(FAQUrl.APPLY_TOPIC_URL)));
            return response;
        }

        //topic权限是否可读
        if (!PermName.isReadable(topicConfig.getPerm())) {
            response.setCode(ResponseCode.NO_PERMISSION);
            responseHeader.setForbiddenType(ForbiddenType.TOPIC_FORBIDDEN);
            response.setRemark("the topic[" + requestHeader.getTopic() + "] pulling message is forbidden");
            return response;
        }

        TopicQueueMappingContext mappingContext = this.brokerController.getTopicQueueMappingManager().buildTopicQueueMappingContext(requestHeader, false);

        {
            RemotingCommand rewriteResult = rewriteRequestForStaticTopic(requestHeader, mappingContext);
            if (rewriteResult != null) {
                return rewriteResult;
            }
        }

        if (requestHeader.getQueueId() < 0 || requestHeader.getQueueId() >= topicConfig.getReadQueueNums()) {
            String errorInfo = String.format("queueId[%d] is illegal, topic:[%s] topicConfig.readQueueNums:[%d] consumer:[%s]",
                requestHeader.getQueueId(), requestHeader.getTopic(), topicConfig.getReadQueueNums(), channel.remoteAddress());
            LOGGER.warn(errorInfo);
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setRemark(errorInfo);
            return response;
        }

        SubscriptionData subscriptionData = null;
        ConsumerFilterData consumerFilterData = null;
        if (hasSubscriptionFlag) {
            try {
                subscriptionData = FilterAPI.build(
                    requestHeader.getTopic(), requestHeader.getSubscription(), requestHeader.getExpressionType()
                );
                if (!ExpressionType.isTagType(subscriptionData.getExpressionType())) {
                    consumerFilterData = ConsumerFilterManager.build(
                        requestHeader.getTopic(), requestHeader.getConsumerGroup(), requestHeader.getSubscription(),
                        requestHeader.getExpressionType(), requestHeader.getSubVersion()
                    );
                    assert consumerFilterData != null;
                }
            } catch (Exception e) {
                LOGGER.warn("Parse the consumer's subscription[{}] failed, group: {}", requestHeader.getSubscription(),
                    requestHeader.getConsumerGroup());
                response.setCode(ResponseCode.SUBSCRIPTION_PARSE_FAILED);
                response.setRemark("parse the consumer's subscription failed");
                return response;
            }
        } else {
            //消费者组信息是否为空
            ConsumerGroupInfo consumerGroupInfo =
                this.brokerController.getConsumerManager().getConsumerGroupInfo(requestHeader.getConsumerGroup());
            if (null == consumerGroupInfo) {
                LOGGER.warn("the consumer's group info not exist, group: {}", requestHeader.getConsumerGroup());
                response.setCode(ResponseCode.SUBSCRIPTION_NOT_EXIST);
                response.setRemark("the consumer's group info not exist" + FAQUrl.suggestTodo(FAQUrl.SAME_GROUP_DIFFERENT_TOPIC));
                return response;
            }

            if (!subscriptionGroupConfig.isConsumeBroadcastEnable()
                && consumerGroupInfo.getMessageModel() == MessageModel.BROADCASTING) {
                response.setCode(ResponseCode.NO_PERMISSION);
                responseHeader.setForbiddenType(ForbiddenType.BROADCASTING_DISABLE_FORBIDDEN);
                response.setRemark("the consumer group[" + requestHeader.getConsumerGroup() + "] can not consume by broadcast way");
                return response;
            }

            boolean readForbidden = this.brokerController.getSubscriptionGroupManager().getForbidden(//
                subscriptionGroupConfig.getGroupName(), requestHeader.getTopic(), PermName.INDEX_PERM_READ);
            if (readForbidden) {
                response.setCode(ResponseCode.NO_PERMISSION);
                responseHeader.setForbiddenType(ForbiddenType.SUBSCRIPTION_FORBIDDEN);
                response.setRemark("the consumer group[" + requestHeader.getConsumerGroup() + "] is forbidden for topic[" + requestHeader.getTopic() + "]");
                return response;
            }

            //请求主题的订阅数据是否为空
            subscriptionData = consumerGroupInfo.findSubscriptionData(requestHeader.getTopic());
            if (null == subscriptionData) {
                LOGGER.warn("the consumer's subscription not exist, group: {}, topic:{}", requestHeader.getConsumerGroup(), requestHeader.getTopic());
                response.setCode(ResponseCode.SUBSCRIPTION_NOT_EXIST);
                response.setRemark("the consumer's subscription not exist" + FAQUrl.suggestTodo(FAQUrl.SAME_GROUP_DIFFERENT_TOPIC));
                return response;
            }

            if (subscriptionData.getSubVersion() < requestHeader.getSubVersion()) {
                LOGGER.warn("The broker's subscription is not latest, group: {} {}", requestHeader.getConsumerGroup(),
                    subscriptionData.getSubString());
                response.setCode(ResponseCode.SUBSCRIPTION_NOT_LATEST);
                response.setRemark("the consumer's subscription not latest");
                return response;
            }
            if (!ExpressionType.isTagType(subscriptionData.getExpressionType())) {
                consumerFilterData = this.brokerController.getConsumerFilterManager().get(requestHeader.getTopic(),
                    requestHeader.getConsumerGroup());
                if (consumerFilterData == null) {
                    response.setCode(ResponseCode.FILTER_DATA_NOT_EXIST);
                    response.setRemark("The broker's consumer filter data is not exist!Your expression may be wrong!");
                    return response;
                }
                if (consumerFilterData.getClientVersion() < requestHeader.getSubVersion()) {
                    LOGGER.warn("The broker's consumer filter data is not latest, group: {}, topic: {}, serverV: {}, clientV: {}",
                        requestHeader.getConsumerGroup(), requestHeader.getTopic(), consumerFilterData.getClientVersion(), requestHeader.getSubVersion());
                    response.setCode(ResponseCode.FILTER_DATA_NOT_LATEST);
                    response.setRemark("the consumer's consumer filter data not latest");
                    return response;
                }
            }
        }

        if (!ExpressionType.isTagType(subscriptionData.getExpressionType())
            && !this.brokerController.getBrokerConfig().isEnablePropertyFilter()) {
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setRemark("The broker does not support consumer to filter message by " + subscriptionData.getExpressionType());
            return response;
        }

        // 消息过滤器 下面为创建MessageFilter逻辑，校验逻辑在这里已经结束了
        MessageFilter messageFilter;
        if (this.brokerController.getBrokerConfig().isFilterSupportRetry()) {
            messageFilter = new ExpressionForRetryMessageFilter(subscriptionData, consumerFilterData,
                this.brokerController.getConsumerFilterManager());
        } else {
            messageFilter = new ExpressionMessageFilter(subscriptionData, consumerFilterData,
                this.brokerController.getConsumerFilterManager());
        }

        // 查询消息的核心入口
        // 参数1：consumerGroup 消费者组
        // 参数2：topic 主题
        // 参数3：queueId
        // 参数4：queueOffset
        // 参数5：maxMsgNums 本次查询最多可拿的消息数量
        // 参数6：messageFilter 上一步创建的消息过滤器（服务器这里，一般都是tagCode过滤..，getMessage方法通过consumerQueue拿到ConsumerQueueData，
        //      该对象有第三个字段tagCode，和messageFilter做个匹配，如果匹配到则去commitLog里拿到这条数据，如果匹配返回false，直接跳过这条消息，不再去
        //      commitL里查询了）
        final GetMessageResult getMessageResult =
            this.brokerController.getMessageStore().getMessage(requestHeader.getConsumerGroup(), requestHeader.getTopic(),
                requestHeader.getQueueId(), requestHeader.getQueueOffset(), requestHeader.getMaxMsgNums(), messageFilter);
        if (getMessageResult != null) {
            response.setRemark(getMessageResult.getStatus().name());
            // 设置responseHeader.nextBeginOffset 为 服务器计算出来的 该队列 下一次pull时使用的offset,假如拉消息之前的offset是10，
            //      拉取了5个之后的offset就是15
            responseHeader.setNextBeginOffset(getMessageResult.getNextBeginOffset());
            // 设置responseHeader.minOffset 为 pull queue的最小offset
            responseHeader.setMinOffset(getMessageResult.getMinOffset());
            // this does not need to be modified since it's not an accurate value under logical queue.
            // 设置responseHeader.maxOffset 为 pull queue的最大offset
            responseHeader.setMaxOffset(getMessageResult.getMaxOffset());
            responseHeader.setTopicSysFlag(topicConfig.getTopicSysFlag());
            responseHeader.setGroupSysFlag(subscriptionGroupConfig.getGroupSysFlag());

            // 根据getMessageResult的状态 设置 response的code
            switch (getMessageResult.getStatus()) {
                case FOUND:
                    response.setCode(ResponseCode.SUCCESS);
                    break;
                case MESSAGE_WAS_REMOVING:
                    response.setCode(ResponseCode.PULL_RETRY_IMMEDIATELY);
                    break;
                case NO_MATCHED_LOGIC_QUEUE:
                case NO_MESSAGE_IN_QUEUE:
                    if (0 != requestHeader.getQueueOffset()) {
                        response.setCode(ResponseCode.PULL_OFFSET_MOVED);
                        // XXX: warn and notify me
                        LOGGER.info("the broker stores no queue data, fix the request offset {} to {}, Topic: {} QueueId: {} Consumer Group: {}",
                            requestHeader.getQueueOffset(),
                            getMessageResult.getNextBeginOffset(),
                            requestHeader.getTopic(),
                            requestHeader.getQueueId(),
                            requestHeader.getConsumerGroup()
                        );
                    } else {
                        response.setCode(ResponseCode.PULL_NOT_FOUND);
                    }
                    break;
                case NO_MATCHED_MESSAGE:
                    response.setCode(ResponseCode.PULL_RETRY_IMMEDIATELY);
                    break;
                case OFFSET_FOUND_NULL:
                    response.setCode(ResponseCode.PULL_NOT_FOUND);
                    break;
                case OFFSET_OVERFLOW_BADLY:
                    response.setCode(ResponseCode.PULL_OFFSET_MOVED);
                    // XXX: warn and notify me
                    LOGGER.info("the request offset: {} over flow badly, fix to {}, broker max offset: {}, consumer: {}",
                        requestHeader.getQueueOffset(), getMessageResult.getNextBeginOffset(), getMessageResult.getMaxOffset(), channel.remoteAddress());
                    break;
                case OFFSET_OVERFLOW_ONE:
                    response.setCode(ResponseCode.PULL_NOT_FOUND);
                    break;
                case OFFSET_TOO_SMALL:
                    response.setCode(ResponseCode.PULL_OFFSET_MOVED);
                    LOGGER.info("the request offset too small. group={}, topic={}, requestOffset={}, brokerMinOffset={}, clientIp={}",
                        requestHeader.getConsumerGroup(), requestHeader.getTopic(), requestHeader.getQueueOffset(),
                        getMessageResult.getMinOffset(), channel.remoteAddress());
                    break;
                default:
                    assert false;
                    break;
            }

            // 设置客户端下次拉该queue时推荐使用的 brokerId
            if (this.brokerController.getBrokerConfig().isSlaveReadEnable() && !this.brokerController.getBrokerConfig().isInBrokerContainer()) {
                // consume too slow ,redirect to another machine
                if (getMessageResult.isSuggestPullingFromSlave()) {
                    responseHeader.setSuggestWhichBrokerId(subscriptionGroupConfig.getWhichBrokerWhenConsumeSlowly());
                }
                // consume ok
                else {
                    responseHeader.setSuggestWhichBrokerId(subscriptionGroupConfig.getBrokerId());
                }
            } else {
                responseHeader.setSuggestWhichBrokerId(MixAll.MASTER_ID);
            }

            if (this.brokerController.getBrokerConfig().getBrokerId() != MixAll.MASTER_ID && !getMessageResult.isSuggestPullingFromSlave()) {
                if (this.brokerController.getMinBrokerIdInGroup() == MixAll.MASTER_ID) {
                    LOGGER.debug("slave redirect pullRequest to master, topic: {}, queueId: {}, consumer group: {}, next: {}, min: {}, max: {}",
                        requestHeader.getTopic(),
                        requestHeader.getQueueId(),
                        requestHeader.getConsumerGroup(),
                        responseHeader.getNextBeginOffset(),
                        responseHeader.getMinOffset(),
                        responseHeader.getMaxOffset()
                    );
                    responseHeader.setSuggestWhichBrokerId(MixAll.MASTER_ID);
                    if (!getMessageResult.getStatus().equals(GetMessageStatus.FOUND)) {
                        response.setCode(ResponseCode.PULL_RETRY_IMMEDIATELY);
                    }
                }
            }

            // 经检查发现 服务器端并没有注册 消费hook，这里这个hook是服务器开发人员 留给自己的扩展接口
            if (this.hasConsumeMessageHook()) {
                String owner = request.getExtFields().get(BrokerStatsManager.COMMERCIAL_OWNER);
                String authType = request.getExtFields().get(BrokerStatsManager.ACCOUNT_AUTH_TYPE);
                String ownerParent = request.getExtFields().get(BrokerStatsManager.ACCOUNT_OWNER_PARENT);
                String ownerSelf = request.getExtFields().get(BrokerStatsManager.ACCOUNT_OWNER_SELF);

                ConsumeMessageContext context = new ConsumeMessageContext();
                context.setConsumerGroup(requestHeader.getConsumerGroup());
                context.setTopic(requestHeader.getTopic());
                context.setQueueId(requestHeader.getQueueId());
                context.setAccountAuthType(authType);
                context.setAccountOwnerParent(ownerParent);
                context.setAccountOwnerSelf(ownerSelf);
                context.setNamespace(NamespaceUtil.getNamespaceFromResource(requestHeader.getTopic()));

                switch (response.getCode()) {
                    case ResponseCode.SUCCESS:// 查询成功
                        int commercialBaseCount = brokerController.getBrokerConfig().getCommercialBaseCount();
                        int incValue = getMessageResult.getMsgCount4Commercial() * commercialBaseCount;

                        context.setCommercialRcvStats(BrokerStatsManager.StatsType.RCV_SUCCESS);
                        context.setCommercialRcvTimes(incValue);
                        context.setCommercialRcvSize(getMessageResult.getBufferTotalSize());
                        context.setCommercialOwner(owner);

                        context.setRcvStat(BrokerStatsManager.StatsType.RCV_SUCCESS);
                        context.setRcvMsgNum(getMessageResult.getMessageCount());
                        context.setRcvMsgSize(getMessageResult.getBufferTotalSize());
                        context.setCommercialRcvMsgNum(getMessageResult.getMsgCount4Commercial());

                        break;
                    case ResponseCode.PULL_NOT_FOUND:
                        if (!brokerAllowSuspend) {

                            context.setCommercialRcvStats(BrokerStatsManager.StatsType.RCV_EPOLLS);
                            context.setCommercialRcvTimes(1);
                            context.setCommercialOwner(owner);

                            context.setRcvStat(BrokerStatsManager.StatsType.RCV_EPOLLS);
                            context.setRcvMsgNum(0);
                            context.setRcvMsgSize(0);
                            context.setCommercialRcvMsgNum(0);
                        }
                        break;
                    case ResponseCode.PULL_RETRY_IMMEDIATELY:
                    case ResponseCode.PULL_OFFSET_MOVED:
                        context.setCommercialRcvStats(BrokerStatsManager.StatsType.RCV_EPOLLS);
                        context.setCommercialRcvTimes(1);
                        context.setCommercialOwner(owner);

                        context.setRcvStat(BrokerStatsManager.StatsType.RCV_EPOLLS);
                        context.setRcvMsgNum(0);
                        context.setRcvMsgSize(0);
                        context.setCommercialRcvMsgNum(0);
                        break;
                    default:
                        assert false;
                        break;
                }

                try {
                    this.executeConsumeMessageHookBefore(context);
                } catch (AbortProcessException e) {
                    response.setCode(e.getResponseCode());
                    response.setRemark(e.getErrorMessage());
                    return response;
                }
            }

            //rewrite the response for the
            RemotingCommand rewriteResult = rewriteResponseForStaticTopic(requestHeader, responseHeader, mappingContext, response.getCode());
            if (rewriteResult != null) {
                response = rewriteResult;
            }

            response = this.pullMessageResultHandler.handle(
                getMessageResult,
                request,
                requestHeader,
                channel,
                subscriptionData,
                subscriptionGroupConfig,
                brokerAllowSuspend,
                messageFilter,
                response
            );
        } else {
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setRemark("store getMessage return null");
        }

        // 1. brokerAllowSuspend == true
        // 2. sysFlag 表示提交消费者本地该queue的offset
        // 2个条件全部成立时，才在broker端存储该消费者组内该queue的消费进度
        boolean storeOffsetEnable = brokerAllowSuspend;
        storeOffsetEnable = storeOffsetEnable && hasCommitOffsetFlag;
        if (storeOffsetEnable) {
            // 存储该消费者组下该主题的指定队列的消费进度
            this.brokerController.getConsumerOffsetManager().commitOffset(RemotingHelper.parseChannelRemoteAddr(channel),
                requestHeader.getConsumerGroup(), requestHeader.getTopic(), requestHeader.getQueueId(), requestHeader.getCommitOffset());
        }
        // 返回response，当response不为null时，外层requestTask的callback 会将数据写给客户端
        return response;
    }

    public boolean hasConsumeMessageHook() {
        return consumeMessageHookList != null && !this.consumeMessageHookList.isEmpty();
    }

    public void executeConsumeMessageHookBefore(final ConsumeMessageContext context) {
        if (hasConsumeMessageHook()) {
            for (ConsumeMessageHook hook : this.consumeMessageHookList) {
                try {
                    hook.consumeMessageBefore(context);
                } catch (Throwable e) {
                }
            }
        }
    }

    public void executeRequestWhenWakeup(final Channel channel,
        final RemotingCommand request) throws RemotingCommandException {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    // 执行 processRequest 方法，注意：第三个参数 这里是 false了，不会再次触发 长轮询了。
                    final RemotingCommand response = PullMessageProcessor.this.processRequest(channel, request, false);

                    if (response != null) {
                        response.setOpaque(request.getOpaque());
                        response.markResponseType();
                        try {
                            // 将结果数据发送给客户端
                            channel.writeAndFlush(response).addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    if (!future.isSuccess()) {
                                        LOGGER.error("processRequestWrapper response to {} failed",
                                            future.channel().remoteAddress(), future.cause());
                                        LOGGER.error(request.toString());
                                        LOGGER.error(response.toString());
                                    }
                                }
                            });
                        } catch (Throwable e) {
                            LOGGER.error("processRequestWrapper process request over, but response failed", e);
                            LOGGER.error(request.toString());
                            LOGGER.error(response.toString());
                        }
                    }
                } catch (RemotingCommandException e1) {
                    LOGGER.error("excuteRequestWhenWakeup run", e1);
                }
            }
        };
        this.brokerController.getPullMessageExecutor().submit(new RequestTask(run, channel, request));
    }

    public void registerConsumeMessageHook(List<ConsumeMessageHook> consumeMessageHookList) {
        this.consumeMessageHookList = consumeMessageHookList;
    }

    public void setPullMessageResultHandler(PullMessageResultHandler pullMessageResultHandler) {
        this.pullMessageResultHandler = pullMessageResultHandler;
    }
}
