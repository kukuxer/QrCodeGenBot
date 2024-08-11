CREATE TABLE IF NOT EXISTS tg_user
(
    id                           BIGSERIAL PRIMARY KEY NOT NULL,
    chat_id                      VARCHAR(32)           NOT NULL,
    telegram_user_id             VARCHAR(255)          NOT NULL,
    tg_username                  VARCHAR(255)          NOT NULL,
    role                         VARCHAR(32)           NOT NULL,
    message_id                   INT,
    additional_message_id        INT,
    step_of_generation_code      int,
    step_of_managing_codes       int,
    secret_code                  int,
    generate_qr_code_right_now   boolean,
    want_to_change_link          boolean,
    want_to_delete               boolean,
    want_to_check_visitors       boolean,
    is_on_final_step_of_creation boolean
);

CREATE TABLE IF NOT EXISTS qr_code
(
    uuid               UUID PRIMARY KEY NOT NULL,
    text               VARCHAR(255)     NOT NULL,
    full_link          VARCHAR(255)     NOT NULL,
    creator_id         INTEGER,
    CONSTRAINT fk_qr_code_tg_user FOREIGN KEY (creator_id) references tg_user (id),
    foregroundColor    VARCHAR(7),
    backgroundColor    VARCHAR(7),
    type               VARCHAR(55),
    creation_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiration_time    TIMESTAMP,
    is_active          BOOLEAN,
    is_created         BOOLEAN,
    qr_code_scan_count INT,
    qr_code_image      BYTEA,

    CONSTRAINT fk_qr_code_creator FOREIGN KEY (creator_id) REFERENCES tg_user (id)
);

CREATE TABLE IF NOT EXISTS qr_code_visitor
(
    id                 BIGSERIAL PRIMARY KEY NOT NULL,
    ip                 VARCHAR(255),
    country            VARCHAR(255),
    city               VARCHAR(255),
    visited_time       TIMESTAMP,
    visited_qr_code_id UUID,
    CONSTRAINT fk_qr_code_visitor_qr_code FOREIGN KEY (visited_qr_code_id) REFERENCES qr_code (uuid)
)