-- liquibase formatted sql

-- changeset author:1
CREATE TABLE notifications
(
    id            SERIAL    NOT NULL PRIMARY KEY,
    chat_id       bigint    NOT NULL,
    position      text,
    comment       text      NOT NULL,
    task_complete smallint check (task_complete between 0 and 3),
    sent_date     timestamp NOT NULL
);

-- changeset author:2
CREATE TABLE IF NOT EXISTS notes_images
(
    id                uuid NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    notification_id   bigint,
    original_filename VARCHAR(255),
    size              BIGINT,
    path_file         VARCHAR(255),
    media_type        VARCHAR(255),
    bytes             BYTEA,
    CONSTRAINT fk_tasks_images_tasks FOREIGN KEY (notification_id) REFERENCES notifications (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

-- changeset author:4
-- подключить расширение (если ещё не подключено)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- changeset author:5
CREATE TABLE IF NOT EXISTS users
(
    id          SERIAL NOT NULL PRIMARY KEY,
    chat_id     bigint,
    first_name  VARCHAR(255),
    last_name   VARCHAR(255)
);

-- changeset author:6
INSERT INTO users(first_name, last_name)
VALUES ('С.В.', 'Шибанов'),
       ('С.А.', 'Новоселов'),
       ('В.В.', 'Лорик'),
       ('С.В.', 'Золотухин'),
       ('Н.А.', 'Бегов'),
       ('А.Н.', 'Вахитов'),
       ('Р.И.', 'Исламгалеев'),
       ('А.М.', 'Агаев'),
       ('А.А.', 'Большаков'),
       ('П.А.', 'Устимов'),
       ('Р.О.', 'Шаповалов'),
       ('И.Г.', 'Хисматулин'),
       ('А.А.', 'Шарипов'),
       ('М.В.', 'Семерня'),
       ('А.С.', 'Омельченко'),
       ('Р.Р.', 'Катеев'),
       ('В.А.', 'Васалатьев'),
       ('В.А.', 'Ушаров'),
       ('Д.М.', 'Замесин');

-- changeset author:7
CREATE TABLE executers
(
    id              SERIAL NOT NULL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    executer_id        BIGINT ,
    CONSTRAINT fk_tasks_executers FOREIGN KEY (notification_id) REFERENCES notifications (id) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_tasks_executers_user FOREIGN KEY (executer_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

-- changeset author:8
-- CREATE INDEX notifications_date_index ON notifications (sent_date) WHERE status = 'SCHEDULED';