CREATE TABLE metering_point (
                                id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                gsrn        VARCHAR(18)  NOT NULL UNIQUE,
                                name        VARCHAR(255) NOT NULL,
                                created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE meter_reading (
                               id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               metering_point_id  BIGINT         NOT NULL REFERENCES metering_point (id),
                               period_start       TIMESTAMPTZ    NOT NULL,
                               quantity_kwh       NUMERIC(14, 3) NOT NULL CHECK (quantity_kwh >= 0),
                               created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
                               CONSTRAINT uq_reading_point_period UNIQUE (metering_point_id, period_start)
);