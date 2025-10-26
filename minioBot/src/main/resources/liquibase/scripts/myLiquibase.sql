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
    notification_date timestamp NOT NULL
);

create table if not exists notes_images
(
    id                SERIAL PRIMARY KEY,
    original_filename VARCHAR(255),
    content_type      VARCHAR(50),
    size              INT,
    data              BYTEA,
    constraint fk_tasks_images_tasks foreign key (id) references notifications (id) on delete cascade on update no action
);

CREATE INDEX notifications_date_index ON notifications (notification_date) WHERE status = 'SCHEDULED';