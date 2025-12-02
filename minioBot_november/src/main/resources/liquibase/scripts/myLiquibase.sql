-- liquibase formated sql

-- changeset author:1
CREATE TABLE notifications
(
    id                SERIAL    NOT NULL PRIMARY KEY,
    chat_id           bigint    NOT NULL,
    position          text      NOT NULL,
    comment           text      NOT NULL,
    task_complete     varchar DEFAULT 'UNCOMPLETED',
    status            varchar DEFAULT 'SCHEDULED',
    sent_date timestamp NOT NULL
);

create table if not exists notes_images
(
    id                SERIAL PRIMARY KEY,
    notification_id   bigint,
    original_filename VARCHAR(255),
    content_type      VARCHAR(50),
    size              INT,
    path_file         VARCHAR(255),
    media_type        VARCHAR(255),
    bytes              BYTEA,
    constraint fk_tasks_images_tasks foreign key (notification_id) references notifications (id) on delete cascade on update no action
);

CREATE INDEX notifications_date_index ON notifications (sent_date) WHERE status = 'SCHEDULED';