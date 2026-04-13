package com.vitorino.apiveiculos.specification;

import com.vitorino.apiveiculos.model.Vehicle;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class VehicleSpecification {

    public static Specification<Vehicle> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("deleted"));
    }

    public static Specification<Vehicle> hasBrand(String brand) {
        return (root, query, criteriaBuilder) ->
                brand == null || brand.isBlank()
                        ? null
                        : criteriaBuilder.equal(criteriaBuilder.lower(root.get("brand")), brand.toLowerCase());
    }

    public static Specification<Vehicle> hasYear(Integer year) {
        return (root, query, criteriaBuilder) ->
                year == null
                        ? null
                        : criteriaBuilder.equal(root.get("vehicleYear"), year);
    }

    public static Specification<Vehicle> hasColor(String color) {
        return (root, query, criteriaBuilder) ->
                color == null || color.isBlank()
                        ? null
                        : criteriaBuilder.equal(criteriaBuilder.lower(root.get("color")), color.toLowerCase());
    }

    public static Specification<Vehicle> priceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                minPrice == null
                        ? null
                        : criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Vehicle> priceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                maxPrice == null
                        ? null
                        : criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}