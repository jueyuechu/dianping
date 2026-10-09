-- =====================================================================
-- 本地生活点评平台（dianping++）完整建表脚本
-- 对齐《数据库ER图设计.md》：tb_ 前缀、InnoDB、utf8mb4、
-- 评分乘 10 存整数、金额 decimal(10,2)、逻辑删除 deleted tinyint(1)
-- 共 48 张表（11 大领域）：
--   用户域 4 | 管理员域 6 | 商户域 5 | 菜品域 4 | 团购域 4
--   评价域 5 | 互动域 5 | 社区域 3 | 审核域 4 | 交易域 3 | 运营域 5
-- 注：tb_admin_role / tb_role_permission 为 RBAC 关联表（ER 文档隐含）
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `dianping` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `dianping`;

-- 确保客户端连接使用 utf8mb4，避免中文种子数据乱码（Windows 下 mysql 客户端默认可能为 gbk）
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. 用户域
-- ---------------------------------------------------------------------

-- 用户表
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone`       VARCHAR(11)  DEFAULT NULL COMMENT '手机号',
  `password`    VARCHAR(128) DEFAULT NULL COMMENT '密码（加密存储）',
  `openid`      VARCHAR(64)  DEFAULT NULL COMMENT '微信 openid',
  `nick_name`   VARCHAR(32)  DEFAULT NULL COMMENT '昵称',
  `icon`        VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常 0 封禁',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删 1 已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户资料表（1 对 1 扩展）
DROP TABLE IF EXISTS `tb_user_profile`;
CREATE TABLE `tb_user_profile` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      BIGINT      NOT NULL COMMENT '用户 id',
  `gender`       TINYINT     DEFAULT NULL COMMENT '性别：0 未知 1 男 2 女',
  `birthday`     DATE        DEFAULT NULL COMMENT '生日',
  `city`         VARCHAR(32) DEFAULT NULL COMMENT '城市',
  `intro`        VARCHAR(255) DEFAULT NULL COMMENT '简介',
  `fans_count`   INT         NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `follow_count` INT         NOT NULL DEFAULT 0 COMMENT '关注数',
  `points`       INT         NOT NULL DEFAULT 0 COMMENT '积分',
  `level`        TINYINT     NOT NULL DEFAULT 1 COMMENT '会员等级',
  `create_time`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_profile_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

-- 用户登录设备表（设备/IP 关联封禁）
DROP TABLE IF EXISTS `tb_user_device`;
CREATE TABLE `tb_user_device` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`         BIGINT      NOT NULL COMMENT '用户 id',
  `device_id`       VARCHAR(64) DEFAULT NULL COMMENT '设备标识',
  `ip`              VARCHAR(45) DEFAULT NULL COMMENT 'IP 地址',
  `last_login_time` TIMESTAMP   NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time`     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_device_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录设备表';

-- 用户封禁表（分级封禁）
DROP TABLE IF EXISTS `tb_user_ban`;
CREATE TABLE `tb_user_ban` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户 id',
  `ban_type`    TINYINT      NOT NULL COMMENT '封禁范围：1 禁登录 2 禁评价 3 禁评论 4 禁下单 5 禁私信',
  `reason`      VARCHAR(255) DEFAULT NULL COMMENT '封禁原因',
  `start_time`  TIMESTAMP    NULL DEFAULT NULL COMMENT '开始时间',
  `end_time`    TIMESTAMP    NULL DEFAULT NULL COMMENT '结束时间（永久则为 NULL）',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0 生效 1 解除',
  `operator_id` BIGINT       DEFAULT NULL COMMENT '操作管理员 id',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_ban_user` (`user_id`),
  KEY `idx_ban_type` (`ban_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户封禁表';

-- ---------------------------------------------------------------------
-- 2. 管理员域（RBAC）
-- ---------------------------------------------------------------------

-- 管理员表
DROP TABLE IF EXISTS `tb_admin`;
CREATE TABLE `tb_admin` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(32)  NOT NULL COMMENT '账号',
  `password`    VARCHAR(128) NOT NULL COMMENT '密码（加密存储）',
  `real_name`   VARCHAR(32)  DEFAULT NULL COMMENT '姓名',
  `phone`       VARCHAR(11)  DEFAULT NULL COMMENT '手机号',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常 0 禁用',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 角色表
DROP TABLE IF EXISTS `tb_role`;
CREATE TABLE `tb_role` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name`   VARCHAR(32) NOT NULL COMMENT '角色名：超级管理员/运营/审核员/财务/客服',
  `role_key`    VARCHAR(32) NOT NULL COMMENT '角色标识',
  `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
DROP TABLE IF EXISTS `tb_permission`;
CREATE TABLE `tb_permission` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(64) NOT NULL COMMENT '权限名',
  `code`        VARCHAR(64) NOT NULL COMMENT '权限编码',
  `type`        TINYINT     NOT NULL DEFAULT 1 COMMENT '类型：1 菜单 2 按钮 3 数据',
  `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 管理员-角色关联表
DROP TABLE IF EXISTS `tb_admin_role`;
CREATE TABLE `tb_admin_role` (
  `id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `admin_id` BIGINT NOT NULL COMMENT '管理员 id',
  `role_id`  BIGINT NOT NULL COMMENT '角色 id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_role` (`admin_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员-角色关联表';

-- 角色-权限关联表
DROP TABLE IF EXISTS `tb_role_permission`;
CREATE TABLE `tb_role_permission` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id`       BIGINT NOT NULL COMMENT '角色 id',
  `permission_id` BIGINT NOT NULL COMMENT '权限 id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- 管理员操作日志表（审计留痕）
DROP TABLE IF EXISTS `tb_admin_log`;
CREATE TABLE `tb_admin_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `admin_id`    BIGINT       NOT NULL COMMENT '管理员 id',
  `module`      VARCHAR(64)  DEFAULT NULL COMMENT '模块',
  `action`      VARCHAR(128) DEFAULT NULL COMMENT '操作',
  `detail`      TEXT         COMMENT '详情',
  `ip`          VARCHAR(45)  DEFAULT NULL COMMENT 'IP',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_alog_admin` (`admin_id`),
  KEY `idx_alog_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- ---------------------------------------------------------------------
-- 3. 商户域
-- ---------------------------------------------------------------------

-- 商户表
DROP TABLE IF EXISTS `tb_merchant`;
CREATE TABLE `tb_merchant` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`          VARCHAR(128) NOT NULL COMMENT '商户名称',
  `license_no`    VARCHAR(64)  DEFAULT NULL COMMENT '营业执照号',
  `license_img`   VARCHAR(255) DEFAULT NULL COMMENT '营业执照照片',
  `legal_person`  VARCHAR(32)  DEFAULT NULL COMMENT '法人',
  `contact_phone` VARCHAR(20)  DEFAULT NULL COMMENT '联系电话（手机或座机，含区号）',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0 待审核 1 正常 2 冻结',
  `create_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户表';

-- 商户员工账号表（商户端登录凭据）
DROP TABLE IF EXISTS `tb_merchant_account`;
CREATE TABLE `tb_merchant_account` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` BIGINT       NOT NULL COMMENT '商户 id',
  `username`    VARCHAR(32)  NOT NULL COMMENT '员工账号',
  `password`    VARCHAR(128) NOT NULL COMMENT '密码（加密存储）',
  `role`        TINYINT      NOT NULL DEFAULT 3 COMMENT '角色：1 老板 2 店长 3 员工',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常 0 禁用',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_ma_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户员工账号表';

-- 店铺表（评分乘 10 存整数）
DROP TABLE IF EXISTS `tb_shop`;
CREATE TABLE `tb_shop` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id`    BIGINT       NOT NULL COMMENT '商户 id',
  `name`           VARCHAR(128) NOT NULL COMMENT '店铺名',
  `type_id`        BIGINT       DEFAULT NULL COMMENT '品类 id（对应 tb_category）',
  `images`         VARCHAR(1024) DEFAULT NULL COMMENT '图片，逗号分隔',
  `area`           VARCHAR(128) DEFAULT NULL COMMENT '商圈',
  `address`        VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `x`              DOUBLE       DEFAULT NULL COMMENT '经度',
  `y`              DOUBLE       DEFAULT NULL COMMENT '纬度',
  `avg_price`      BIGINT       DEFAULT NULL COMMENT '人均（元）',
  `sold`           INT          NOT NULL DEFAULT 0 COMMENT '销量',
  `comments`       INT          NOT NULL DEFAULT 0 COMMENT '评论数',
  `score`          INT          NOT NULL DEFAULT 0 COMMENT '综合评分（乘10）',
  `taste_score`    INT          NOT NULL DEFAULT 0 COMMENT '口味评分（乘10）',
  `env_score`      INT          NOT NULL DEFAULT 0 COMMENT '环境评分（乘10）',
  `service_score`  INT          NOT NULL DEFAULT 0 COMMENT '服务评分（乘10）',
  `value_score`    INT          NOT NULL DEFAULT 0 COMMENT '性价比评分（乘10）',
  `open_hours`     VARCHAR(32)  DEFAULT NULL COMMENT '营业时间',
  `phone`          VARCHAR(20)  DEFAULT NULL COMMENT '电话（手机或座机，含区号）',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '营业状态：1 营业 0 休息',
  `create_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_merchant` (`merchant_id`),
  KEY `idx_shop_type` (`type_id`),
  KEY `idx_shop_geo` (`x`, `y`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- 店铺位置表
DROP TABLE IF EXISTS `tb_shop_location`;
CREATE TABLE `tb_shop_location` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id`          BIGINT       NOT NULL COMMENT '店铺 id',
  `x`                DOUBLE       DEFAULT NULL COMMENT '导航经度',
  `y`                DOUBLE       DEFAULT NULL COMMENT '导航纬度',
  `detail_address`   VARCHAR(255) DEFAULT NULL COMMENT '详细地址/门牌号',
  `business_circle`  VARCHAR(64)  DEFAULT NULL COMMENT '商圈',
  `delivery_range`   INT          DEFAULT NULL COMMENT '配送范围（米，外卖预留）',
  `create_time`      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_loc_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺位置表';

-- 店铺审核表（位置/照片等修改审核快照）
DROP TABLE IF EXISTS `tb_shop_audit`;
CREATE TABLE `tb_shop_audit` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id`     BIGINT       NOT NULL COMMENT '店铺 id',
  `audit_type`  TINYINT      NOT NULL COMMENT '审核类型：1 位置修改 2 照片修改 3 信息修改',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '审核状态（对齐统一状态机）',
  `reason`      VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
  `snapshot`    TEXT         COMMENT '修改内容快照（JSON）',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sa_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺审核表';

-- ---------------------------------------------------------------------
-- 4. 菜品域
-- ---------------------------------------------------------------------

-- 菜品分类表
DROP TABLE IF EXISTS `tb_dish_category`;
CREATE TABLE `tb_dish_category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id`     BIGINT      NOT NULL COMMENT '店铺 id',
  `name`        VARCHAR(32) NOT NULL COMMENT '分类名：热菜/凉菜/饮品/套餐',
  `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_dc_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- 菜品表
DROP TABLE IF EXISTS `tb_dish`;
CREATE TABLE `tb_dish` (
  `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id`        BIGINT         NOT NULL COMMENT '店铺 id',
  `category_id`    BIGINT         DEFAULT NULL COMMENT '菜品分类 id',
  `name`           VARCHAR(64)    NOT NULL COMMENT '名称',
  `image`          VARCHAR(255)   DEFAULT NULL COMMENT '图片',
  `price`          DECIMAL(10,2)  NOT NULL COMMENT '价格',
  `original_price` DECIMAL(10,2)  DEFAULT NULL COMMENT '原价',
  `description`    VARCHAR(255)   DEFAULT NULL COMMENT '描述',
  `stock`          INT            NOT NULL DEFAULT 0 COMMENT '库存',
  `status`         TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1 上架 0 下架 2 售罄 3 停售',
  `is_recommend`   TINYINT        NOT NULL DEFAULT 0 COMMENT '是否推荐',
  `sold`           INT            NOT NULL DEFAULT 0 COMMENT '销量',
  `create_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_dish_shop` (`shop_id`),
  KEY `idx_dish_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 菜品规格表（多规格）
DROP TABLE IF EXISTS `tb_dish_sku`;
CREATE TABLE `tb_dish_sku` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id`     BIGINT        NOT NULL COMMENT '菜品 id',
  `spec_name`   VARCHAR(32)   DEFAULT NULL COMMENT '规格名：大份/小份/辣度',
  `spec_value`  VARCHAR(32)   DEFAULT NULL COMMENT '规格值',
  `price`       DECIMAL(10,2) DEFAULT NULL COMMENT '规格价',
  `stock`       INT           NOT NULL DEFAULT 0 COMMENT '库存',
  `create_time` TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sku_dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品规格表';

-- 菜品审核表（新增/修改审核快照）
DROP TABLE IF EXISTS `tb_dish_audit`;
CREATE TABLE `tb_dish_audit` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id`     BIGINT       NOT NULL COMMENT '菜品 id',
  `audit_type`  TINYINT      NOT NULL COMMENT '审核类型：1 新增 2 修改',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '审核状态（对齐统一状态机）',
  `reason`      VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
  `snapshot`    TEXT         COMMENT '修改内容快照（JSON）',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_da_dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品审核表';

-- ---------------------------------------------------------------------
-- 5. 团购域
-- ---------------------------------------------------------------------

-- 团购表
DROP TABLE IF EXISTS `tb_groupbuy`;
CREATE TABLE `tb_groupbuy` (
  `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id`        BIGINT         NOT NULL COMMENT '店铺 id',
  `name`           VARCHAR(128)   NOT NULL COMMENT '团购名',
  `image`          VARCHAR(255)   DEFAULT NULL COMMENT '图片',
  `original_price` DECIMAL(10,2)  DEFAULT NULL COMMENT '原价',
  `groupbuy_price` DECIMAL(10,2)  NOT NULL COMMENT '团购价',
  `content`        TEXT           COMMENT '内容明细',
  `type`           TINYINT        NOT NULL DEFAULT 1 COMMENT '类型：1 套餐 2 代金券 3 折扣券 4 次卡 5 多人餐',
  `stock`          INT            NOT NULL DEFAULT 0 COMMENT '库存',
  `sold`           INT            NOT NULL DEFAULT 0 COMMENT '销量',
  `status`         TINYINT        NOT NULL DEFAULT 2 COMMENT '状态：1 上架 0 下架 2 审核中',
  `verify_type`    TINYINT        NOT NULL DEFAULT 3 COMMENT '核销方式：1 扫码 2 输入核销码 3 两者均可',
  `create_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_gb_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购表';

-- 团购规则表
DROP TABLE IF EXISTS `tb_groupbuy_rule`;
CREATE TABLE `tb_groupbuy_rule` (
  `id`              BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `groupbuy_id`     BIGINT     NOT NULL COMMENT '团购 id',
  `holiday_usable`  TINYINT    NOT NULL DEFAULT 1 COMMENT '节假日是否可用：1 可用 0 不可用',
  `per_user_limit`  INT        NOT NULL DEFAULT 0 COMMENT '每人限购（0 不限）',
  `per_table_limit` INT        NOT NULL DEFAULT 1 COMMENT '每桌限用',
  `valid_start`     TIMESTAMP  NULL DEFAULT NULL COMMENT '有效期开始',
  `valid_end`       TIMESTAMP  NULL DEFAULT NULL COMMENT '有效期结束',
  `refund_rule`     TINYINT    NOT NULL DEFAULT 1 COMMENT '退款规则：1 随时退 2 过期退 3 不可退',
  `create_time`     TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`         TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_rule_groupbuy` (`groupbuy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购规则表';

-- 团购订单表
DROP TABLE IF EXISTS `tb_groupbuy_order`;
CREATE TABLE `tb_groupbuy_order` (
  `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`     VARCHAR(64)    NOT NULL COMMENT '订单号',
  `user_id`      BIGINT         NOT NULL COMMENT '用户 id',
  `groupbuy_id`  BIGINT         NOT NULL COMMENT '团购 id',
  `shop_id`      BIGINT         NOT NULL COMMENT '店铺 id',
  `quantity`     INT            NOT NULL DEFAULT 1 COMMENT '数量',
  `amount`       DECIMAL(10,2)  NOT NULL COMMENT '金额',
  `status`       TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1 待支付 2 待使用 3 已使用 4 已退款 5 已过期',
  `verify_code`  VARCHAR(64)    DEFAULT NULL COMMENT '核销码',
  `create_time`  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_o_user` (`user_id`),
  KEY `idx_o_groupbuy` (`groupbuy_id`),
  KEY `idx_o_shop` (`shop_id`),
  KEY `idx_o_verify_code` (`verify_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购订单表';

-- 核销记录表
DROP TABLE IF EXISTS `tb_verification_record`;
CREATE TABLE `tb_verification_record` (
  `id`          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id`    BIGINT     NOT NULL COMMENT '订单 id',
  `operator_id` BIGINT     DEFAULT NULL COMMENT '操作员 id（商户员工）',
  `verify_time` TIMESTAMP  NULL DEFAULT NULL COMMENT '核销时间',
  `method`      TINYINT    NOT NULL DEFAULT 1 COMMENT '核销方式：1 扫码 2 输入核销码',
  `create_time` TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_vr_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核销记录表';

-- ---------------------------------------------------------------------
-- 6. 评价域
-- ---------------------------------------------------------------------

-- 评价表（order_id 为消费凭证，评分乘 10 存整数）
DROP TABLE IF EXISTS `tb_review`;
CREATE TABLE `tb_review` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT       NOT NULL COMMENT '用户 id',
  `shop_id`        BIGINT       NOT NULL COMMENT '店铺 id',
  `order_id`       BIGINT       DEFAULT NULL COMMENT '关联订单（消费凭证）',
  `score`          INT          NOT NULL COMMENT '总分（乘10）',
  `taste_score`    INT          DEFAULT NULL COMMENT '口味（乘10）',
  `env_score`      INT          DEFAULT NULL COMMENT '环境（乘10）',
  `service_score`  INT          DEFAULT NULL COMMENT '服务（乘10）',
  `value_score`    INT          DEFAULT NULL COMMENT '性价比（乘10）',
  `content`        TEXT         COMMENT '文字评价',
  `tags`           VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  `is_anonymous`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否匿名',
  `is_append`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否追评',
  `parent_id`      BIGINT       DEFAULT NULL COMMENT '追评的原始评价 id',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常 0 隐藏 2 删除',
  `like_count`     INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_r_shop` (`shop_id`),
  KEY `idx_r_user` (`user_id`),
  KEY `idx_r_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 评价图片表
DROP TABLE IF EXISTS `tb_review_image`;
CREATE TABLE `tb_review_image` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `review_id`   BIGINT       NOT NULL COMMENT '评价 id',
  `url`         VARCHAR(255) NOT NULL COMMENT '图片/视频 URL',
  `type`        TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：1 图片 2 视频',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_ri_review` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价图片表';

-- 评价回复表（商家回复/用户回复）
DROP TABLE IF EXISTS `tb_review_reply`;
CREATE TABLE `tb_review_reply` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `review_id`   BIGINT       NOT NULL COMMENT '评价 id',
  `user_id`     BIGINT       NOT NULL COMMENT '回复人 id（商户账号或用户）',
  `content`     VARCHAR(255) NOT NULL COMMENT '内容',
  `type`        TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：1 商家回复 2 用户回复',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_rr_review` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价回复表';

-- 评价点赞表
DROP TABLE IF EXISTS `tb_review_like`;
CREATE TABLE `tb_review_like` (
  `id`          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `review_id`   BIGINT     NOT NULL COMMENT '评价 id',
  `user_id`     BIGINT     NOT NULL COMMENT '用户 id',
  `create_time` TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_like` (`review_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价点赞表';

-- 评价举报表
DROP TABLE IF EXISTS `tb_review_report`;
CREATE TABLE `tb_review_report` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `review_id`   BIGINT       NOT NULL COMMENT '评价 id',
  `user_id`     BIGINT       NOT NULL COMMENT '举报人 id',
  `reason`      VARCHAR(255) DEFAULT NULL COMMENT '举报原因',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '处理状态：0 待处理 1 已处理',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_rrp_review` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价举报表';

-- ---------------------------------------------------------------------
-- 7. 互动域
-- ---------------------------------------------------------------------

-- 评论表（评价/问答等对象的通用评论）
DROP TABLE IF EXISTS `tb_comment`;
CREATE TABLE `tb_comment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户 id',
  `target_type` TINYINT      NOT NULL COMMENT '评论对象：1 评价 2 问答',
  `target_id`   BIGINT       NOT NULL COMMENT '对象 id',
  `content`     VARCHAR(255) NOT NULL COMMENT '内容',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常 0 删除 2 隐藏',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_c_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 点赞表（通用对象点赞）
DROP TABLE IF EXISTS `tb_like`;
CREATE TABLE `tb_like` (
  `id`          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT     NOT NULL COMMENT '用户 id',
  `target_type` TINYINT    NOT NULL COMMENT '对象类型：1 评价 2 帖子 3 评论',
  `target_id`   BIGINT     NOT NULL COMMENT '对象 id',
  `create_time` TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 收藏表
DROP TABLE IF EXISTS `tb_collect`;
CREATE TABLE `tb_collect` (
  `id`          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT     NOT NULL COMMENT '用户 id',
  `target_type` TINYINT    NOT NULL COMMENT '对象类型：1 商家 2 菜品 3 团购 4 帖子',
  `target_id`   BIGINT     NOT NULL COMMENT '对象 id',
  `create_time` TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collect` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 关注表
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow` (
  `id`             BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT     NOT NULL COMMENT '关注者 id',
  `follow_user_id` BIGINT     NOT NULL COMMENT '被关注者 id',
  `create_time`    TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`user_id`, `follow_user_id`),
  KEY `idx_follow_target` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 消息表（站内信）
DROP TABLE IF EXISTS `tb_message`;
CREATE TABLE `tb_message` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '接收用户 id',
  `type`        TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：1 系统 2 商家回复 3 评论 4 订阅',
  `content`     VARCHAR(255) DEFAULT NULL COMMENT '内容',
  `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_msg_user` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ---------------------------------------------------------------------
-- 8. 社区域（发帖：探店笔记 + 自由动态，P1）
-- ---------------------------------------------------------------------

-- 帖子表（shop_id 可空：探店笔记必填，自由动态为 NULL）
DROP TABLE IF EXISTS `tb_blog`;
CREATE TABLE `tb_blog` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT        NOT NULL COMMENT '作者 id',
  `shop_id`     BIGINT        DEFAULT NULL COMMENT '关联店铺（探店笔记必填）',
  `type`        TINYINT       NOT NULL DEFAULT 1 COMMENT '类型：1 探店笔记 2 自由动态',
  `title`       VARCHAR(128)  DEFAULT NULL COMMENT '标题（可空）',
  `content`     TEXT          COMMENT '正文',
  `images`      VARCHAR(2048) DEFAULT NULL COMMENT '图片/视频 URL，逗号分隔',
  `location`    VARCHAR(64)   DEFAULT NULL COMMENT '定位（城市/商圈）',
  `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 隐藏（敏感词/审核） 2 删除',
  `liked`       INT           NOT NULL DEFAULT 0 COMMENT '点赞数（冗余）',
  `comments`    INT           NOT NULL DEFAULT 0 COMMENT '评论数（冗余）',
  `collected`   INT           NOT NULL DEFAULT 0 COMMENT '收藏数（冗余）',
  `create_time` TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_blog_user` (`user_id`),
  KEY `idx_blog_shop` (`shop_id`),
  KEY `idx_blog_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- 帖子评论表（楼中楼）
DROP TABLE IF EXISTS `tb_blog_comment`;
CREATE TABLE `tb_blog_comment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `blog_id`     BIGINT       NOT NULL COMMENT '帖子 id',
  `user_id`     BIGINT       NOT NULL COMMENT '评论人 id',
  `parent_id`   BIGINT       DEFAULT NULL COMMENT '顶级评论 id（楼中楼，顶评为 NULL）',
  `answer_id`   BIGINT       DEFAULT NULL COMMENT '回复目标用户 id（@ 某人）',
  `content`     VARCHAR(255) NOT NULL COMMENT '内容',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 删除 2 隐藏',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_bc_blog` (`blog_id`),
  KEY `idx_bc_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论表';

-- 帖子点赞表
DROP TABLE IF EXISTS `tb_blog_like`;
CREATE TABLE `tb_blog_like` (
  `id`          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `blog_id`     BIGINT     NOT NULL COMMENT '帖子 id',
  `user_id`     BIGINT     NOT NULL COMMENT '用户 id',
  `create_time` TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blog_like` (`blog_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

-- ---------------------------------------------------------------------
-- 9. 审核域（统一审核中心，对齐《审核状态机详细设计.md》）
-- ---------------------------------------------------------------------

-- 审核任务表
DROP TABLE IF EXISTS `tb_audit_task`;
CREATE TABLE `tb_audit_task` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type`      TINYINT      NOT NULL COMMENT '业务类型：1 商户 2 店铺 3 菜品 4 团购 5 评价 6 图片 7 帖子',
  `biz_id`        BIGINT       NOT NULL COMMENT '业务对象 id',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0 草稿 1 待审核 2 审核中 3 通过 4 驳回 5 上架 6 下架 7 封禁',
  `submitter_id`  BIGINT       DEFAULT NULL COMMENT '提交人 id',
  `auditor_id`    BIGINT       DEFAULT NULL COMMENT '审核人 id（接单后写入）',
  `result`        TINYINT      DEFAULT NULL COMMENT '结果：1 通过 2 驳回 3 批量通过',
  `reason`        VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
  `create_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `audit_time`    TIMESTAMP    NULL DEFAULT NULL COMMENT '审核时间',
  `deleted`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_at_biz` (`biz_type`, `biz_id`),
  KEY `idx_at_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一审核任务表';

-- 审核记录表（五要素留痕）
DROP TABLE IF EXISTS `tb_audit_record`;
CREATE TABLE `tb_audit_record` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id`     BIGINT       NOT NULL COMMENT '审核任务 id',
  `auditor_id`  BIGINT       DEFAULT NULL COMMENT '审核人 id（系统自动操作则为 NULL）',
  `action`      TINYINT      NOT NULL COMMENT '动作：1 提交 2 接单 3 通过 4 驳回 5 上架 6 下架 7 封禁 8 释放',
  `comment`     VARCHAR(255) DEFAULT NULL COMMENT '意见',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_ar_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- 举报表
DROP TABLE IF EXISTS `tb_report`;
CREATE TABLE `tb_report` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `reporter_id` BIGINT       NOT NULL COMMENT '举报人 id',
  `target_type` TINYINT      NOT NULL COMMENT '举报对象：1 用户 2 评价 3 商家 4 帖子',
  `target_id`   BIGINT       NOT NULL COMMENT '对象 id',
  `reason`      VARCHAR(255) DEFAULT NULL COMMENT '举报原因',
  `result`      TINYINT      DEFAULT NULL COMMENT '处理结果：1 驳回 2 删除内容 3 警告 4 封禁',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_rp_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 敏感词表
DROP TABLE IF EXISTS `tb_sensitive_word`;
CREATE TABLE `tb_sensitive_word` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `word`        VARCHAR(64) NOT NULL COMMENT '敏感词',
  `level`       TINYINT     NOT NULL DEFAULT 1 COMMENT '等级：1 低危（打码） 2 中危（人工复审） 3 高危（拦截）',
  `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- ---------------------------------------------------------------------
-- 10. 交易域
-- ---------------------------------------------------------------------

-- 支付表
DROP TABLE IF EXISTS `tb_payment`;
CREATE TABLE `tb_payment` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id`    BIGINT         NOT NULL COMMENT '订单 id',
  `pay_no`      VARCHAR(64)    DEFAULT NULL COMMENT '支付流水号',
  `amount`      DECIMAL(10,2)  NOT NULL COMMENT '金额',
  `channel`     TINYINT        NOT NULL DEFAULT 1 COMMENT '支付渠道：1 微信支付',
  `status`      TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1 待支付 2 成功 3 失败',
  `pay_time`    TIMESTAMP      NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_pay_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付表';

-- 退款表
DROP TABLE IF EXISTS `tb_refund`;
CREATE TABLE `tb_refund` (
  `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id`     BIGINT         NOT NULL COMMENT '订单 id',
  `refund_no`    VARCHAR(64)    DEFAULT NULL COMMENT '退款单号',
  `amount`       DECIMAL(10,2)  NOT NULL COMMENT '退款金额',
  `reason`       VARCHAR(255)   DEFAULT NULL COMMENT '退款原因',
  `status`       TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1 申请 2 审核 3 退款中 4 已退 5 驳回',
  `apply_time`   TIMESTAMP      NULL DEFAULT NULL COMMENT '申请时间',
  `refund_time`  TIMESTAMP      NULL DEFAULT NULL COMMENT '退款完成时间',
  `create_time`  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`      TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_rf_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款表';

-- 结算表（商户结算 + 平台抽佣）
DROP TABLE IF EXISTS `tb_settlement`;
CREATE TABLE `tb_settlement` (
  `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id`   BIGINT         NOT NULL COMMENT '商户 id',
  `order_id`      BIGINT         DEFAULT NULL COMMENT '订单 id',
  `amount`        DECIMAL(10,2)  NOT NULL COMMENT '结算金额',
  `commission`    DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '平台抽佣',
  `status`        TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1 待结算 2 已结算 3 已提现',
  `settle_time`   TIMESTAMP      NULL DEFAULT NULL COMMENT '结算时间',
  `create_time`   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_st_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算表';

-- ---------------------------------------------------------------------
-- 11. 运营域
-- ---------------------------------------------------------------------

-- 分类表（一级/二级；店铺品类 type_id 亦引用）
DROP TABLE IF EXISTS `tb_category`;
CREATE TABLE `tb_category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(32)  NOT NULL COMMENT '分类名：美食/火锅/烧烤/奶茶/酒店',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类 id（0 表示一级）',
  `icon`        VARCHAR(255) DEFAULT NULL COMMENT '图标',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_cat_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 标签表（口味/环境/服务标签）
DROP TABLE IF EXISTS `tb_tag`;
CREATE TABLE `tb_tag` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(32) NOT NULL COMMENT '标签名：分量足/服务好/排队久',
  `type`        TINYINT     NOT NULL DEFAULT 1 COMMENT '类型：1 口味 2 环境 3 服务 4 通用',
  `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- Banner 表（运营位）
DROP TABLE IF EXISTS `tb_banner`;
CREATE TABLE `tb_banner` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `image`       VARCHAR(255) NOT NULL COMMENT '图片',
  `link_type`   TINYINT      DEFAULT NULL COMMENT '跳转类型：1 店铺 2 团购 3 专题 4 外链',
  `link_id`     BIGINT       DEFAULT NULL COMMENT '跳转对象 id（外链时为 NULL，link_url 存 ext）',
  `link_url`    VARCHAR(255) DEFAULT NULL COMMENT '外链地址',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用 0 停用',
  `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner 表';

-- 优惠券表
DROP TABLE IF EXISTS `tb_coupon`;
CREATE TABLE `tb_coupon` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id`     BIGINT         DEFAULT NULL COMMENT '店铺 id（平台券为 NULL）',
  `name`        VARCHAR(64)    DEFAULT NULL COMMENT '券名',
  `face_value`  DECIMAL(10,2)  NOT NULL COMMENT '面额',
  `threshold`   DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '使用门槛',
  `valid_start` TIMESTAMP      NULL DEFAULT NULL COMMENT '有效期开始',
  `valid_end`   TIMESTAMP      NULL DEFAULT NULL COMMENT '有效期结束',
  `total`       INT            NOT NULL DEFAULT 0 COMMENT '发放总量',
  `received`    INT            NOT NULL DEFAULT 0 COMMENT '已领取数',
  `status`      TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1 启用 0 停用',
  `create_time` TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_cp_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 活动表
DROP TABLE IF EXISTS `tb_activity`;
CREATE TABLE `tb_activity` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(64) NOT NULL COMMENT '活动名',
  `type`        TINYINT     NOT NULL DEFAULT 1 COMMENT '类型：1 专题 2 新客立减 3 报名',
  `start_time`  TIMESTAMP   NULL DEFAULT NULL COMMENT '开始时间',
  `end_time`    TIMESTAMP   NULL DEFAULT NULL COMMENT '结束时间',
  `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1 启用 0 停用',
  `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- =====================================================================
-- 演示种子数据
-- =====================================================================

-- 管理员（演示密码明文 123456，正式环境必须加密存储）
INSERT INTO `tb_admin` (`username`, `password`, `real_name`, `phone`, `status`) VALUES
('admin', '123456', '超管', '13800000000', 1);

-- 角色
INSERT INTO `tb_role` (`role_name`, `role_key`, `sort`) VALUES
('超级管理员', 'SUPER_ADMIN', 1),
('运营', 'OPERATOR', 2),
('审核员', 'AUDITOR', 3),
('财务', 'FINANCE', 4),
('客服', 'SUPPORT', 5);

-- 超管绑定超级管理员角色
INSERT INTO `tb_admin_role` (`admin_id`, `role_id`) VALUES (1, 1);

-- 平台分类
INSERT INTO `tb_category` (`name`, `parent_id`, `sort`) VALUES
('美食', 0, 1),
('火锅', 1, 1),
('烧烤', 1, 2),
('奶茶', 1, 3),
('酒店', 1, 4);

-- 评价标签
INSERT INTO `tb_tag` (`name`, `type`, `sort`) VALUES
('分量足', 1, 1),
('味道好', 1, 2),
('环境好', 2, 3),
('服务好', 3, 4),
('性价比高', 4, 5),
('排队久', 4, 6);

-- 敏感词（演示）
INSERT INTO `tb_sensitive_word` (`word`, `level`) VALUES
('加微信', 2),
('代购', 2),
('违禁品', 3);

-- 演示商户 + 员工账号 + 店铺
INSERT INTO `tb_merchant` (`name`, `license_no`, `legal_person`, `contact_phone`, `status`) VALUES
('杭州海悦餐饮管理有限公司', '91330100MA2DEMO001', '张三', '13900000001', 1);

INSERT INTO `tb_merchant_account` (`merchant_id`, `username`, `password`, `role`, `status`) VALUES
(1, 'mer001', '123456', 1, 1);

INSERT INTO `tb_shop` (`merchant_id`, `name`, `type_id`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `taste_score`, `env_score`, `service_score`, `value_score`, `open_hours`, `phone`, `status`) VALUES
(1, '海底捞火锅（水晶城店）', 2, '大关', '上塘路458号水晶城购物中心F6', 120.15, 30.31, 104, 5300, 3035, 49, 49, 48, 50, 47, '10:00-次日07:00', '057188888888', 1);

INSERT INTO `tb_shop_location` (`shop_id`, `x`, `y`, `detail_address`, `business_circle`) VALUES
(1, 120.15, 30.31, '上塘路458号水晶城购物中心F6', '大关');

-- 菜品分类 + 菜品
INSERT INTO `tb_dish_category` (`shop_id`, `name`, `sort`) VALUES
(1, '锅底', 1),
(1, '涮菜', 2);

INSERT INTO `tb_dish` (`shop_id`, `category_id`, `name`, `price`, `original_price`, `description`, `stock`, `status`, `is_recommend`, `sold`) VALUES
(1, 1, '经典麻辣锅底', 89.00, 99.00, '地道川味，麻辣鲜香', 999, 1, 1, 2100),
(1, 2, '捞派肥牛', 48.00, 56.00, '肥瘦相间，入口即化', 999, 1, 1, 3200);

-- 团购 + 规则
INSERT INTO `tb_groupbuy` (`shop_id`, `name`, `original_price`, `groupbuy_price`, `content`, `type`, `stock`, `sold`, `status`, `verify_type`) VALUES
(1, '双人套餐', 398.00, 238.00, '锅底任选 + 捞派肥牛 + 虾滑 + 蔬菜拼盘 + 饮料 2 杯', 1, 999, 1560, 1, 3);

INSERT INTO `tb_groupbuy_rule` (`groupbuy_id`, `holiday_usable`, `per_user_limit`, `per_table_limit`, `refund_rule`) VALUES
(1, 1, 3, 1, 1);
