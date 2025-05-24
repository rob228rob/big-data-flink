package com.mai.flink;

/**
 * @author batoyan.rl
 * @since 23.05.2025
 */
public interface QueryConstants {

    String SQL_DIM_COUNTRY =
            "INSERT INTO dim_countries(name) VALUES(?)";

    String SQL_DIM_CITY =
            "INSERT INTO dim_cities(name) VALUES(?)";

    String SQL_DIM_DATE =
            "INSERT INTO dim_dates(date) VALUES(?)";

    String SQL_DIM_PET_TYPE =
            "INSERT INTO dim_pet_types(name) VALUES(?)";

    String SQL_DIM_PET_BREED =
            "INSERT INTO dim_pet_breeds(name) VALUES(?)";

    String SQL_DIM_PET_CAT =
            "INSERT INTO dim_pet_categories(name) VALUES(?)";

    String SQL_DIM_PCAT =
            "INSERT INTO dim_product_categories(name) VALUES(?)";

    String SQL_DIM_PCOLOR =
            "INSERT INTO dim_product_colors(name) VALUES(?)";

    String SQL_DIM_PSIZE =
            "INSERT INTO dim_product_sizes(name) VALUES(?)";

    String SQL_DIM_PBRAND =
            "INSERT INTO dim_product_brands(name) VALUES(?)";

    String SQL_DIM_PVERB =
            "INSERT INTO dim_product_verbose(name) VALUES(?)";

    String SQL_DIM_PETS =
            "INSERT INTO dim_pets(type_id, name, breed_id, category_id) " +
                    "SELECT pt.id, ?, pb.id, pc.id " +
                    "FROM dim_pet_types pt " +
                    "JOIN dim_pet_breeds pb ON pb.name = ? " +
                    "JOIN dim_pet_categories pc ON pc.name = ? " +
                    "WHERE pt.name = ?";

    String SQL_DIM_PRODUCTS =
            "INSERT INTO dim_products(" +
                    "name, category_id, price, weight, " +
                    "color_id, size_id, brand_id, material_id, " +
                    "description, rating, reviews, " +
                    "release_date_id, expiry_date_id) " +
                    "SELECT ?, pc.id, ?, ?, " +
                    "cl.id, sz.id, br.id, ve.id, " +
                    "?, ?, ?, rd.id, ed.id " +
                    "FROM dim_product_categories pc " +
                    "JOIN dim_product_colors cl ON cl.name = ? " +
                    "JOIN dim_product_sizes sz ON sz.name = ? " +
                    "JOIN dim_product_brands br ON br.name = ? " +
                    "JOIN dim_product_verbose ve ON ve.name = ? " +
                    "WHERE pc.name = ? " +
                    "AND NOT EXISTS (" +
                    "SELECT 1 FROM dim_products pr " +
                    "WHERE pr.name = ? " +
                    "AND pr.price = ? " +
                    "AND pr.weight = ? " +
                    "AND pr.color_id = cl.id " +
                    "AND pr.size_id = sz.id " +
                    "AND pr.brand_id = br.id " +
                    "AND pr.material_id = ve.id " +
                    "AND pr.category_id = pc.id " +
                    ")";

    String SQL_DIM_PRODUCTS_MERGE =
            "MERGE INTO dim_products AS target " +
                    "USING (" +
                    "    SELECT ? AS name, pc.id AS category_id, ? AS price, ? AS weight, " +
                    "    cl.id AS color_id, sz.id AS size_id, br.id AS brand_id, ve.id AS material_id, " +
                    "    ? AS description, ? AS rating, ? AS reviews, rd.id AS release_date_id, ed.id AS expiry_date_id " +
                    "    FROM dim_product_categories pc " +
                    "    JOIN dim_product_colors cl ON cl.name = ? " +
                    "    JOIN dim_product_sizes sz ON sz.name = ? " +
                    "    JOIN dim_product_brands br ON br.name = ? " +
                    "    JOIN dim_product_verbose ve ON ve.name = ? " +
                    "    JOIN dim_dates rd ON rd.date = ? " +
                    "    JOIN dim_dates ed ON ed.date = ? " +
                    "    WHERE pc.name = ?" +
                    ") AS source " +
                    "ON (" +
                    "    target.name = source.name AND target.category_id = source.category_id " +
                    "    AND target.price = source.price AND target.weight = source.weight " +
                    "    AND target.color_id = source.color_id AND target.size_id = source.size_id " +
                    "    AND target.brand_id = source.brand_id AND target.material_id = source.material_id " +
                    "    AND target.release_date_id = source.release_date_id AND target.expiry_date_id = source.expiry_date_id" +
                    ") " +
                    "WHEN NOT MATCHED THEN " +
                    "INSERT (name, category_id, price, weight, color_id, size_id, brand_id, material_id, description, rating, reviews, release_date_id, expiry_date_id) " +
                    "VALUES (source.name, source.category_id, source.price, source.weight, source.color_id, source.size_id, source.brand_id, source.material_id, source.description, source.rating, source.reviews, source.release_date_id, source.expiry_date_id)";

    String SQL_DIM_CUSTOMERS =
            "INSERT INTO dim_customers(" +
                    "first_name, last_name, age, email, " +
                    "country_id, postal_code, pet_id" +
                    ") " +
                    "SELECT ?, ?, ?, ?, " +
                    "cn.id, ?, p.id " +
                    "FROM dim_countries cn " +
                    "JOIN dim_pets p ON p.name = ? " +
                    "WHERE cn.name = ?";

    String SQL_DIM_SELLERS =
            "INSERT INTO dim_sellers(" +
                    "first_name, last_name, email, country_id, postal_code" +
                    ") " +
                    "SELECT ?, ?, ?, cn.id, ? " +
                    "FROM dim_countries cn " +
                    "WHERE cn.name = ?";

    String SQL_DIM_STORES =
            "INSERT INTO dim_stores(" +
                    "name, location, city_id, state, country_id, phone, email" +
                    ") " +
                    "SELECT ?, ?, ci.id, ?, cn.id, ?, ? " +
                    "FROM dim_cities ci " +
                    "JOIN dim_countries cn ON ci.name = ? AND cn.name = ?";

    String SQL_DIM_SUPPLIERS =
            "INSERT INTO dim_suppliers(" +
                    "name, contact, email, phone, address, city_id, country_id" +
                    ") " +
                    "SELECT ?, ?, ?, ?, ?, ci.id, cn.id " +
                    "FROM dim_cities ci " +
                    "JOIN dim_countries cn ON ci.name = ? AND cn.name = ?";

    String SQL_FACT_SALES =
            "INSERT INTO facts_sales(" +
                    "customer_id, seller_id, product_id, product_quantity, date_id, total_price, store_id, supplier_id" +
                    ") " +
                    "SELECT cu.id, s.id, pr.id, ?, d.id, ?, st.id, su.id " +
                    "FROM dim_customers cu " +
                    "JOIN dim_sellers s ON s.email = ? " +
                    "JOIN dim_products pr ON pr.name = ? " +
                    "JOIN dim_dates d ON d.date = ? " +
                    "JOIN dim_stores st ON st.email = ? " +
                    "JOIN dim_suppliers su ON su.email = ?";
}