-- Nettoyage fiable avant re-création par Hibernate (évite les conflits FK avec JOINED inheritance + DevTools)
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS route_delivery_points;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS system_configs;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS routes;
DROP TABLE IF EXISTS delivery_points;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS dispatchers;
DROP TABLE IF EXISTS drivers;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;
