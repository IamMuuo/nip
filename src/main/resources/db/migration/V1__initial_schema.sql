-- accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id          uuid                        NOT NULL,
    created_at  timestamp(6) with time zone NOT NULL,
    updated_at  timestamp(6) with time zone NOT NULL,
    active      boolean                     NOT NULL,
    description character varying(255),
    name        character varying(255)      NOT NULL,
    CONSTRAINT accounts_pkey PRIMARY KEY (id)
);


-- api_keys table
CREATE TABLE IF NOT EXISTS api_keys (
    id           uuid                        NOT NULL,
    created_at   timestamp(6) with time zone NOT NULL,
    updated_at   timestamp(6) with time zone NOT NULL,
    expires_at   timestamp(6) with time zone,
    key_hash     character varying(64)       NOT NULL,
    label        character varying(255),
    last_used_at timestamp(6) with time zone,
    account_id   uuid,
    is_active    boolean,
    CONSTRAINT api_keys_pkey PRIMARY KEY (id),
    CONSTRAINT ukcwrf6urvb9lnvuli1giq8t2k9 UNIQUE (key_hash),
    CONSTRAINT fk9n9egl89f981quvspbpev6b9c FOREIGN KEY (account_id) REFERENCES accounts (id)
);
