package com.mai.flink.services.dao;

import com.mai.flink.model.MockRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
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

    private void simpleUpsert(String sqlQuery, String values) throws SQLException {
        if (values == null || values.isBlank()) {
            log.error("Invalid values string");
            return;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.setString(1, values);
            stmt.executeUpdate();
        }
    }

    private void simpleUpsert(String sqlQuery, java.sql.Date dateValue) throws SQLException {
        if (dateValue == null) {
            log.error("Invalid date value (null)");
            return;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.setDate(1, dateValue);
            stmt.executeUpdate();
        }
    }

    private void executeWithParams(String sql, ParameterSetter setter) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.setParameters(stmt);
            stmt.executeUpdate();
        }
    }

    public void process(final MockRecord rawMockData) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Simple upserts
                simpleUpsert(SQL_DIM_COUNTRY, rawMockData.customerCountry);
                simpleUpsert(SQL_DIM_COUNTRY, rawMockData.sellerCountry);
                simpleUpsert(SQL_DIM_COUNTRY, rawMockData.storeCountry);
                simpleUpsert(SQL_DIM_COUNTRY, rawMockData.supplierCountry);
                simpleUpsert(SQL_DIM_CITY,    rawMockData.storeCity);
                simpleUpsert(SQL_DIM_CITY,    rawMockData.supplierCity);
                simpleUpsert(SQL_DIM_DATE, toSqlDate(rawMockData.saleDate));
                simpleUpsert(SQL_DIM_DATE, toSqlDate(rawMockData.productReleaseDate));
                simpleUpsert(SQL_DIM_DATE, toSqlDate(rawMockData.productExpiryDate));
                simpleUpsert(SQL_DIM_PET_TYPE,  rawMockData.customerPetType);
                simpleUpsert(SQL_DIM_PET_BREED, rawMockData.customerPetBreed);
                simpleUpsert(SQL_DIM_PET_CAT,   rawMockData.petCategory);
                simpleUpsert(SQL_DIM_PCAT,      rawMockData.productCategory);
                simpleUpsert(SQL_DIM_PCOLOR,    rawMockData.productColor);
                simpleUpsert(SQL_DIM_PSIZE,     rawMockData.productSize);
                simpleUpsert(SQL_DIM_PBRAND,    rawMockData.productBrand);
                simpleUpsert(SQL_DIM_PVERB,     rawMockData.productMaterial);

                // Pets table
                executeWithParams(SQL_DIM_PETS, stmt -> {
                    stmt.setString(1, rawMockData.customerPetName);
                    stmt.setString(2, rawMockData.customerPetType);
                    stmt.setString(3, rawMockData.customerPetBreed);
                    stmt.setString(4, rawMockData.petCategory);
                });

                executeWithParams(SQL_DIM_PRODUCTS_MERGE, stmt -> {
                    stmt.setString(1, rawMockData.productName);            // name
                    stmt.setBigDecimal(2, rawMockData.productPrice);       // price
                    stmt.setBigDecimal(3, BigDecimal.valueOf(rawMockData.productWeight));      // weight
                    stmt.setString(4, rawMockData.productDescription);     // description
                    stmt.setFloat(5, rawMockData.productRating);           // rating
                    stmt.setInt(6, (int) rawMockData.productReviews);            // reviews

                    // Lookup tables join parameters
                    stmt.setString(7, rawMockData.productColor);           // cl.name
                    stmt.setString(8, rawMockData.productSize);            // sz.name
                    stmt.setString(9, rawMockData.productBrand);           // br.name
                    stmt.setString(10, rawMockData.productMaterial);       // ve.name
                    stmt.setString(11, rawMockData.productReleaseDate);    // rd.date
                    stmt.setString(12, rawMockData.productExpiryDate);     // ed.date
                    stmt.setString(13, rawMockData.productCategory);       // pc.name
                });


                // Customers table
                executeWithParams(SQL_DIM_CUSTOMERS, stmt -> {
                    stmt.setString(1, rawMockData.customerFirstName);
                    stmt.setString(2, rawMockData.customerLastName);
                    stmt.setLong(3, rawMockData.customerAge);
                    stmt.setString(4, rawMockData.customerEmail);
                    stmt.setString(5, rawMockData.customerPostalCode);
                    stmt.setString(6, rawMockData.customerPetName);
                    stmt.setString(7, rawMockData.customerCountry);
                });

                // Sellers table
                executeWithParams(SQL_DIM_SELLERS, stmt -> {
                    stmt.setString(1, rawMockData.sellerFirstName);
                    stmt.setString(2, rawMockData.sellerLastName);
                    stmt.setString(3, rawMockData.sellerEmail);
                    stmt.setString(4, rawMockData.sellerPostalCode);
                    stmt.setString(5, rawMockData.sellerCountry);
                });

                // Stores table
                executeWithParams(SQL_DIM_STORES, stmt -> {
                    stmt.setString(1, rawMockData.storeName);
                    stmt.setString(2, rawMockData.storeLocation);
                    stmt.setString(3, rawMockData.storeCity);
                    stmt.setString(4, rawMockData.storeState);
                    stmt.setString(5, rawMockData.storeCountry);
                    stmt.setString(6, rawMockData.storePhone);
                    stmt.setString(7, rawMockData.storeEmail);
                });

                // Suppliers table
                executeWithParams(SQL_DIM_SUPPLIERS, stmt -> {
                    stmt.setString(1, rawMockData.supplierName);
                    stmt.setString(2, rawMockData.supplierContact);
                    stmt.setString(3, rawMockData.supplierEmail);
                    stmt.setString(4, rawMockData.supplierPhone);
                    stmt.setString(5, rawMockData.supplierAddress);
                    stmt.setString(6, rawMockData.supplierCity);
                    stmt.setString(7, rawMockData.supplierCountry);
                });

                // Sales fact table
                executeWithParams(SQL_FACT_SALES, stmt -> {
                    stmt.setLong(1, rawMockData.saleQuantity);               // product_quantity
                    stmt.setBigDecimal(2, rawMockData.saleTotalPrice);       // total_price
                    stmt.setString(3, rawMockData.sellerEmail);              // s.email
                    stmt.setString(4, rawMockData.productName);              // pr.name
                    stmt.setDate(5, toSqlDate(rawMockData.saleDate));        // d.date
                    stmt.setString(6, rawMockData.storeEmail);               // st.email
                    stmt.setString(7, rawMockData.supplierEmail);            // su.email
                });

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
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