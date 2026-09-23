# 票务分销上架 - API 原始数据表
# 供应商接入上下文，只存原始数据，不聚合、不上架。

CREATE database if NOT EXISTS `ticket_catalog` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use `ticket_catalog`;

# ------------------------------------------------------------
# supplier 供应商
# 包含全部 API 供应商，另有一条固定的 OFFLINE 虚拟供应商。
# ------------------------------------------------------------

DROP TABLE IF EXISTS `supplier`;

CREATE TABLE `supplier` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID；供应商ID',
  `name` varchar(64) NOT NULL COMMENT '供应商名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商';

# ------------------------------------------------------------
# api_project 供应商原始项目
# ------------------------------------------------------------

DROP TABLE IF EXISTS `api_project`;

CREATE TABLE `api_project` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `supplier_id` bigint unsigned NOT NULL COMMENT '供应商ID；关联 supplier 表',
  `supplier_project_id` varchar(64) NOT NULL COMMENT '供应商侧项目ID',
  `poi_id` bigint unsigned DEFAULT NULL COMMENT '平台 POI ID；关联 poi.poi_id，不是 poi 表主键',
  `name` varchar(128) NOT NULL COMMENT '项目名称',
  `poster_url` varchar(512) DEFAULT NULL COMMENT '海报地址',
  `ticket_notice` text COMMENT '购票须知',
  `sale_status` tinyint NOT NULL DEFAULT '0' COMMENT '售卖状态；0停售 1在售',
  `real_name` tinyint NOT NULL DEFAULT '0' COMMENT '是否实名；0否 1是',
  `seat_selection` tinyint NOT NULL DEFAULT '0' COMMENT '是否选座；0否 1是',
  `timezone` varchar(64) NOT NULL COMMENT 'IANA 时区，如 Asia/Shanghai。场次与售卖时间都按此时区解释，不存 +08:00',
  `earliest_session_start_time` datetime DEFAULT NULL COMMENT '最早场次开始时间；项目时区下的钟面时间',
  `latest_session_start_time` datetime DEFAULT NULL COMMENT '最晚场次开始时间；项目时区下的钟面时间',
  `stock_mode` tinyint NOT NULL COMMENT '合作方式；1共享库存 2买断库存',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_project` (`supplier_id`, `supplier_project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商原始项目';

# ------------------------------------------------------------
# poi 平台 POI
# poi_id 由外部服务 RPC 分配，多个系统共用，不能用本表主键代替。
# ------------------------------------------------------------

DROP TABLE IF EXISTS `poi`;

CREATE TABLE `poi` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID；仅本表主键，不对外作为 POI 标识',
  `supplier_id` bigint unsigned NOT NULL COMMENT '供应商ID；关联 supplier 表',
  `supplier_poi_name` varchar(128) NOT NULL COMMENT '供应商侧 POI 名称',
  `supplier_poi_address` varchar(256) DEFAULT NULL COMMENT '供应商侧 POI 地址',
  `poi_id` bigint unsigned NOT NULL COMMENT '平台分配的 POI ID；调用外部 RPC 生成，不是本表主键',
  `poi_name` varchar(128) NOT NULL COMMENT '平台 POI 名称',
  `longitude` decimal(10,7) NOT NULL COMMENT '经度',
  `latitude` decimal(10,7) NOT NULL COMMENT '纬度',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_poi_id` (`poi_id`),
  KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台 POI';

# ------------------------------------------------------------
# api_session 供应商原始场次
# ------------------------------------------------------------

DROP TABLE IF EXISTS `api_session`;

CREATE TABLE `api_session` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `supplier_id` bigint unsigned NOT NULL COMMENT '供应商ID；关联 supplier 表',
  `project_id` bigint unsigned NOT NULL COMMENT '项目ID；关联 api_project.id',
  `supplier_session_id` varchar(64) NOT NULL COMMENT '供应商侧场次ID',
  `name` varchar(128) NOT NULL COMMENT '场次名称',
  `session_start_time` datetime NOT NULL COMMENT '场次开始时间；项目时区下的钟面时间',
  `session_end_time` datetime DEFAULT NULL COMMENT '场次结束时间；项目时区下的钟面时间',
  `sale_start_time` datetime DEFAULT NULL COMMENT '开售时间；项目时区下的钟面时间',
  `sale_end_time` datetime DEFAULT NULL COMMENT '停售时间；项目时区下的钟面时间',
  `real_name` tinyint NOT NULL DEFAULT '0' COMMENT '是否实名；0否 1是',
  `seat_selection` tinyint NOT NULL DEFAULT '0' COMMENT '是否选座；0否 1是',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '场次状态；0停售 1可售',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_session` (`supplier_id`, `supplier_session_id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商原始场次';

# ------------------------------------------------------------
# api_ticket_item 供应商原始票品（票档）
# 展示库存存在这里，供应商兜底，本系统不扣减真实库存。
# ------------------------------------------------------------

DROP TABLE IF EXISTS `api_ticket_item`;

CREATE TABLE `api_ticket_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `supplier_id` bigint unsigned NOT NULL COMMENT '供应商ID；关联 supplier 表',
  `supplier_ticket_item_id` varchar(64) NOT NULL COMMENT '供应商侧票品ID',
  `session_id` bigint unsigned NOT NULL COMMENT '场次ID；关联 api_session.id',
  `name` varchar(128) NOT NULL COMMENT '票品名称',
  `description` varchar(512) DEFAULT NULL COMMENT '票品描述',
  `min_sale_qty` int NOT NULL DEFAULT '1' COMMENT '最小售卖数量',
  `max_sale_qty` int NOT NULL DEFAULT '1' COMMENT '最大售卖数量',
  `price` decimal(10,2) NOT NULL COMMENT '价格；禁止使用 float、double',
  `currency` varchar(8) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `stock_show_num` int NOT NULL DEFAULT '0' COMMENT '展示库存；供应商兜底，本系统不扣减',
  `plu` varchar(64) DEFAULT NULL COMMENT '供应商 PLU 编码',
  `plu_id` bigint unsigned DEFAULT NULL COMMENT '聚合后的 PLU ID；关联 api_plu.id，未聚合为空',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '票品状态；0停售 1可售',
  `person_count` int NOT NULL DEFAULT '1' COMMENT '人数；套票时大于 1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_ticket_item` (`supplier_id`, `supplier_ticket_item_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_plu_id` (`plu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商原始票品';
