package com.mai.flink;

/**
 * @author batoyan.rl
 * @since 23.05.2025
 */
public interface QueryConstants {

    String SQL_UPSERT_AND_GET_ID_TEMPLATE =
            "INSERT INTO %s(name) VALUES(LOWER(?)) ON CONFLICT (name) DO UPDATE SET name=EXCLUDED.name RETURNING id;";

    String SQL_UPSERT_DATE_AND_GET_ID =
            "INSERT INTO dim_dates(date) VALUES(?) ON CONFLICT (date) DO UPDATE SET date=EXCLUDED.date RETURNING id;";

    String SQL_UPSERT_CITY_AND_GET_ID =
            "INSERT INTO dim_cities(name, country_id) VALUES(LOWER(?), ?) ON CONFLICT (name, country_id) DO UPDATE SET name=EXCLUDED.name RETURNING id;";

    String SQL_UPSERT_PETS =
            "INSERT INTO dim_pets(name, type_id, breed_id, category_id) " +
                    "VALUES(LOWER(?), ?, ?, ?) " +
                    "ON CONFLICT (name, type_id, breed_id, category_id) DO UPDATE SET name=EXCLUDED.name RETURNING id;";

    String SQL_UPSERT_PRODUCTS =
            "INSERT INTO dim_products(" +
                    "name, category_id, price, weight, color_id, size_id, brand_id, material_id, description, rating, reviews, release_date_id, expiry_date_id" +
                    ") VALUES (" +
                    "LOWER(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                    ") " +
                    "ON CONFLICT (name, brand_id) DO UPDATE SET " +
                    "category_id = EXCLUDED.category_id, " +
                    "price = EXCLUDED.price, " +
                    "weight = EXCLUDED.weight, " +
                    "color_id = EXCLUDED.color_id, " +
                    "size_id = EXCLUDED.size_id, " +
                    "material_id = EXCLUDED.material_id, " +
                    "description = EXCLUDED.description, " +
                    "rating = EXCLUDED.rating, " +
                    "reviews = EXCLUDED.reviews, " +
                    "release_date_id = EXCLUDED.release_date_id, " +
                    "expiry_date_id = EXCLUDED.expiry_date_id " +
                    "RETURNING id;";

    String SQL_UPSERT_CUSTOMERS =
            "INSERT INTO dim_customers(" +
                    "first_name, last_name, age, email, country_id, postal_code, pet_id" +
                    ") VALUES (LOWER(?), LOWER(?), ?, LOWER(?), ?, ?, ?) " +
                    "ON CONFLICT (email) DO UPDATE SET " +
                    "first_name = EXCLUDED.first_name, " +
                    "last_name = EXCLUDED.last_name, " +
                    "age = EXCLUDED.age, " +
                    "country_id = EXCLUDED.country_id, " +
                    "postal_code = EXCLUDED.postal_code, " +
                    "pet_id = EXCLUDED.pet_id " +
                    "RETURNING id;";

    String SQL_UPSERT_SELLERS =
            "INSERT INTO dim_sellers(" +
                    "first_name, last_name, email, country_id, postal_code" +
                    ") VALUES (LOWER(?), LOWER(?), LOWER(?), ?, ?) " +
                    "ON CONFLICT (email) DO UPDATE SET " +
                    "first_name = EXCLUDED.first_name, " +
                    "last_name = EXCLUDED.last_name, " +
                    "country_id = EXCLUDED.country_id, " +
                    "postal_code = EXCLUDED.postal_code " +
                    "RETURNING id;";

    String SQL_UPSERT_STORES =
            "INSERT INTO dim_stores(" +
                    "name, location, city_id, state, country_id, phone, email" +
                    ") VALUES (LOWER(?), LOWER(?), ?, LOWER(?), ?, ?, LOWER(?)) " +
                    "ON CONFLICT (email) DO UPDATE SET " +
                    "name = EXCLUDED.name, " +
                    "location = EXCLUDED.location, " +
                    "city_id = EXCLUDED.city_id, " +
                    "state = EXCLUDED.state, " +
                    "country_id = EXCLUDED.country_id, " +
                    "phone = EXCLUDED.phone " +
                    "RETURNING id;";

    String SQL_UPSERT_SUPPLIERS =
            "INSERT INTO dim_suppliers(" +
                    "name, contact, email, phone, address, city_id, country_id" +
                    ") VALUES (LOWER(?), LOWER(?), LOWER(?), ?, LOWER(?), ?, ?) " +
                    "ON CONFLICT (email) DO UPDATE SET " +
                    "name = EXCLUDED.name, " +
                    "contact = EXCLUDED.contact, " +
                    "phone = EXCLUDED.phone, " +
                    "address = EXCLUDED.address, " +
                    "city_id = EXCLUDED.city_id, " +
                    "country_id = EXCLUDED.country_id " +
                    "RETURNING id;";

    String SQL_INSERT_FACT_SALES =
            "INSERT INTO facts_sales(" +
                    "customer_id, seller_id, product_id, product_quantity, date_id, total_price, store_id, supplier_id" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
}