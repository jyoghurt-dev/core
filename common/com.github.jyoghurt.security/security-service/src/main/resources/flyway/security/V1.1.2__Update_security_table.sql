DELIMITER //
CREATE EVENT `EVENT_CREATE_MONTH_LOG_TABLE` ON SCHEDULE EVERY 1 MONTH STARTS '2017-05-15 00:00:30' ON COMPLETION NOT PRESERVE ENABLE DO
call PRC_CREATE_MONTH_LOG_TABLE()//
DELIMITER ;
set global event_scheduler = on;


