-- -- 表结构 --
-- CREATE TABLE IF NOT EXISTS `SecurityUserT` (
--   `userId` varchar(36) NOT NULL,
--   `extId` varchar(36) DEFAULT NULL,
--   `userName` varchar(30) DEFAULT NULL,
--   `slaveList` varchar(200) DEFAULT NULL,
--   `mobile` varchar(20) DEFAULT NULL,
--   `linkWay` varchar(20) DEFAULT NULL,
--   `emailAddr` varchar(50) DEFAULT NULL,
--   `userAccount` varchar(20) DEFAULT NULL,
--   `position` varchar(20) DEFAULT NULL,
--   `passwd` varchar(100) DEFAULT NULL,
--   `belongOrg` varchar(36) DEFAULT NULL,
--   `belongOrgName` varchar(20) DEFAULT NULL,
--   `type` varchar(2) DEFAULT '1',
--   `createDateTime` datetime DEFAULT NULL,
--   `modifyDateTime` datetime DEFAULT NULL,
--   `founderId` varchar(36) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建人id',
--   `founderName` varchar(20) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建人姓名',
--   `modifierId` varchar(36) DEFAULT NULL COMMENT '修改人ID',
--   `modifierName` varchar(20) DEFAULT NULL COMMENT '修改人姓名',
--   `openType` char(1) DEFAULT NULL,
--   PRIMARY KEY (`userId`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
--
-- ALTER TABLE `SecurityUserT`
-- ADD COLUMN `deleteFlag`  tinyint(1) NULL DEFAULT '0' COMMENT '删除标识';
--
-- CREATE TABLE IF NOT EXISTS `SecurityButtonT` (
--   `buttonId` varchar(36) NOT NULL,
--   `buttonName` varchar(20) DEFAULT NULL,
--   `menuId` varchar(36) DEFAULT NULL,
--   `buttonCode` varchar(20) DEFAULT NULL,
--   `createDateTime` datetime DEFAULT NULL,
--   `modifyDateTime` datetime DEFAULT NULL,
--   `operatorId` varchar(36) DEFAULT NULL,
--   `operatorName` varchar(20) DEFAULT NULL,
--   PRIMARY KEY (`buttonId`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
-- ALTER TABLE `SecurityButtonT`
-- ADD COLUMN `deleteFlag`  tinyint(1) NULL DEFAULT '0' COMMENT '删除标识';
--
-- CREATE TABLE IF NOT EXISTS `SecurityDataDic` (
--   `id` varchar(36) NOT NULL COMMENT '字典ID',
--   `dicName` varchar(20) DEFAULT NULL COMMENT '字典名称',
--   `key` varchar(20) DEFAULT NULL COMMENT 'key',
--   `value` varchar(20) DEFAULT NULL COMMENT 'value',
--   PRIMARY KEY (`id`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
--
--
-- CREATE TABLE IF NOT EXISTS `SecurityMenuT` (
--   `menuId` varchar(36) NOT NULL,
--   `parentId` varchar(36) DEFAULT NULL,
--   `menuName` varchar(20) DEFAULT NULL,
--   `menuUrl` varchar(100) DEFAULT NULL,
--   `isLeaf` varchar(1) DEFAULT NULL,
--   `createDateTime` datetime DEFAULT NULL,
--   `modifyDateTime` datetime DEFAULT NULL,
--   `icon` varchar(200) DEFAULT NULL,
--   `sortId` int(11) DEFAULT NULL,
--   `founderId` varchar(36) DEFAULT NULL,
--   `founderName` varchar(20) DEFAULT NULL,
--   `modifierId` varchar(36) DEFAULT NULL COMMENT '修改人ID',
--   `modifierName` varchar(20) DEFAULT NULL COMMENT '修改人姓名',
--   PRIMARY KEY (`menuId`),
--   KEY `FK_Reference_1` (`founderId`) USING BTREE,
--   CONSTRAINT `SecurityMenuT_ibfk_1` FOREIGN KEY (`founderId`) REFERENCES `SecurityUserT` (`userId`) ON DELETE SET NULL ON UPDATE CASCADE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
-- ALTER TABLE `SecurityMenuT`
-- ADD COLUMN `deleteFlag`  tinyint(1) NULL DEFAULT '0' COMMENT '删除标识';
--
-- CREATE TABLE IF NOT EXISTS `SecurityRoleT` (
--   `roleId` varchar(36) NOT NULL,
--   `roleName` varchar(20) DEFAULT NULL,
--   `roleType` varchar(2) DEFAULT NULL,
--   `belongUnit` varchar(36) DEFAULT NULL,
--   `createDateTime` datetime DEFAULT NULL,
--   `modifyDateTime` datetime DEFAULT NULL,
--   `founderId` varchar(36) DEFAULT NULL,
--   `founderName` varchar(20) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建人姓名',
--   `modifierId` varchar(36) DEFAULT NULL COMMENT '修改人ID',
--   `modifierName` varchar(20) DEFAULT NULL COMMENT '修改人姓名',
--   PRIMARY KEY (`roleId`),
--   KEY `FK_Reference_2` (`founderId`) USING BTREE,
--   CONSTRAINT `SecurityRoleT_ibfk_1` FOREIGN KEY (`founderId`) REFERENCES `SecurityUserT` (`userId`) ON DELETE SET NULL ON UPDATE CASCADE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
-- ALTER TABLE `SecurityRoleT`
-- ADD COLUMN `deleteFlag`  tinyint(1) NULL DEFAULT '0' COMMENT '删除标识';
--
--
-- CREATE TABLE IF NOT EXISTS `SecurityMenuRoleR` (
--   `relId` varchar(36) NOT NULL,
--   `roleId` varchar(36) DEFAULT NULL,
--   `menuId` varchar(36) DEFAULT NULL,
--   `createDateTime` datetime DEFAULT NULL,
--   `modifyDateTime` datetime DEFAULT NULL,
--   `founderId` varchar(36) DEFAULT NULL,
--   `founderName` varchar(20) DEFAULT NULL,
--   `modifierId` varchar(36) DEFAULT NULL COMMENT '修改人ID',
--   `modifierName` varchar(20) DEFAULT NULL COMMENT '修改人姓名',
--   PRIMARY KEY (`relId`),
--   KEY `FK_Reference_27` (`menuId`) USING BTREE,
--   KEY `FK_Reference_5` (`roleId`) USING BTREE,
--   KEY `FK_Reference_9` (`founderId`) USING BTREE,
--   CONSTRAINT `SecurityMenuRoleR_ibfk_1` FOREIGN KEY (`menuId`) REFERENCES `SecurityMenuT` (`menuId`) ON DELETE CASCADE ON UPDATE CASCADE,
--   CONSTRAINT `SecurityMenuRoleR_ibfk_2` FOREIGN KEY (`roleId`) REFERENCES `SecurityRoleT` (`roleId`) ON DELETE CASCADE ON UPDATE CASCADE,
--   CONSTRAINT `SecurityMenuRoleR_ibfk_3` FOREIGN KEY (`founderId`) REFERENCES `SecurityUserT` (`userId`) ON DELETE SET NULL ON UPDATE CASCADE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
--
-- ALTER TABLE `SecurityMenuRoleR`
-- ADD COLUMN `deleteFlag`  tinyint(1) NULL DEFAULT '0' COMMENT '删除标识';
--
--
-- CREATE TABLE IF NOT EXISTS `SecurityRoleButtonR` (
--   `mrbId` varchar(36) NOT NULL,
--   `roleId` varchar(36) DEFAULT NULL,
--   `buttonId` varchar(36) DEFAULT NULL,
--   `createDateTime` datetime DEFAULT NULL,
--   `modifyDateTime` datetime DEFAULT NULL,
--   `founderId` varchar(36) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建人id',
--   `founderName` varchar(20) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建人姓名',
--   `modifierId` varchar(36) DEFAULT NULL COMMENT '修改人ID',
--   `modifierName` varchar(20) DEFAULT NULL COMMENT '修改人姓名',
--   PRIMARY KEY (`mrbId`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
-- ALTER TABLE `SecurityRoleButtonR`
-- ADD COLUMN `deleteFlag`  tinyint(1) NULL DEFAULT '0' COMMENT '删除标识';
--
--
-- CREATE TABLE IF NOT EXISTS `SecuritySyslogT` (
--   `logId` varchar(36) NOT NULL COMMENT '日志ID',
--   `ipAddress` varchar(128) DEFAULT NULL COMMENT 'IP地址',
--   `moduleName` varchar(20) DEFAULT NULL COMMENT '模块名称',
--   `state` int(1) DEFAULT NULL COMMENT '状态',
--   `logMessage` varchar(100) DEFAULT NULL COMMENT '日志内容',
--   `errorMessage` varchar(10000) DEFAULT NULL COMMENT '异常信息',
--   `classMethodName` varchar(500) DEFAULT NULL COMMENT '类方法名',
--   `methodParameterValues` longtext COMMENT '参数列表',
--   `systemType` varchar(20) DEFAULT NULL COMMENT '系统类型',
--   `clientType` varchar(20) DEFAULT NULL COMMENT '客户端类型',
--   `invokeDuration` bigint(20) DEFAULT NULL COMMENT '调用时长',
--   `createDateTime` datetime DEFAULT NULL COMMENT '操作时间',
--   `modifyDateTime` datetime DEFAULT NULL,
--   `founderId` varchar(36) DEFAULT NULL COMMENT '创建人id',
--   `founderName` varchar(20) DEFAULT NULL COMMENT '创建人姓名',
--   `modifierId` varchar(36) DEFAULT NULL COMMENT '修改人ID',
--   `modifierName` varchar(20) DEFAULT NULL COMMENT '修改人姓名',
--   `abnormalLogContent` varchar(100) DEFAULT NULL COMMENT '违法访问判断标识',
--   PRIMARY KEY (`logId`),
--   KEY `FK_Reference_8` (`founderId`) USING BTREE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
--
-- CREATE TABLE IF NOT EXISTS `SecurityUnitT` (
--   `unitId` varchar(36) NOT NULL COMMENT '组织机构ID',
--   `parentId` varchar(36) NOT NULL COMMENT '上级单位',
--   `unitName` varchar(20) DEFAULT NULL COMMENT '组织机构名称',
--   `type` varchar(1) DEFAULT NULL COMMENT '组织机构类型 0-4s店 1-保险代理公司 2-保险公估公司',
--   `compType` varchar(1) DEFAULT '1' COMMENT '类型 0-公司 1-部门',
--   `appId` varchar(50) DEFAULT NULL COMMENT '订阅号appId',
--   `secretKey` varchar(50) DEFAULT NULL COMMENT '订阅号secretKey',
--   `appIdAccount` varchar(50) DEFAULT NULL COMMENT '订阅号账号',
--   `appIdPwd` varchar(50) DEFAULT NULL COMMENT '订阅号密码',
--   `appIdF` varchar(50) DEFAULT NULL COMMENT '服务号appId',
--   `secretKeyF` varchar(50) DEFAULT NULL COMMENT '服务号secretKeyF',
--   `appIdFAccount` varchar(50) DEFAULT NULL COMMENT '服务号账号',
--   `appIdFPwd` varchar(50) DEFAULT NULL COMMENT '服务号密码',
--   `appIdQ` varchar(50) DEFAULT NULL COMMENT '企业号appIdQ',
--   `secretKeyQ` varchar(50) DEFAULT NULL COMMENT '服务号secretKeyQ',
--   `appIdQAccount` varchar(50) DEFAULT NULL COMMENT '企业号账号',
--   `appIdQPwd` varchar(50) DEFAULT NULL COMMENT '企业号密码',
--   `clientId` varchar(30) DEFAULT NULL COMMENT '腾讯企业邮箱账户',
--   `clientSecret` varchar(40) DEFAULT NULL COMMENT '腾讯企业邮箱密钥',
--   `bsflag` varchar(1) DEFAULT '0' COMMENT '是否删除 0-否，1-是',
--   `acol1ColAssessor` varchar(20) DEFAULT NULL COMMENT '公估属性1',
--   `acol2ColAssessor` varchar(20) DEFAULT NULL COMMENT '公估属性2',
--   `pcol1ColProxy` varchar(20) DEFAULT NULL COMMENT '代理属性1',
--   `pcol2ColProxy` varchar(20) DEFAULT NULL COMMENT '代理属性2',
--   `col1Col4s` varchar(20) DEFAULT NULL COMMENT '业务所在地KEY',
--   `col2Col4s` varchar(20) DEFAULT NULL COMMENT '业务所在地VALUE',
--   `createDateTime` datetime DEFAULT NULL,
--   `modifyDateTime` datetime DEFAULT NULL,
--   `founderId` varchar(36) DEFAULT NULL COMMENT '创建人id',
--   `founderName` varchar(20) DEFAULT NULL COMMENT '创建人姓名',
--   `modifierId` varchar(36) DEFAULT NULL COMMENT '修改人ID',
--   `modifierName` varchar(20) DEFAULT NULL COMMENT '修改人姓名',
--   `sortId` int(3) DEFAULT NULL,
--   PRIMARY KEY (`unitId`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
--
-- ALTER TABLE `SecurityUnitT`
-- ADD COLUMN `deleteFlag`  tinyint(1) NULL DEFAULT '0' COMMENT '删除标识';
--
-- CREATE TABLE IF NOT EXISTS `SecurityUserResourceR` (
--   `userResourceRId` varchar(36) NOT NULL COMMENT '用户资源关系ID',
--   `userId` varchar(36) DEFAULT NULL COMMENT '用户ID',
--   `belongModel` varchar(20) DEFAULT NULL COMMENT '所属模块',
--   `resourceType` varchar(36) DEFAULT NULL COMMENT '资源类型',
--   `resourceId` varchar(36) DEFAULT NULL COMMENT '资源ID',
--   `resourceName` varchar(20) DEFAULT NULL COMMENT '资源名称',
--   `founderId` varchar(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
--   `founderName` varchar(30) NOT NULL DEFAULT '' COMMENT '创建人姓名',
--   `modifierId` varchar(36) NOT NULL DEFAULT '' COMMENT '修改人ID',
--   `modifierName` varchar(30) NOT NULL DEFAULT '' COMMENT '修改人姓名',
--   `createDateTime` datetime NOT NULL DEFAULT '1000-01-01 00:00:00' COMMENT '创建时间',
--   `modifyDateTime` datetime NOT NULL DEFAULT '1000-01-01 00:00:00' COMMENT '修改时间',
--   PRIMARY KEY (`userResourceRId`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户资源关系表';
--
-- ALTER TABLE `SecurityUserResourceR`
-- ADD COLUMN `deleteFlag`  tinyint(1) NULL DEFAULT '0' COMMENT '删除标识';
--
-- CREATE TABLE IF NOT EXISTS `SecurityUserRoleR` (
--   `relId` varchar(36) NOT NULL,
--   `roleId` varchar(36) DEFAULT NULL,
--   `userId` varchar(36) DEFAULT NULL,
--   `createDateTime` datetime DEFAULT NULL,
--   `modifyDateTime` datetime DEFAULT NULL,
--   `founderId` varchar(36) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建人id',
--   `founderName` varchar(20) CHARACTER SET utf8 DEFAULT NULL COMMENT '创建人姓名',
--   `modifierId` varchar(36) DEFAULT NULL COMMENT '修改人ID',
--   `modifierName` varchar(20) DEFAULT NULL COMMENT '修改人姓名',
--   PRIMARY KEY (`relId`),
--   KEY `FK_Reference_10` (`roleId`) USING BTREE,
--   KEY `FK_Reference_11` (`userId`) USING BTREE,
--   CONSTRAINT `SecurityUserRoleR_ibfk_1` FOREIGN KEY (`roleId`) REFERENCES `SecurityRoleT` (`roleId`) ON DELETE CASCADE ON UPDATE CASCADE,
--   CONSTRAINT `SecurityUserRoleR_ibfk_2` FOREIGN KEY (`userId`) REFERENCES `SecurityUserT` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
-- ALTER TABLE `SecurityUserRoleR`
-- ADD COLUMN `deleteFlag`  tinyint(1) NULL DEFAULT '0' COMMENT '删除标识';
--
--
-- -- UPDATE SecurityMenuT SET  `menuUrl`='/workflow/AdminWorkFlowSearchTable'
-- where menuName='流程查询' ;
-- -- UPDATE SecurityMenuT SET  `menuUrl`='/workflow/AdminWorkFlowToDoTable?procDefKey=Administration'
-- where menuName='待办任务' ;
-- -- UPDATE SecurityMenuT SET  `menuUrl`='/AdministrationFlow/AdministrationFlow?insuredStatus=newInsurance'
-- where menuName='保单录入' ;
--
-- DELIMITER //
--
-- drop function `getChildCompanyPersonList` //
-- CREATE  FUNCTION `getChildCompanyPersonList`(rootId VARCHAR(50)) RETURNS longtext CHARSET utf8
-- BEGIN
-- 	DECLARE
-- 		pTemp longtext ;
-- DECLARE
-- 	cTemp longtext ;
--
-- SET pTemp = '$';
-- SET cTemp = rootId;
-- WHILE cTemp IS NOT NULL DO
--
-- SET pTemp = concat(pTemp, ',', cTemp);
-- SELECT
-- 	group_concat(uu.unitId) INTO cTemp
-- FROM
-- 	(
-- 		SELECT
-- 			unit.unitId,
-- 			unit.unitName,
-- 			unit.parentId,
-- 			unit.compType
-- 		FROM
-- 			SecurityUnitT unit
-- 		WHERE
-- 			unit.bsflag = '0'
-- 		UNION
-- 			SELECT
-- 				u.userId AS 'unitId',
-- 				u.userName AS 'unitName',
-- 				u.belongOrg AS 'parentId',
-- 				'3' AS 'compType'
-- 			FROM
-- 				SecurityUserT u
-- 	) uu
-- WHERE
-- 	FIND_IN_SET(uu.parentId, cTemp) > 0;
-- END
-- WHILE;
-- RETURN pTemp;
--
-- END//
-- DELIMITER ;




