package com.mai.flink.converter;

import com.mai.flink.dto.MockRecordDto;
import com.mai.flink.model.MockRecord;

import java.math.BigDecimal;

/**
 * @author batoyan.rl
 * @since 24.05.2025
 */
public class MockRecordConverter {

    public static MockRecord fromDto(MockRecordDto dto) {
        MockRecord record = new MockRecord();

        record.setId(parseLong(dto.getId()));

        record.setCustomerFirstName(dto.getCustomerFirstName());
        record.setCustomerLastName(dto.getCustomerLastName());
        record.setCustomerAge(parseLong(dto.getCustomerAge()));
        record.setCustomerEmail(dto.getCustomerEmail());
        record.setCustomerCountry(dto.getCustomerCountry());
        record.setCustomerPostalCode(dto.getCustomerPostalCode());
        record.setCustomerPetType(dto.getCustomerPetType());
        record.setCustomerPetName(dto.getCustomerPetName());
        record.setCustomerPetBreed(dto.getCustomerPetBreed());
        record.setPetCategory(dto.getPetCategory());

        record.setSellerFirstName(dto.getSellerFirstName());
        record.setSellerLastName(dto.getSellerLastName());
        record.setSellerEmail(dto.getSellerEmail());
        record.setSellerCountry(dto.getSellerCountry());
        record.setSellerPostalCode(dto.getSellerPostalCode());

        record.setProductName(dto.getProductName());
        record.setProductCategory(dto.getProductCategory());
        record.setProductPrice(parseBigDecimal(dto.getProductPrice()));
        record.setProductWeight(parseFloat(dto.getProductWeight()));
        record.setProductColor(dto.getProductColor());
        record.setProductSize(dto.getProductSize());
        record.setProductBrand(dto.getProductBrand());
        record.setProductMaterial(dto.getProductMaterial());
        record.setProductDescription(dto.getProductDescription());
        record.setProductRating(parseFloat(dto.getProductRating()));
        record.setProductReviews(parseLong(dto.getProductReviews()));
        record.setProductReleaseDate(dto.getProductReleaseDate());
        record.setProductExpiryDate(dto.getProductExpiryDate());

        record.setStoreName(dto.getStoreName());
        record.setStoreLocation(dto.getStoreLocation());
        record.setStoreCity(dto.getStoreCity());
        record.setStoreState(dto.getStoreState());
        record.setStoreCountry(dto.getStoreCountry());
        record.setStorePhone(dto.getStorePhone());
        record.setStoreEmail(dto.getStoreEmail());

        record.setSupplierName(dto.getSupplierName());
        record.setSupplierContact(dto.getSupplierContact());
        record.setSupplierEmail(dto.getSupplierEmail());
        record.setSupplierPhone(dto.getSupplierPhone());
        record.setSupplierAddress(dto.getSupplierAddress());
        record.setSupplierCity(dto.getSupplierCity());
        record.setSupplierCountry(dto.getSupplierCountry());

        record.setSaleDate(dto.getSaleDate());
        record.setSaleQuantity(parseLong(dto.getSaleQuantity()));
        record.setSaleTotalPrice(parseBigDecimal(dto.getSaleTotalPrice()));

        return record;
    }

    private static Long parseLong(String s) {
        try {
            return (s == null || s.isBlank()) ? 0L : Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static Float parseFloat(String s) {
        try {
            return (s == null || s.isBlank()) ? 0f : Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    private static BigDecimal parseBigDecimal(String s) {
        try {
            return (s == null || s.isBlank()) ? BigDecimal.ZERO : new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
