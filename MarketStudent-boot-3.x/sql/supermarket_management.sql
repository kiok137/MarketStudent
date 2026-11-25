-- =============================================================
-- 超市进销存管理系统 (MarketStudent) 数据库初始化脚本
-- 适用：MySQL 8.0+
-- 用法：mysql -uroot -p < supermarket_management.sql
-- 说明：包含全部 15 张表结构与初始化数据（含菜单、角色、示例业务数据）
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `supermarket_management`;
CREATE DATABASE `supermarket_management` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `supermarket_management`;

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `category_id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` int DEFAULT NULL COMMENT '父分类ID',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分类描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品分类表';
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `inventory_id` int NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `product_id` int NOT NULL COMMENT '商品ID，外键',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `min_quantity` int NOT NULL DEFAULT '10' COMMENT '最低库存量',
  `max_quantity` int NOT NULL DEFAULT '1000' COMMENT '最高库存量',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`inventory_id`) USING BTREE,
  UNIQUE KEY `uk_product` (`product_id`) USING BTREE,
  CONSTRAINT `inventory_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存表';
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `product_id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `category_id` int NOT NULL COMMENT '分类ID，外键',
  `barcode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品条码',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计量单位',
  `retail_price` decimal(10,2) NOT NULL COMMENT '零售价格',
  `cost_price` decimal(10,2) NOT NULL COMMENT '成本价格',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品图片URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-下架，1-上架）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '商品描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`product_id`) USING BTREE,
  KEY `category_id` (`category_id`) USING BTREE,
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品表';
DROP TABLE IF EXISTS `product_batch_quality`;
CREATE TABLE `product_batch_quality` (
  `quality_id` int NOT NULL AUTO_INCREMENT COMMENT '质量记录ID',
  `product_id` int NOT NULL COMMENT '商品ID，外键',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '批次号',
  `supplier_id` int NOT NULL COMMENT '供应商ID，外键',
  `quality_level` tinyint NOT NULL COMMENT '质量等级（1-优，2-良，3-中，4-差）',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '质量评价内容',
  `user_id` int NOT NULL COMMENT '评价用户ID，外键',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`quality_id`) USING BTREE,
  KEY `product_id` (`product_id`) USING BTREE,
  KEY `supplier_id` (`supplier_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `product_batch_quality_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `product_batch_quality_ibfk_2` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`supplier_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `product_batch_quality_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品批次质量表';
DROP TABLE IF EXISTS `purchase_order`;
CREATE TABLE `purchase_order` (
  `order_id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `supplier_id` int NOT NULL COMMENT '供应商ID，外键',
  `user_id` int NOT NULL COMMENT '下单用户ID，外键',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态（0-待审核，1-已审核，2-已收货，3-已取消）',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `approved_at` datetime DEFAULT NULL COMMENT '审核时间',
  `received_at` datetime DEFAULT NULL COMMENT '收货时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE KEY `order_no` (`order_no`) USING BTREE,
  KEY `supplier_id` (`supplier_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `purchase_order_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`supplier_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `purchase_order_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购订单表';
DROP TABLE IF EXISTS `purchase_order_detail`;
CREATE TABLE `purchase_order_detail` (
  `detail_id` int NOT NULL AUTO_INCREMENT COMMENT '详情ID',
  `order_id` int NOT NULL COMMENT '订单ID，外键',
  `product_id` int NOT NULL COMMENT '商品ID，外键',
  `quantity` int NOT NULL COMMENT '采购数量',
  `price` decimal(10,2) NOT NULL COMMENT '采购单价',
  `amount` decimal(10,2) NOT NULL COMMENT '采购金额',
  `received_quantity` int NOT NULL DEFAULT '0' COMMENT '已收货数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`detail_id`) USING BTREE,
  KEY `order_id` (`order_id`) USING BTREE,
  KEY `product_id` (`product_id`) USING BTREE,
  CONSTRAINT `purchase_order_detail_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `purchase_order_detail_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购订单详情表';
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `role_id` int NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '角色描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色表';
DROP TABLE IF EXISTS `stock_in`;
CREATE TABLE `stock_in` (
  `stock_in_id` int NOT NULL AUTO_INCREMENT COMMENT '入库ID',
  `stock_in_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '入库单号',
  `order_id` int DEFAULT NULL COMMENT '采购订单ID，外键',
  `product_id` int NOT NULL COMMENT '商品ID，外键',
  `quantity` int NOT NULL COMMENT '入库数量',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '批次号',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '过期日期',
  `user_id` int NOT NULL COMMENT '操作用户ID，外键',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`stock_in_id`) USING BTREE,
  UNIQUE KEY `stock_in_no` (`stock_in_no`) USING BTREE,
  KEY `order_id` (`order_id`) USING BTREE,
  KEY `product_id` (`product_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `stock_in_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `stock_in_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `stock_in_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='入库记录表';
DROP TABLE IF EXISTS `stock_out`;
CREATE TABLE `stock_out` (
  `stock_out_id` int NOT NULL AUTO_INCREMENT COMMENT '出库ID',
  `stock_out_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出库单号',
  `product_id` int NOT NULL COMMENT '商品ID，外键',
  `quantity` int NOT NULL COMMENT '出库数量',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '批次号',
  `user_id` int NOT NULL COMMENT '操作用户ID，外键',
  `department` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '领用部门',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`stock_out_id`) USING BTREE,
  UNIQUE KEY `stock_out_no` (`stock_out_no`) USING BTREE,
  KEY `product_id` (`product_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `stock_out_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `stock_out_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='出库记录表';
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier` (
  `supplier_id` int NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '电话号码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '电子邮箱',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`supplier_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='供应商表';
DROP TABLE IF EXISTS `supplier_evaluation`;
CREATE TABLE `supplier_evaluation` (
  `evaluation_id` int NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `supplier_id` int NOT NULL COMMENT '供应商ID，外键',
  `order_id` int NOT NULL COMMENT '订单ID，外键',
  `delivery_speed_score` int NOT NULL COMMENT '配送速度评分（1-5）',
  `quality_score` int NOT NULL COMMENT '商品质量评分（1-5）',
  `service_score` int NOT NULL COMMENT '服务态度评分（1-5）',
  `price_score` int NOT NULL COMMENT '价格评分（1-5）',
  `total_score` int NOT NULL COMMENT '总评分',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '评价内容',
  `user_id` int NOT NULL COMMENT '评价用户ID，外键',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`evaluation_id`) USING BTREE,
  KEY `supplier_id` (`supplier_id`) USING BTREE,
  KEY `order_id` (`order_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `supplier_evaluation_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`supplier_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `supplier_evaluation_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `supplier_evaluation_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='供应商评价表';
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单路径',
  `component` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单图标',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `pid` int DEFAULT NULL COMMENT '父菜单ID',
  `sort_num` int DEFAULT '1' COMMENT '排序号',
  `hidden` tinyint DEFAULT '0' COMMENT '是否隐藏',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='菜单表';
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` int NOT NULL COMMENT '角色ID',
  `menu_id` int NOT NULL COMMENT '菜单ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`) USING BTREE,
  KEY `menu_id` (`menu_id`) USING BTREE,
  CONSTRAINT `sys_role_menu_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `sys_role_menu_ibfk_2` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=217 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色菜单关联表';
DROP TABLE IF EXISTS `system_log`;
CREATE TABLE `system_log` (
  `log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `operation_type` tinyint NOT NULL COMMENT '操作类型: 0-登录, 1-登出, 2-增加, 3-删除, 4-修改, 5-查询, 6-其他',
  `operation_module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作模块',
  `operation_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作描述',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'IP地址',
  `operation_time` datetime NOT NULL COMMENT '操作时间',
  `device_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '设备信息',
  `status` tinyint NOT NULL COMMENT '操作状态: 0-失败, 1-成功',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请求方法',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '请求参数',
  `execution_time` bigint DEFAULT NULL COMMENT '执行时长(毫秒)',
  `exception_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '异常信息',
  PRIMARY KEY (`log_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_operation_time` (`operation_time`) USING BTREE,
  KEY `idx_operation_type` (`operation_type`) USING BTREE,
  KEY `idx_ip_address` (`ip_address`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  CONSTRAINT `fk_system_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=171 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统日志表';
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '真实姓名',
  `role_id` int NOT NULL COMMENT '角色ID，外键',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '电话号码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '电子邮箱',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE,
  KEY `role_id` (`role_id`) USING BTREE,
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';

INSERT INTO `category` VALUES (1,'食品',NULL,'食品类商品','2025-04-05 23:43:30','2025-04-06 13:52:37');
INSERT INTO `category` VALUES (2,'饮料',NULL,'饮料类商品','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `category` VALUES (3,'日用品',NULL,'日常生活用品','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `category` VALUES (4,'新鲜水果',1,'新鲜水果类商品','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `category` VALUES (5,'休闲零食',1,'休闲零食类商品','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `category` VALUES (6,'碳酸饮料',2,'碳酸饮料类商品','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `category` VALUES (7,'果汁饮料',2,'果汁饮料类商品','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `category` VALUES (8,'清洁用品',3,'清洁用品类商品','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `category` VALUES (9,'个人护理',3,'个人护理类商品','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `inventory` VALUES (1,1,101,51,501,'2025-08-23 00:58:10');
INSERT INTO `inventory` VALUES (2,2,150,30,300,'2025-04-06 16:02:45');
INSERT INTO `inventory` VALUES (3,3,415,100,1000,'2025-08-22 21:05:45');
INSERT INTO `inventory` VALUES (4,4,250,100,1000,'2025-04-05 23:43:30');
INSERT INTO `inventory` VALUES (5,5,100,30,300,'2025-04-05 23:43:30');
INSERT INTO `inventory` VALUES (6,6,80,20,200,'2025-04-05 23:43:30');
INSERT INTO `inventory` VALUES (7,7,120,30,300,'2025-04-05 23:43:30');
INSERT INTO `inventory` VALUES (8,8,200,50,500,'2025-04-05 23:43:30');
INSERT INTO `inventory` VALUES (9,9,150,160,300,'2025-04-17 23:32:08');
INSERT INTO `inventory` VALUES (10,10,100,20,200,'2025-04-05 23:43:30');
INSERT INTO `inventory` VALUES (12,12,14,10,1000,'2025-04-17 23:31:48');
INSERT INTO `product` VALUES (1,'红富士苹果',4,'6901234567890','斤',6.50,3.80,'/img/apple.jpg',1,'新鲜红富士苹果，产地陕西1','2025-04-05 23:43:30','2025-08-23 01:02:20');
INSERT INTO `product` VALUES (2,'香蕉',4,'6901234567891','斤',3.98,2.50,'/img/banana.jpg',1,'进口香蕉，口感香甜1','2025-04-05 23:43:30','2025-08-23 01:02:28');
INSERT INTO `product` VALUES (3,'可口可乐',6,'6901234567892','瓶',3.00,2.00,'/img/cola.jpg',1,'可口可乐500ml','2025-04-05 23:43:30','2025-08-23 01:02:46');
INSERT INTO `product` VALUES (4,'百事可乐',6,'6901234567893','瓶',3.00,2.00,'/img/pepsi.jpg',1,'百事可乐500ml','2025-04-05 23:43:30','2025-08-23 01:03:12');
INSERT INTO `product` VALUES (5,'橙汁',7,'6901234567894','瓶',5.00,3.50,'/img/orange.jpg',1,'纯鲜橙汁500ml','2025-04-05 23:43:30','2025-08-23 01:03:23');
INSERT INTO `product` VALUES (6,'洗衣液',8,'6901234567895','瓶',15.90,10.50,'/img/washing.jpg',1,'超能洗衣液2kg','2025-04-05 23:43:30','2025-08-23 01:03:47');
INSERT INTO `product` VALUES (7,'牙膏',9,'6901234567896','支',9.90,6.50,'/img/toothpaste.jpg',1,'高露洁牙膏120g','2025-04-05 23:43:30','2025-08-23 01:03:58');
INSERT INTO `product` VALUES (8,'薯片',5,'6901234567897','包',8.50,5.50,'/img/chip.jpg',1,'乐事薯片原味104g','2025-04-05 23:43:30','2025-08-23 01:04:12');
INSERT INTO `product` VALUES (9,'巧克力',5,'6901234567898','块',12.50,8.50,'/img/chocolate.jpg',1,'德芙丝滑牛奶巧克力100g','2025-04-05 23:43:30','2025-08-23 01:04:20');
INSERT INTO `product` VALUES (10,'卫生纸',8,'6901234567899','提',14.90,10.00,'/img/paper.jpg',1,'维达卫生纸10卷','2025-04-05 23:43:30','2025-08-23 01:04:28');
INSERT INTO `product` VALUES (12,'胶带',3,'16156165159165','个',2.00,1.00,'/img/tape.jpg',1,'1111111111','2025-04-17 23:26:44','2025-08-23 01:04:41');
INSERT INTO `product_batch_quality` VALUES (1,1,'B202204010001',1,1,'苹果非常新鲜，品质优异',2,'2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `product_batch_quality` VALUES (2,2,'B202204010002',1,2,'香蕉成熟度适中，品质良好',2,'2025-04-05 23:43:30','2025-08-23 00:10:40');
INSERT INTO `product_batch_quality` VALUES (3,6,'B202204010003',3,1,'洗衣液清洁效果好，泡沫丰富',3,'2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `product_batch_quality` VALUES (4,12,'PC71115',5,2,'',1,'2025-04-17 23:28:32','2025-04-17 23:28:32');
INSERT INTO `purchase_order` VALUES (1,'PO202204010001',1,2,2000.00,1,'水果采购订单','2025-04-05 23:43:30',NULL,NULL,'2025-04-05 23:43:30');
INSERT INTO `purchase_order` VALUES (2,'PO202204010002',2,2,1500.00,3,'饮料采购订单','2025-04-05 23:43:30','2025-04-06 15:02:20',NULL,'2025-04-18 09:34:06');
INSERT INTO `purchase_order` VALUES (3,'PO202204010003',3,3,3000.00,2,'日用品采购订单','2025-04-05 23:43:30',NULL,NULL,'2025-04-05 23:43:30');
INSERT INTO `purchase_order` VALUES (4,'PO20250406E01B56BC',2,1,17.40,2,'111','2025-04-06 15:15:18','2025-04-06 20:38:43','2025-04-06 20:40:57','2025-04-06 20:40:57');
INSERT INTO `purchase_order` VALUES (5,'PO202504067985E74D',2,1,16.00,2,'1111','2025-04-06 20:45:59','2025-04-06 20:46:18','2025-04-06 20:46:26','2025-04-06 20:46:26');
INSERT INTO `purchase_order` VALUES (6,'PO20250417B64675A8',5,1,38.00,2,'无','2025-04-17 23:28:59','2025-04-17 23:29:22','2025-04-17 23:29:42','2025-04-17 23:29:42');
INSERT INTO `purchase_order` VALUES (7,'PO202505136E3070C2',1,1,19.00,2,'1111','2025-05-13 18:40:06','2025-05-13 18:40:09','2025-05-13 18:47:16','2025-05-13 18:47:16');
INSERT INTO `purchase_order_detail` VALUES (1,1,1,300,3.80,1140.00,8,'2025-04-06 20:38:18','2025-04-06 20:45:03');
INSERT INTO `purchase_order_detail` VALUES (2,1,2,200,2.50,500.00,3,'2025-04-06 20:38:18','2025-04-06 20:43:31');
INSERT INTO `purchase_order_detail` VALUES (3,2,3,500,2.00,1000.00,0,'2025-04-06 20:38:18','2025-04-06 20:38:18');
INSERT INTO `purchase_order_detail` VALUES (4,2,5,100,3.50,350.00,0,'2025-04-06 20:38:18','2025-04-06 20:38:18');
INSERT INTO `purchase_order_detail` VALUES (5,3,6,100,10.50,1050.00,100,'2025-04-06 20:38:18','2025-04-06 20:38:18');
INSERT INTO `purchase_order_detail` VALUES (6,3,7,200,6.50,1300.00,200,'2025-04-06 20:38:18','2025-04-06 20:38:18');
INSERT INTO `purchase_order_detail` VALUES (7,3,10,50,10.00,500.00,50,'2025-04-06 20:38:18','2025-04-06 20:38:18');
INSERT INTO `purchase_order_detail` VALUES (8,4,1,3,5.80,17.40,3,'2025-04-06 20:38:37','2025-04-06 20:40:57');
INSERT INTO `purchase_order_detail` VALUES (9,5,5,3,4.50,13.50,3,'2025-04-06 20:46:12','2025-04-06 20:46:22');
INSERT INTO `purchase_order_detail` VALUES (10,5,2,1,2.50,2.50,1,'2025-04-06 20:46:12','2025-04-06 20:46:26');
INSERT INTO `purchase_order_detail` VALUES (11,6,12,12,1.00,12.00,12,'2025-04-17 23:28:59','2025-04-17 23:29:33');
INSERT INTO `purchase_order_detail` VALUES (12,6,7,4,6.50,26.00,4,'2025-04-17 23:28:59','2025-04-17 23:29:42');
INSERT INTO `purchase_order_detail` VALUES (13,7,1,5,3.80,19.00,5,'2025-05-13 18:40:06','2025-05-13 18:47:16');
INSERT INTO `role` VALUES (1,'管理员','系统管理员，拥有所有权限','2025-04-05 23:43:30','2025-04-07 12:02:08');
INSERT INTO `role` VALUES (2,'普通用户','普通员工，负责日常业务操作','2025-04-05 23:43:30','2025-04-07 12:02:12');
INSERT INTO `stock_in` VALUES (1,'SI202204010001',1,1,300,'B202204010001','2022-03-20','2022-04-20',2,'苹果入库','2025-04-06 20:37:29','2025-04-06 20:37:29');
INSERT INTO `stock_in` VALUES (2,'SI202204010002',1,2,150,'B202204010002','2022-03-25','2022-04-15',2,'香蕉入库','2025-04-06 20:37:29','2025-04-06 20:37:29');
INSERT INTO `stock_in` VALUES (3,'SI202204010003',3,6,100,'B202204010003','2022-02-15','2024-02-15',3,'洗衣液入库','2025-04-06 20:37:29','2025-04-06 20:37:29');
INSERT INTO `stock_in` VALUES (4,'SI202204010004',3,7,200,'B202204010004','2022-01-10','2023-01-10',3,'牙膏入库','2025-04-06 20:37:29','2025-04-06 20:37:29');
INSERT INTO `stock_in` VALUES (5,'SI202204010005',3,10,50,'B202204010005','2022-03-01','2024-03-01',3,'卫生纸入库','2025-04-06 20:37:29','2025-04-06 20:37:29');
INSERT INTO `stock_in` VALUES (6,'SI2025041790F72723',NULL,12,4,'qswdqqdqw','2025-04-16','2025-04-25',1,'','2025-04-17 23:30:14','2025-04-17 23:30:14');
INSERT INTO `stock_in` VALUES (7,'SI20250430EC11CB23',NULL,3,115,'BT34567890','2025-04-17','2025-05-08',2,'','2025-04-30 18:29:15','2025-08-22 21:05:45');
INSERT INTO `stock_in` VALUES (8,'SI20250513241464',7,1,2,'PO7',NULL,NULL,1,'采购订单入库：PO202505136E3070C2，商品：红富士苹果','2025-05-13 18:47:16','2025-05-13 18:47:16');
INSERT INTO `stock_out` VALUES (1,'SO202204010001',1,50,'B202204010001',2,'销售部','苹果出库','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `stock_out` VALUES (2,'SO202204010002',2,30,'B202204010002',2,'销售部','香蕉出库','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `stock_out` VALUES (3,'SO202204010003',6,20,'B202204010003',3,'销售部','洗衣液出库','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `stock_out` VALUES (4,'SO202204010004',7,30,'B202204010004',3,'销售部','牙膏出库','2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `stock_out` VALUES (6,'SO20250417F3D0D09D',1,1,'wdewdewdq',1,'未知','','2025-04-17 23:30:31','2025-04-17 23:30:31');
INSERT INTO `stock_out` VALUES (7,'SO20250513895417',1,50,NULL,1,'销售部','企业订单出库：ES20250513706503','2025-05-13 18:23:35','2025-05-13 18:23:35');
INSERT INTO `supplier` VALUES (1,'绿色果园有限公司','张经理','18612345678','green@example.com','北京市朝阳区农业园区1号',0,'2025-04-05 23:43:30','2025-04-17 23:27:02');
INSERT INTO `supplier` VALUES (2,'可乐饮料有限公司','王经理','18612345679','cola@example.com','上海市浦东新区工业园2号',1,'2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `supplier` VALUES (3,'优质生活用品有限公司','李经理','18612345680','quality@example.com','广州市天河区商业大道3号',1,'2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `supplier` VALUES (4,'休闲零食有限公司','赵经理','18612345681','snack@example.com','深圳市南山区科技路4号',1,'2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `supplier` VALUES (5,'百货','baihuo','13912345678','1796145602@qq.com','中国台湾省',1,'2025-04-17 23:27:32','2025-04-17 23:27:39');
INSERT INTO `supplier_evaluation` VALUES (1,1,1,5,5,5,5,20,'水果新鲜，配送及时',2,'2025-04-05 23:43:30','2025-08-23 00:10:06');
INSERT INTO `supplier_evaluation` VALUES (2,3,3,4,5,4,3,16,'商品质量好，价格稍高',3,'2025-04-05 23:43:30','2025-04-05 23:43:30');
INSERT INTO `supplier_evaluation` VALUES (3,5,1,2,4,4,4,14,'',1,'2025-04-17 23:27:56','2025-04-17 23:27:56');
INSERT INTO `sys_menu` VALUES (1,'系统管理','/system','','Setting','系统管理菜单',NULL,2,0,'2025-04-05 23:43:30','2025-04-06 21:57:58');
INSERT INTO `sys_menu` VALUES (2,'用户管理','/system/user','system/user/index','User','用户管理菜单',1,1,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (3,'角色管理','/system/role','system/role/index','UserFilled','角色管理菜单',1,2,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (4,'菜单管理','/system/menu','system/menu/index','Menu','菜单管理菜单',1,3,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (5,'商品管理','/product',NULL,'Goods','商品管理菜单',NULL,2,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (6,'商品列表','/product/list','product/list/index','ShoppingBag','商品列表菜单',5,1,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (7,'商品分类','/product/category','product/category/index','Folder','商品分类菜单',5,2,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (8,'供应商管理','/supplier',NULL,'OfficeBuilding','供应商管理菜单',NULL,3,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (9,'供应商列表','/supplier/list','supplier/index','Notebook','供应商列表菜单',8,1,0,'2025-04-05 23:43:30','2025-04-06 14:23:11');
INSERT INTO `sys_menu` VALUES (10,'供应商评价','/supplier/evaluation','supplier/evaluation/index','Star','供应商评价菜单',8,2,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (11,'采购管理','/purchase',NULL,'ShoppingCart','采购管理菜单',NULL,4,0,'2025-04-06 20:33:43','2025-04-06 20:33:43');
INSERT INTO `sys_menu` VALUES (12,'采购订单','/purchase/order','purchase/order/index','Document','采购订单菜单',11,1,0,'2025-04-06 20:33:43','2025-04-06 20:33:43');
INSERT INTO `sys_menu` VALUES (14,'库存管理','/inventory',NULL,'Box','库存管理菜单',NULL,5,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (15,'库存列表','/inventory/list','inventory/index','Files','库存列表菜单',14,1,0,'2025-04-05 23:43:30','2025-04-06 13:57:03');
INSERT INTO `sys_menu` VALUES (16,'入库管理','/inventory/stockin','inventory/stockin/index','Download','入库管理菜单',14,2,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (17,'出库管理','/inventory/stockout','inventory/stockout/index','Upload','出库管理菜单',14,3,0,'2025-04-05 23:43:30','2025-04-06 12:01:31');
INSERT INTO `sys_menu` VALUES (23,'操作日志','/system/log','system/log/index','Timer','操作日志菜单',1,2,0,'2025-04-05 23:43:30','2025-04-07 10:22:15');
INSERT INTO `sys_menu` VALUES (24,'首页','/dashboard','Dashboard','house','系统首页  ',NULL,1,0,'2025-02-11 23:42:01','2025-04-07 10:36:26');
INSERT INTO `sys_menu` VALUES (26,'产品批次质量','/supplier/quality','supplier/quality/index','DArrowRight','产品批次质量管理菜单',8,3,0,'2025-04-06 14:25:25','2025-04-06 14:30:27');
INSERT INTO `sys_menu` VALUES (31,'个人信息','/personal-info','personInfo/index','Avatar','管理个人信息和修改密码',NULL,100,0,'2025-04-07 11:58:07','2025-04-07 12:03:40');
INSERT INTO `sys_role_menu` VALUES (174,1,24,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (175,1,1,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (176,1,2,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (177,1,3,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (178,1,23,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (179,1,4,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (180,1,5,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (181,1,6,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (182,1,7,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (183,1,8,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (184,1,9,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (185,1,10,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (186,1,26,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (187,1,11,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (188,1,12,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (189,1,14,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (190,1,15,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (191,1,16,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (192,1,17,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (199,1,31,'2025-04-07 12:02:08');
INSERT INTO `sys_role_menu` VALUES (200,2,24,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (201,2,5,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (202,2,6,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (203,2,7,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (204,2,8,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (205,2,9,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (206,2,10,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (207,2,26,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (208,2,14,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (209,2,15,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (210,2,16,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (211,2,17,'2025-04-07 12:02:12');
INSERT INTO `sys_role_menu` VALUES (216,2,31,'2025-04-07 12:02:12');
INSERT INTO `user` VALUES (1,'admin','$2a$10$P4fAjNOmpYcQCMmr9nfW7eemEwHo7Lcc/SGvLK7fIEc8pkGM5noU.','系统管理员',1,'13800138000','admin@example.com',1,'2025-04-05 23:43:30','2025-04-17 23:21:32');
INSERT INTO `user` VALUES (2,'user1','$2a$10$P4fAjNOmpYcQCMmr9nfW7eemEwHo7Lcc/SGvLK7fIEc8pkGM5noU.','张三',2,'13800138001','zhangsan@example.com',1,'2025-04-05 23:43:30','2025-04-06 19:22:00');
INSERT INTO `user` VALUES (3,'user2','$2a$10$P4fAjNOmpYcQCMmr9nfW7eemEwHo7Lcc/SGvLK7fIEc8pkGM5noU.','李四',2,'13800138002','lisi@example.com',1,'2025-04-05 23:43:30','2025-04-06 19:22:01');
INSERT INTO `user` VALUES (4,'test1','$2a$10$P4fAjNOmpYcQCMmr9nfW7eemEwHo7Lcc/SGvLK7fIEc8pkGM5noU.','王五',2,'13800138003','wangwu@supplier.com',1,'2025-04-05 23:43:30','2025-04-17 23:24:32');

SET FOREIGN_KEY_CHECKS = 1;

