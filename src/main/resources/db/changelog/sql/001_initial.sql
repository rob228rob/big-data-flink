
CREATE TABLE dim_countries
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_cities
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(64) NOT NULL,
    country_id BIGINT REFERENCES dim_countries (id),
    UNIQUE (name, country_id)
);

CREATE TABLE dim_dates
(
    id   BIGSERIAL PRIMARY KEY,
    date DATE UNIQUE NOT NULL
);

CREATE TABLE dim_product_categories
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_product_colors
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_product_sizes
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_product_brands
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_product_materials
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_pet_types
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_pet_breeds
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_pet_categories
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_pets
(
    id          BIGSERIAL PRIMARY KEY,
    type_id     BIGINT REFERENCES dim_pet_types (id),
    name        VARCHAR(64) NOT NULL,
    breed_id    BIGINT REFERENCES dim_pet_breeds (id),
    category_id BIGINT REFERENCES dim_pet_categories (id),
    UNIQUE (name, type_id, breed_id, category_id)
);

CREATE TABLE dim_customers
(
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(64),
    last_name   VARCHAR(64),
    age         BIGINT,
    email       VARCHAR(64) UNIQUE NOT NULL,
    country_id  BIGINT REFERENCES dim_countries (id),
    postal_code VARCHAR(64),
    pet_id      BIGINT REFERENCES dim_pets (id)
);

CREATE TABLE dim_sellers
(
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(64),
    last_name   VARCHAR(64),
    email       VARCHAR(64) UNIQUE NOT NULL,
    country_id  BIGINT REFERENCES dim_countries (id),
    postal_code VARCHAR(64)
);

CREATE TABLE dim_products
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL,
    category_id     BIGINT REFERENCES dim_product_categories (id),
    price           NUMERIC(10, 2),
    weight          NUMERIC(10, 2),
    color_id        BIGINT REFERENCES dim_product_colors (id),
    size_id         BIGINT REFERENCES dim_product_sizes (id),
    brand_id        BIGINT REFERENCES dim_product_brands (id) NOT NULL,
    material_id     BIGINT REFERENCES dim_product_materials (id),
    description     VARCHAR(1024),
    rating          NUMERIC(3, 1),
    reviews         BIGINT,
    release_date_id BIGINT REFERENCES dim_dates (id),
    expiry_date_id  BIGINT REFERENCES dim_dates (id),
    UNIQUE (name, brand_id)
);

CREATE TABLE dim_stores
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(64) NOT NULL,
    location   VARCHAR(64),
    city_id    BIGINT REFERENCES dim_cities (id),
    state      VARCHAR(64),
    country_id BIGINT REFERENCES dim_countries (id),
    phone      VARCHAR(64),
    email      VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE dim_suppliers
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(64) NOT NULL,
    contact    VARCHAR(64),
    email      VARCHAR(64) UNIQUE NOT NULL,
    phone      VARCHAR(64),
    address    VARCHAR(64),
    city_id    BIGINT REFERENCES dim_cities (id),
    country_id BIGINT REFERENCES dim_countries (id)
);

-- Таблица фактов
CREATE TABLE facts_sales
(
    id               BIGSERIAL PRIMARY KEY,
    customer_id      BIGINT REFERENCES dim_customers (id) NOT NULL,
    seller_id        BIGINT REFERENCES dim_sellers (id) NOT NULL,
    product_id       BIGINT REFERENCES dim_products (id) NOT NULL,
    product_quantity BIGINT,
    date_id          BIGINT REFERENCES dim_dates (id) NOT NULL,
    total_price      DECIMAL(10, 2),
    store_id         BIGINT REFERENCES dim_stores (id) NOT NULL,
    supplier_id      BIGINT REFERENCES dim_suppliers (id) NOT NULL
);