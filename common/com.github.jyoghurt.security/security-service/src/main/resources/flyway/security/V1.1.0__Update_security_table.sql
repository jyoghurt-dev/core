INSERT INTO `ScheduleJob` (`jobId`, `jobName`, `jobGroup`, `jobState`, `cronExpression`, `description`, `className`, `beanName`, `createDateTime`, `modifyDateTime`, `founderId`, `founderName`, `modifierId`, `modifierName`, `deleteFlag`, `rollBackTheWrong`) VALUES ('21', 'UserSchedule', 'UserSchedule', '0', '0 10 23 * * ?', '定时清理用户登录验证码', 'UserSchedule', '', NULL, NULL, NULL, NULL, NULL, NULL, '0', '0');