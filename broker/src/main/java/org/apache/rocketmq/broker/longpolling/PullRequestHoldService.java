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
package org.apache.rocketmq.broker.longpolling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.SystemClock;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;
import org.apache.rocketmq.store.ConsumeQueueExt;

//长轮询服务
public class PullRequestHoldService extends ServiceThread {
    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);
    protected static final String TOPIC_QUEUEID_SEPARATOR = "@";
    protected final BrokerController brokerController;
    private final SystemClock systemClock = new SystemClock();
    protected ConcurrentMap<String/* topic@queueId */, ManyPullRequest> pullRequestTable =
        new ConcurrentHashMap<String, ManyPullRequest>(1024);

    public PullRequestHoldService(final BrokerController brokerController) {
        this.brokerController = brokerController;
    }

    public void suspendPullRequest(final String topic, final int queueId, final PullRequest pullRequest) {
        String key = this.buildKey(topic, queueId);
        ManyPullRequest mpr = this.pullRequestTable.get(key);
        if (null == mpr) {
            mpr = new ManyPullRequest();
            ManyPullRequest prev = this.pullRequestTable.putIfAbsent(key, mpr);
            if (prev != null) {
                mpr = prev;
            }
        }

        mpr.addPullRequest(pullRequest);
    }

    private String buildKey(final String topic, final int queueId) {
        StringBuilder sb = new StringBuilder(topic.length() + 5);
        sb.append(topic);
        sb.append(TOPIC_QUEUEID_SEPARATOR);
        sb.append(queueId);
        return sb.toString();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());
        while (!this.isStopped()) {
            try {
                if (this.brokerController.getBrokerConfig().isLongPollingEnable()) {
                    // 服务器开启长轮询开关：每次循环休眠5秒
                    this.waitForRunning(5 * 1000);
                } else {
                    // 服务器关闭长轮询开关：每次循环休眠1秒
                    this.waitForRunning(this.brokerController.getBrokerConfig().getShortPollingTimeMills());
                }

                long beginLockTimestamp = this.systemClock.now();
                this.checkHoldRequest();//核心逻辑
                long costTime = this.systemClock.now() - beginLockTimestamp;
                if (costTime > 5 * 1000) {
                    log.warn("PullRequestHoldService: check hold pull request cost {}ms", costTime);
                }
            } catch (Throwable e) {
                log.warn(this.getServiceName() + " service has exception. ", e);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    @Override
    public String getServiceName() {
        if (brokerController != null && brokerController.getBrokerConfig().isInBrokerContainer()) {
            return this.brokerController.getBrokerIdentity().getLoggerIdentifier() + PullRequestHoldService.class.getSimpleName();
        }
        return PullRequestHoldService.class.getSimpleName();
    }

    protected void checkHoldRequest() {
        for (String key : this.pullRequestTable.keySet()) {
            // 循环体内为每个 topic@queueId k-v 的处理逻辑

            // key 按照@拆分，得到 topic 和 queueId
            String[] kArray = key.split(TOPIC_QUEUEID_SEPARATOR);

            if (2 == kArray.length) {
                // 主题
                String topic = kArray[0];
                // queueId
                int queueId = Integer.parseInt(kArray[1]);
                //到存储模块查询该ConsumeQueue的最大offset 如何获取？获取ConsumeQueue中MappedFile最后一个的文件名 + 文件正在顺序写的数据位点 =》 maxOffset
                final long offset = this.brokerController.getMessageStore().getMaxOffsetInQueue(topic, queueId);
                try {
                    // 通知消息达到的逻辑
                    // 参数1：主题
                    // 参数2：queueId
                    // 参数3：offset，当前queue最大offset
                    this.notifyMessageArriving(topic, queueId, offset);
                } catch (Throwable e) {
                    log.error(
                        "PullRequestHoldService: failed to check hold request failed, topic={}, queueId={}", topic,
                        queueId, e);
                }
            }
        }
    }

    // 通知消息达到的逻辑
    // 参数1：主题
    // 参数2：queueId
    // 参数3：offset，当前queue最大offset
    public void notifyMessageArriving(final String topic, final int queueId, final long maxOffset) {
        notifyMessageArriving(topic, queueId, maxOffset, null, 0, null, null);
    }

    // 该方法有两个调用点：
    // 1. pullRequestHoldService.run()..
    // 2. ReputMessageService 异步构建 ConsumeQueue 和 index 的消息转发服务
    public void notifyMessageArriving(final String topic, final int queueId, final long maxOffset, final Long tagsCode,
        long msgStoreTime, byte[] filterBitMap, Map<String, String> properties) {
        // 构建key，规则：主题@queueId
        String key = this.buildKey(topic, queueId);

        // 获取“主题@queueId”的manyPullRequest对象
        ManyPullRequest mpr = this.pullRequestTable.get(key);
        if (mpr != null) {
            // 获取该queue下的 pullRequest list 数据
            List<PullRequest> requestList = mpr.cloneListAndClear();
            if (requestList != null) {

                // 重放列表，当某个 pullRequest 即不超时，
                // 对应的queue的maxOffset <= pullRequest.offset 的话，就将该pullRequest 再放入replayList
                List<PullRequest> replayList = new ArrayList<PullRequest>();
                for (PullRequest request : requestList) {
                    long newestOffset = maxOffset;
                    if (newestOffset <= request.getPullFromThisOffset()) {
                        // 保证newestOffset为 queue的 maxOffset
                        newestOffset = this.brokerController.getMessageStore().getMaxOffsetInQueue(topic, queueId);
                    }

                    // 条件成立：说明该request关注的queue内有 本次pull 查询的数据了，长轮询该结束了
                    if (newestOffset > request.getPullFromThisOffset()) {
                        boolean match = request.getMessageFilter().isMatchedByConsumeQueue(tagsCode,
                            new ConsumeQueueExt.CqExtUnit(tagsCode, msgStoreTime, filterBitMap));
                        // match by bit map, need eval again when properties is not null.
                        if (match && properties != null) {
                            match = request.getMessageFilter().isMatchedByCommitLog(null, properties);
                        }
                        if (match) {
                            try {
                                // 将满足条件的 pullRequest 再次封装出 RequestTask 提交到 线程池内执行，
                                // 会再次调用 PullMessageProcess.processRequest(...) 三个参数的方法，并且 不允许再次长轮询。
                                this.brokerController.getPullMessageProcessor().executeRequestWhenWakeup(request.getClientChannel(),
                                    request.getRequestCommand());
                            } catch (Throwable e) {
                                log.error(
                                    "PullRequestHoldService#notifyMessageArriving: failed to execute request when "
                                        + "message matched, topic={}, queueId={}", topic, queueId, e);
                            }
                            continue;
                        }
                    }

                    // 判断 该 pullRequest 是否超时，超时，也是将它再次封装出 RequestTask 提交到 线程池内执行   三个参数的方法，并且 不允许再次长轮询。
                    if (System.currentTimeMillis() >= (request.getSuspendTimestamp() + request.getTimeoutMillis())) {
                        try {
                            this.brokerController.getPullMessageProcessor().executeRequestWhenWakeup(request.getClientChannel(),
                                request.getRequestCommand());
                        } catch (Throwable e) {
                            log.error(
                                "PullRequestHoldService#notifyMessageArriving: failed to execute request when time's "
                                    + "up, topic={}, queueId={}", topic, queueId, e);
                        }
                        continue;
                    }

                    //既没有超时，也没有满足条件，重新方法重试队列里去
                    replayList.add(request);
                }

                if (!replayList.isEmpty()) {
                    mpr.addPullRequest(replayList);
                }
            }
        }
    }

    public void notifyMasterOnline() {
        for (ManyPullRequest mpr : this.pullRequestTable.values()) {
            if (mpr == null || mpr.isEmpty()) {
                continue;
            }
            for (PullRequest request : mpr.cloneListAndClear()) {
                try {
                    log.info("notify master online, wakeup {} {}", request.getClientChannel(), request.getRequestCommand());
                    this.brokerController.getPullMessageProcessor().executeRequestWhenWakeup(request.getClientChannel(),
                        request.getRequestCommand());
                } catch (Throwable e) {
                    log.error("execute request when master online failed.", e);
                }
            }
        }

    }
}
