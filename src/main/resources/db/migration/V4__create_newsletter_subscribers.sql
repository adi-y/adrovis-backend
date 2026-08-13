CREATE TABLE newsletter_subscriber (
    id UUID NOT NULL,

    email VARCHAR(150) NOT NULL,

    status VARCHAR(20) NOT NULL,

    subscribed_at TIMESTAMP(6) NOT NULL,

    unsubscribed_at TIMESTAMP(6),

    created_at TIMESTAMP(6) NOT NULL,

    updated_at TIMESTAMP(6) NOT NULL,

    version BIGINT NOT NULL,

    CONSTRAINT pk_newsletter_subscriber
        PRIMARY KEY (id),

    CONSTRAINT uq_newsletter_subscriber_email
        UNIQUE (email),

    CONSTRAINT chk_newsletter_subscriber_status
        CHECK (status IN ('SUBSCRIBED', 'UNSUBSCRIBED'))
);

CREATE INDEX idx_newsletter_subscriber_status
    ON newsletter_subscriber(status);

CREATE INDEX idx_newsletter_subscriber_created_at
    ON newsletter_subscriber(created_at);