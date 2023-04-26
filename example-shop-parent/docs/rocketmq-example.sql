/*
 Navicat Premium Data Transfer

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 50741 (5.7.41)
 Source Host           : localhost:3306
 Source Schema         : rocketmq-example

 Target Server Type    : MySQL
 Target Server Version : 50741 (5.7.41)
 File Encoding         : 65001

 Date: 26/04/2023 16:27:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for consumer_msg
-- ----------------------------
DROP TABLE IF EXISTS `consumer_msg`;
CREATE TABLE `consumer_msg` (
  `msg_id` varchar(50) DEFAULT NULL COMMENT '消息ID',
  `group_name` varchar(255) NOT NULL COMMENT '消费者组名',
  `msg_tag` varchar(255) NOT NULL COMMENT 'Tag',
  `msg_key` varchar(255) NOT NULL COMMENT 'Key',
  `msg_body` varchar(500) DEFAULT NULL COMMENT '消息体',
  `consumer_status` int(1) DEFAULT NULL COMMENT '0:正在处理;1:处理成功;2:处理失败',
  `consumer_times` int(1) DEFAULT NULL COMMENT '消费次数',
  `consumer_timestamp` datetime DEFAULT NULL COMMENT '消费时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='MQ消息消费表';

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `coupon_id` bigint(50) NOT NULL COMMENT '优惠券ID',
  `coupon_price` decimal(10,2) DEFAULT NULL COMMENT '优惠券金额',
  `user_id` bigint(50) DEFAULT NULL COMMENT '用户ID',
  `order_id` bigint(50) DEFAULT NULL COMMENT '订单ID',
  `is_used` int(1) DEFAULT NULL COMMENT '是否使用 0未使用 1已使用',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  PRIMARY KEY (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='优惠券表';

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `goods_id` bigint(50) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `goods_number` int(11) DEFAULT NULL COMMENT '商品库存',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `goods_desc` varchar(255) DEFAULT NULL COMMENT '商品描述',
  `add_time` datetime DEFAULT NULL COMMENT '添加时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='商品表';

-- ----------------------------
-- Table structure for goods_order_log
-- ----------------------------
DROP TABLE IF EXISTS `goods_order_log`;
CREATE TABLE `goods_order_log` (
  `goods_id` bigint(50) NOT NULL COMMENT '商品ID',
  `order_id` bigint(50) NOT NULL COMMENT '订单ID',
  `goods_number` int(11) DEFAULT NULL COMMENT '库存数量',
  `log_time` datetime DEFAULT NULL COMMENT '记录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单商品日志表';

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `order_id` bigint(50) NOT NULL COMMENT '订单ID',
  `user_id` bigint(50) DEFAULT NULL COMMENT '用户ID',
  `order_status` int(1) DEFAULT NULL COMMENT '订单状态 0未确认 1已确认 2已取消 3无效 4退款',
  `pay_status` int(1) DEFAULT NULL COMMENT '支付状态 0未支付 1支付中 2已支付',
  `shipping_status` int(1) DEFAULT NULL COMMENT '发货状态 0未发货 1已发货 2已退货',
  `address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `consignee` varchar(255) DEFAULT NULL COMMENT '收货人',
  `goods_id` bigint(50) DEFAULT NULL COMMENT '商品ID',
  `goods_number` int(11) DEFAULT NULL COMMENT '商品数量',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `goods_amount` decimal(10,0) DEFAULT NULL COMMENT '商品总价',
  `shipping_fee` decimal(10,2) DEFAULT NULL COMMENT '运费',
  `order_amount` decimal(10,2) DEFAULT NULL COMMENT '订单价格',
  `coupon_id` bigint(50) DEFAULT NULL COMMENT '优惠券ID',
  `coupon_paid` decimal(10,2) DEFAULT NULL COMMENT '优惠券',
  `money_paid` decimal(10,2) DEFAULT NULL COMMENT '已付金额',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `add_time` datetime DEFAULT NULL COMMENT '创建时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '订单确认时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单表';

-- ----------------------------
-- Table structure for order_pay
-- ----------------------------
DROP TABLE IF EXISTS `order_pay`;
CREATE TABLE `order_pay` (
  `pay_id` bigint(50) NOT NULL AUTO_INCREMENT COMMENT '支付编号',
  `order_id` bigint(50) DEFAULT NULL COMMENT '订单编号',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `is_paid` int(1) DEFAULT NULL COMMENT '是否已支付 1否 2是',
  PRIMARY KEY (`pay_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单支付表';

-- ----------------------------
-- Table structure for producer_msg
-- ----------------------------
DROP TABLE IF EXISTS `producer_msg`;
CREATE TABLE `producer_msg` (
  `id` varchar(100) NOT NULL COMMENT '主键',
  `group_name` varchar(255) DEFAULT NULL COMMENT '生产者组名',
  `msg_topic` varchar(255) DEFAULT NULL COMMENT '消息主题',
  `msg_tag` varchar(255) DEFAULT NULL COMMENT 'Tag',
  `msg_key` varchar(255) DEFAULT NULL COMMENT 'Key',
  `msg_body` varchar(255) DEFAULT NULL COMMENT '消息内容',
  `msg_status` int(1) DEFAULT NULL COMMENT '0:未处理;1:已经处理',
  `create_time` datetime DEFAULT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='MQ消息生产表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` bigint(50) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(255) DEFAULT NULL COMMENT '用户姓名',
  `user_password` varchar(255) DEFAULT NULL COMMENT '用户密码',
  `user_mobile` varchar(255) DEFAULT NULL COMMENT '手机号',
  `user_score` int(11) DEFAULT NULL COMMENT '积分',
  `user_reg_time` datetime DEFAULT NULL COMMENT '注册时间',
  `user_money` decimal(10,2) DEFAULT NULL COMMENT '用户余额',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='用户表';

-- ----------------------------
-- Table structure for user_money_log
-- ----------------------------
DROP TABLE IF EXISTS `user_money_log`;
CREATE TABLE `user_money_log` (
  `user_id` bigint(50) NOT NULL COMMENT '用户ID',
  `order_id` bigint(50) NOT NULL COMMENT '订单ID',
  `money_log_type` int(1) DEFAULT NULL COMMENT '日志类型 1订单付款 2 订单退款',
  `use_money` decimal(10,2) DEFAULT NULL COMMENT '操作金额',
  `create_time` datetime DEFAULT NULL COMMENT '日志时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户余额日志表';

SET FOREIGN_KEY_CHECKS = 1;
