CREATE TABLE public.payment_transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    application_id UUID NOT NULL UNIQUE,

    reference_id VARCHAR(40) NOT NULL UNIQUE,

    razorpay_payment_link_id VARCHAR(100) NOT NULL UNIQUE,

    razorpay_order_id VARCHAR(100),

    razorpay_payment_id VARCHAR(100),

    amount BIGINT NOT NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    status VARCHAR(30) NOT NULL,

    payment_link_url TEXT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    paid_at TIMESTAMP WITH TIME ZONE,

    expired_at TIMESTAMP WITH TIME ZONE,

    cancelled_at TIMESTAMP WITH TIME ZONE,

    failed_attempts INTEGER NOT NULL DEFAULT 0,

    last_failed_at TIMESTAMP WITH TIME ZONE,

    last_failure_reason TEXT,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_payment_application
        FOREIGN KEY (application_id)
        REFERENCES public.application(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_payment_amount
        CHECK (amount > 0),

    CONSTRAINT chk_payment_status
        CHECK (
            status IN (
                'CREATED',
                'ISSUED',
                'PAID',
                'EXPIRED',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_payment_transaction_application
    ON public.payment_transaction(application_id);

CREATE INDEX idx_payment_transaction_status
    ON public.payment_transaction(status);

CREATE INDEX idx_payment_transaction_razorpay_order
    ON public.payment_transaction(razorpay_order_id);


CREATE TABLE public.payment_webhook_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_id VARCHAR(150) NOT NULL UNIQUE,

    event_type VARCHAR(100) NOT NULL,

    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_webhook_event_type
    ON public.payment_webhook_event(event_type);

CREATE INDEX idx_payment_webhook_received_at
    ON public.payment_webhook_event(received_at);