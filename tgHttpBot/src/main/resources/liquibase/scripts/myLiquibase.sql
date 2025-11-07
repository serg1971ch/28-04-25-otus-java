-- liquibase formated sql
CREATE DATABASE telegramDB;
-- changeset author:1
CREATE TABLE notifications (
                               id SERIAL NOT NULL PRIMARY KEY,
                               chat_id bigint NOT NULL,
                               title text NOT NULL,
                               position text NOT NULL,
                               comment text NOT NULL,
                               status varchar DEFAULT 'SCHEDULED',
                               notification_date timestamp NOT NULL
);

CREATE INDEX notifications_date_index ON notifications (notification_date) WHERE status = 'SCHEDULED';