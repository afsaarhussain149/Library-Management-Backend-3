-- ================================================================
-- Library Seat Booking System - PostgreSQL Schema
-- Run this manually against your `lib_db` database before starting
-- the app (spring.jpa.hibernate.ddl-auto=none, same convention as
-- the original reference project).
--
--   createdb lib_db
--   psql -U postgres -d lib_db -f init.sql
-- ================================================================

-- 1) ADMIN
CREATE TABLE IF NOT EXISTS admin_user (
    admin_id    SERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    phone       VARCHAR(20)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    image       TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2) APP USER (student/member)
CREATE TABLE IF NOT EXISTS app_user (
    user_id            SERIAL PRIMARY KEY,
    full_name          VARCHAR(150) NOT NULL,
    phone_number       VARCHAR(20)  UNIQUE,
    photo              TEXT,
    father_name        VARCHAR(150),
    preparation_for    VARCHAR(150),
    dob                DATE,
    blood_group        VARCHAR(10),
    email              VARCHAR(150),
    personal_number    VARCHAR(20),
    emergency_number   VARCHAR(20),
    present_address    TEXT,
    permanent_address  TEXT,
    gender             VARCHAR(20),
    aadhar_number      VARCHAR(20),
    password           VARCHAR(255),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3) PAYMENT (online + cash, plan/shift/seat details flattened, seats as CSV)
CREATE TABLE IF NOT EXISTS payment (
    payment_id              SERIAL PRIMARY KEY,
    user_id                 VARCHAR(50) NOT NULL,
    amount                  NUMERIC(10,2) NOT NULL,
    currency                VARCHAR(10) DEFAULT 'INR',
    is_active               BOOLEAN DEFAULT TRUE,
    plan_expire_seat_block  BOOLEAN DEFAULT FALSE,
    payment_mode            VARCHAR(20) DEFAULT 'online',   -- online / cash
    plan_hours              INTEGER,
    plan_type                VARCHAR(50),
    plan_amount              NUMERIC(10,2),
    shift_label              VARCHAR(50),
    shift_time                VARCHAR(50),
    seats                    TEXT,                          -- comma separated seat numbers e.g. "12,13"
    end_plan_date             DATE,
    razorpay_order_id        VARCHAR(100),
    razorpay_payment_id      VARCHAR(100),
    razorpay_signature       VARCHAR(255),
    status                    VARCHAR(20) DEFAULT 'created', -- created / paid / pending / failed
    is_approved_by_admin     BOOLEAN DEFAULT FALSE,
    metadata                  JSONB,
    created_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_payment_user_id ON payment(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment(status);

-- 4) SEAT SELECTION
CREATE TABLE IF NOT EXISTS seat_selection (
    seat_selection_id  SERIAL PRIMARY KEY,
    user_id             INTEGER NOT NULL,
    plan_id             INTEGER,
    seat_no             INTEGER NOT NULL,
    status              VARCHAR(20) DEFAULT 'booked',  -- booked / available
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5) SHIFT SELECTION
CREATE TABLE IF NOT EXISTS shift_selection (
    shift_selection_id  SERIAL PRIMARY KEY,
    user_id              INTEGER NOT NULL,
    plan_id              INTEGER,
    shift_label          VARCHAR(50) NOT NULL,
    shift_time           VARCHAR(50) NOT NULL,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6) USER SELECTION (plan+option pick, before payment)
CREATE TABLE IF NOT EXISTS user_selection (
    selection_id     SERIAL PRIMARY KEY,
    user_id           INTEGER NOT NULL,
    full_name         VARCHAR(150),
    email             VARCHAR(150),
    plan_hours        INTEGER,
    selected_option   VARCHAR(100),
    price             NUMERIC(10,2),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7) PLAN CATALOG + OPTIONS (auto-seeded on first /api/plans call)
CREATE TABLE IF NOT EXISTS plan_catalog (
    plan_id  SERIAL PRIMARY KEY,
    hours    INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS plan_option (
    option_id  SERIAL PRIMARY KEY,
    plan_id    INTEGER NOT NULL REFERENCES plan_catalog(plan_id) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    price      NUMERIC(10,2) NOT NULL
);

-- 8) COMPLAINT
CREATE TABLE IF NOT EXISTS complaint (
    complaint_id  SERIAL PRIMARY KEY,
    user_id        INTEGER NOT NULL,
    message        TEXT NOT NULL,
    issue_type     VARCHAR(100) DEFAULT 'General',
    status         VARCHAR(50) DEFAULT 'Pending',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9) PUBLIC QUERY (contact-us form)
CREATE TABLE IF NOT EXISTS public_query (
    query_id    SERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    mail         VARCHAR(150) NOT NULL,
    subject      VARCHAR(150) NOT NULL,
    message      VARCHAR(2000) NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
