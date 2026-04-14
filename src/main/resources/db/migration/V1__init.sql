CREATE TABLE users (
    id UUID NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE vehicle (
    id UUID NOT NULL,
    license_plate VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    vehicle_year INTEGER NOT NULL,
    color VARCHAR(255) NOT NULL,
    price NUMERIC(38, 2) NOT NULL,
    deleted BOOLEAN,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT pk_vehicle PRIMARY KEY (id),
    CONSTRAINT uk_vehicle_license_plate UNIQUE (license_plate)
);
