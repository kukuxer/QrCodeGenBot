CREATE TABLE IF NOT EXISTS tg_user
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    chat_id                  VARCHAR(32)  NOT NULL,
    telegram_user_id         VARCHAR(255) NOT NULL,
    tg_username              VARCHAR(255) NOT NULL,
    role                     VARCHAR(32)  NOT NULL,
    qr_code_id UUID,
    message_id               INT,
    generate_qr_code_right_now boolean,
    is_on_final_step_of_creation boolean,
    CONSTRAINT fk_tg_user_qr_code FOREIGN KEY (qr_code_id) REFERENCES qr_code (uuid)
);

CREATE TABLE IF NOT EXISTS qr_code
(
    uuid UUID PRIMARY KEY NOT NULL,
    text               VARCHAR(255) NOT NULL,
    full_link          VARCHAR(255) NOT NULL,
    creator_id         INTEGER,
    CONSTRAINT fk_qr_code_tg_user FOREIGN KEY (creator_id) references tg_user (id),
    foregroundColor    VARCHAR(7),
    backgroundColor    VARCHAR(7),
    creation_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiration_time    TIMESTAMP,
    is_active          BOOLEAN,
    is_created          BOOLEAN,
    qr_code_scan_count INT,
    qr_code_visitor_id INTEGER,
    CONSTRAINT fk_qr_code_qr_code_visitor FOREIGN KEY (qr_code_visitor_id) REFERENCES qr_code_visitor (id)
);

CREATE TABLE IF NOT EXISTS qr_code_visitor
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    ip      VARCHAR(255),
    country VARCHAR(255),
    city VARCHAR(255),
    visited_qr_code_id UUID,
    CONSTRAINT fk_qr_code_visitor_qr_code FOREIGN KEY (visited_qr_code_id) REFERENCES qr_code (uuid)
)