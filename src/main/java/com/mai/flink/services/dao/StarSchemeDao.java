package com.mai.flink.services.dao;

import com.mai.flink.model.MockRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.sql.DataSource;

import static com.mai.flink.QueryConstants.*;

/**
 * @author batoyan.rl
 * @since 24.05.2025
 */
public class StarSchemeDao {
    private static final Logger log = LoggerFactory.getLogger(StarSchemeDao.class);

    private final DataSource dataSource;

    public StarSchemeDao(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    /**
     * Универсальный метод для UPSERT (INSERT ON CONFLICT DO UPDATE) и получения ID
     * для простых измерений с одним полем 'name' и UNIQUE(name).
     *
     * @param tableName Имя таблицы измерения (например, "dim_countries").
     * @param nameValue Значение поля 'name'.
     * @param conn      Активное соединение с БД.
     * @return ID вставленной или существующей записи, или null, если nameValue пустое.
     * @throws SQLException Если произошла ошибка SQL.
     */
    private Long upsertAndGetId(String tableName, String nameValue, Connection conn) throws SQLException {
        if (nameValue == null || nameValue.isBlank()) {
            log.warn("Attempted to upsert into {} with null or blank nameValue. Returning null ID.", tableName);
            return null; // Или ID для "UNKNOWN" записи, если такая стратегия выбрана
        }

        String sql = String.format(SQL_UPSERT_AND_GET_ID_TEMPLATE, tableName);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nameValue); // LOWER() уже в SQL-шаблоне
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        log.error("Failed to upsert and retrieve ID for {} with name '{}'. This should not happen.", tableName, nameValue);
        throw new SQLException("Failed to upsert and retrieve ID for " + tableName + " with name " + nameValue);
    }

    /**
     * UPSERT и получение ID для dim_dates.
     */
    private Long upsertDateAndGetId(Date dateValue, Connection conn) throws SQLException {
        if (dateValue == null) {
            log.warn("Attempted to upsert into dim_dates with null dateValue. Returning null ID.");
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPSERT_DATE_AND_GET_ID)) {
            stmt.setDate(1, dateValue);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        log.error("Failed to upsert and retrieve ID for dim_dates with date '{}'. This should not happen.", dateValue);
        throw new SQLException("Failed to upsert and retrieve ID for dim_dates with date " + dateValue);
    }

    /**
     * UPSERT и получение ID для dim_cities.
     */
    private Long upsertCityAndGetId(String cityName, Long countryId, Connection conn) throws SQLException {
        if (cityName == null || cityName.isBlank()) {
            log.warn("Attempted to upsert into dim_cities with null or blank cityName. Returning null ID.");
            return null;
        }
        if (countryId == null) {
            log.warn("Attempted to upsert into dim_cities for city '{}' with null countryId. Returning null ID.", cityName);
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPSERT_CITY_AND_GET_ID)) {
            stmt.setString(1, cityName);
            stmt.setLong(2, countryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        log.error("Failed to upsert and retrieve ID for dim_cities with name '{}' and country_id {}. This should not happen.", cityName, countryId);
        throw new SQLException("Failed to upsert and retrieve ID for dim_cities with name " + cityName);
    }

    /**
     * UPSERT и получение ID для dim_pets.
     */
    private Long upsertPetAndGetId(String petName, Long typeId, Long breedId, Long categoryId, Connection conn) throws SQLException {
        // Если petName пуст, или любой из FK null, то питомца не можем вставить корректно
        if (petName == null || petName.isBlank() || typeId == null || breedId == null || categoryId == null) {
            log.warn("Attempted to upsert into dim_pets with incomplete data (name: {}, typeId: {}, breedId: {}, categoryId: {}). Returning null ID.",
                    petName, typeId, breedId, categoryId);
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPSERT_PETS)) {
            stmt.setString(1, petName);
            stmt.setLong(2, typeId);
            stmt.setLong(3, breedId);
            stmt.setLong(4, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        log.error("Failed to upsert and retrieve ID for dim_pets with name '{}'. This should not happen.", petName);
        throw new SQLException("Failed to upsert and retrieve ID for dim_pets with name " + petName);
    }

    /**
     * UPSERT и получение ID для dim_products.
     */
    private Long upsertProductAndGetId(
            String name, Long categoryId, BigDecimal price, BigDecimal weight,
            Long colorId, Long sizeId, Long brandId, Long materialId,
            String description, Float rating, Long reviews,
            Long releaseDateId, Long expiryDateId, Connection conn) throws SQLException {

        // Валидация обязательных полей для продукта
        if (name == null || name.isBlank() || brandId == null) {
            log.warn("Attempted to upsert into dim_products with incomplete data (name: {}, brandId: {}). Returning null ID.", name, brandId);
            return null;
        }

        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPSERT_PRODUCTS)) {
            int i = 1;
            stmt.setString(i++, name); // name (LOWER() в SQL)
            stmt.setObject(i++, categoryId, java.sql.Types.BIGINT); // category_id
            stmt.setBigDecimal(i++, price); // price
            stmt.setBigDecimal(i++, weight); // weight
            stmt.setObject(i++, colorId, java.sql.Types.BIGINT); // color_id
            stmt.setObject(i++, sizeId, java.sql.Types.BIGINT); // size_id
            stmt.setObject(i++, brandId, java.sql.Types.BIGINT); // brand_id
            stmt.setObject(i++, materialId, java.sql.Types.BIGINT); // material_id
            stmt.setString(i++, description); // description
            stmt.setObject(i++, rating, java.sql.Types.REAL); // rating (FLOAT в DDL)
            stmt.setObject(i++, reviews, java.sql.Types.BIGINT); // reviews
            stmt.setObject(i++, releaseDateId, java.sql.Types.BIGINT); // release_date_id
            stmt.setObject(i++, expiryDateId, java.sql.Types.BIGINT); // expiry_date_id

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        log.error("Failed to upsert and retrieve ID for dim_products with name '{}' and brandId {}. This should not happen.", name, brandId);
        throw new SQLException("Failed to upsert and retrieve ID for dim_products with name " + name);
    }

    /**
     * UPSERT и получение ID для dim_customers.
     */
    private Long upsertCustomerAndGetId(
            String firstName, String lastName, Long age, String email,
            Long countryId, String postalCode, Long petId, Connection conn) throws SQLException {
        if (email == null || email.isBlank()) {
            log.warn("Attempted to upsert into dim_customers with null or blank email. Returning null ID.");
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPSERT_CUSTOMERS)) {
            int i = 1;
            stmt.setString(i++, firstName);
            stmt.setString(i++, lastName);
            stmt.setObject(i++, age, java.sql.Types.BIGINT);
            stmt.setString(i++, email);
            stmt.setObject(i++, countryId, java.sql.Types.BIGINT);
            stmt.setString(i++, postalCode);
            stmt.setObject(i++, petId, java.sql.Types.BIGINT);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        log.error("Failed to upsert and retrieve ID for dim_customers with email '{}'. This should not happen.", email);
        throw new SQLException("Failed to upsert and retrieve ID for dim_customers with email " + email);
    }

    /**
     * UPSERT и получение ID для dim_sellers.
     */
    private Long upsertSellerAndGetId(
            String firstName, String lastName, String email,
            Long countryId, String postalCode, Connection conn) throws SQLException {
        if (email == null || email.isBlank()) {
            log.warn("Attempted to upsert into dim_sellers with null or blank email. Returning null ID.");
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPSERT_SELLERS)) {
            int i = 1;
            stmt.setString(i++, firstName);
            stmt.setString(i++, lastName);
            stmt.setString(i++, email);
            stmt.setObject(i++, countryId, java.sql.Types.BIGINT);
            stmt.setString(i++, postalCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        log.error("Failed to upsert and retrieve ID for dim_sellers with email '{}'. This should not happen.", email);
        throw new SQLException("Failed to upsert and retrieve ID for dim_sellers with email " + email);
    }

    /**
     * UPSERT и получение ID для dim_stores.
     */
    private Long upsertStoreAndGetId(
            String name, String location, Long cityId, String state,
            Long countryId, String phone, String email, Connection conn) throws SQLException {
        if (email == null || email.isBlank()) {
            log.warn("Attempted to upsert into dim_stores with null or blank email. Returning null ID.");
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPSERT_STORES)) {
            int i = 1;
            stmt.setString(i++, name);
            stmt.setString(i++, location);
            stmt.setObject(i++, cityId, java.sql.Types.BIGINT);
            stmt.setString(i++, state);
            stmt.setObject(i++, countryId, java.sql.Types.BIGINT);
            stmt.setString(i++, phone);
            stmt.setString(i++, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        log.error("Failed to upsert and retrieve ID for dim_stores with email '{}'. This should not happen.", email);
        throw new SQLException("Failed to upsert and retrieve ID for dim_stores with email " + email);
    }

    /**
     * UPSERT и получение ID для dim_suppliers.
     */
    private Long upsertSupplierAndGetId(
            String name, String contact, String email, String phone,
            String address, Long cityId, Long countryId, Connection conn) throws SQLException {
        if (email == null || email.isBlank()) {
            log.warn("Attempted to upsert into dim_suppliers with null or blank email. Returning null ID.");
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPSERT_SUPPLIERS)) {
            int i = 1;
            stmt.setString(i++, name);
            stmt.setString(i++, contact);
            stmt.setString(i++, email);
            stmt.setString(i++, phone);
            stmt.setString(i++, address);
            stmt.setObject(i++, cityId, java.sql.Types.BIGINT);
            stmt.setObject(i++, countryId, java.sql.Types.BIGINT);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        log.error("Failed to upsert and retrieve ID for dim_suppliers with email '{}'. This should not happen.", email);
        throw new SQLException("Failed to upsert and retrieve ID for dim_suppliers with email " + email);
    }


    public void process(final MockRecord rawMockData) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Получаем ID для всех простых измерений
                Long customerCountryId = upsertAndGetId("dim_countries", rawMockData.customerCountry, conn);
                Long sellerCountryId = upsertAndGetId("dim_countries", rawMockData.sellerCountry, conn);
                Long storeCountryId = upsertAndGetId("dim_countries", rawMockData.storeCountry, conn);
                Long supplierCountryId = upsertAndGetId("dim_countries", rawMockData.supplierCountry, conn);

                Long storeCityId = upsertCityAndGetId(rawMockData.storeCity, storeCountryId, conn);
                Long supplierCityId = upsertCityAndGetId(rawMockData.supplierCity, supplierCountryId, conn);

                Long saleDateId = upsertDateAndGetId(toSqlDate(rawMockData.saleDate), conn);
                Long productReleaseDateId = upsertDateAndGetId(toSqlDate(rawMockData.productReleaseDate), conn);
                Long productExpiryDateId = upsertDateAndGetId(toSqlDate(rawMockData.productExpiryDate), conn);

                Long petTypeId = upsertAndGetId("dim_pet_types", rawMockData.customerPetType, conn);
                Long petBreedId = upsertAndGetId("dim_pet_breeds", rawMockData.customerPetBreed, conn);
                Long petCategoryId = upsertAndGetId("dim_pet_categories", rawMockData.petCategory, conn);

                Long productCategoryId = upsertAndGetId("dim_product_categories", rawMockData.productCategory, conn);
                Long productColorId = upsertAndGetId("dim_product_colors", rawMockData.productColor, conn);
                Long productSizeId = upsertAndGetId("dim_product_sizes", rawMockData.productSize, conn);
                Long productBrandId = upsertAndGetId("dim_product_brands", rawMockData.productBrand, conn);
                Long productMaterialId = upsertAndGetId("dim_product_materials", rawMockData.productMaterial, conn); // Используем новое имя

                // Получаем ID для более сложных измерений, используя полученные FK
                Long petId = upsertPetAndGetId(
                        rawMockData.customerPetName, petTypeId, petBreedId, petCategoryId, conn
                );

                Long productId = upsertProductAndGetId(
                        rawMockData.productName, productCategoryId, rawMockData.productPrice,
                        BigDecimal.valueOf(rawMockData.productWeight),
                        productColorId, productSizeId, productBrandId, productMaterialId,
                        rawMockData.productDescription, rawMockData.productRating, rawMockData.productReviews,
                        productReleaseDateId, productExpiryDateId, conn
                );

                Long customerId = upsertCustomerAndGetId(
                        rawMockData.customerFirstName, rawMockData.customerLastName, rawMockData.customerAge,
                        rawMockData.customerEmail, customerCountryId, rawMockData.customerPostalCode, petId, conn
                );

                Long sellerId = upsertSellerAndGetId(
                        rawMockData.sellerFirstName, rawMockData.sellerLastName, rawMockData.sellerEmail,
                        sellerCountryId, rawMockData.sellerPostalCode, conn
                );

                Long storeId = upsertStoreAndGetId(
                        rawMockData.storeName, rawMockData.storeLocation, storeCityId,
                        rawMockData.storeState, storeCountryId, rawMockData.storePhone, rawMockData.storeEmail, conn
                );

                Long supplierId = upsertSupplierAndGetId(
                        rawMockData.supplierName, rawMockData.supplierContact, rawMockData.supplierEmail,
                        rawMockData.supplierPhone, rawMockData.supplierAddress, supplierCityId, supplierCountryId, conn
                );

               if (customerId == null || sellerId == null || productId == null || saleDateId == null || storeId == null || supplierId == null) {
                    log.error("Skipping fact_sales insertion due to missing required dimension IDs for record: {}", rawMockData.id);
                    conn.rollback();
                    throw new IllegalStateException("fact_sales insertion missing!!");
                }

                try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_FACT_SALES)) {
                    int i = 1;
                    stmt.setLong(i++, customerId);
                    stmt.setLong(i++, sellerId);
                    stmt.setLong(i++, productId);
                    stmt.setObject(i++, rawMockData.saleQuantity, java.sql.Types.BIGINT); // Используем setObject для nullable
                    stmt.setLong(i++, saleDateId);
                    stmt.setBigDecimal(i++, rawMockData.saleTotalPrice);
                    stmt.setLong(i++, storeId);
                    stmt.setLong(i++, supplierId);
                    stmt.executeUpdate();
                }

                conn.commit(); // Завершить транзакцию
            } catch (SQLException e) {
                conn.rollback(); // Откатить транзакцию при ошибке
                log.error("Error processing record: {}", rawMockData.id, e);
                throw e; // Перебросить исключение для Flink
            }
        }
    }

    private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

    private java.sql.Date toSqlDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateStr, CUSTOM_FORMATTER);
            return Date.valueOf(localDate);
        } catch (Exception e) {
            log.error("Failed to parse date '{}': {}", dateStr, e.getMessage());
            return null;
        }
    }

    @FunctionalInterface
    private interface ParameterSetter {
        void setParameters(PreparedStatement stmt) throws SQLException;
    }
}