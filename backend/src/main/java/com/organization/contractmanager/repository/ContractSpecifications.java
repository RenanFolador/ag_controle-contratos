package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractAssignment;
import com.organization.contractmanager.domain.ContractStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ContractSpecifications {

    private ContractSpecifications() {
    }

    public static Specification<Contract> textContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("contractNumber")), pattern),
                builder.like(builder.lower(root.get("processNumber")), pattern),
                builder.like(builder.lower(root.get("companyName")), pattern),
                builder.like(builder.lower(root.get("companyCnpj")), pattern),
                builder.like(builder.lower(root.get("object")), pattern));
    }

    public static Specification<Contract> hasStatus(ContractStatus status) {
        return status == null ? null
                : (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Contract> endsInYear(Integer year) {
        return year == null ? null : (root, query, builder) -> builder.equal(
                builder.function("year", Integer.class, root.get("endDate")), year);
    }

    public static Specification<Contract> assignedTo(UUID personId) {
        if (personId == null) {
            return null;
        }
        return (root, query, builder) -> {
            var subquery = query.subquery(UUID.class);
            var assignment = subquery.from(ContractAssignment.class);
            subquery.select(assignment.get("contract").get("id"))
                    .where(builder.equal(assignment.get("person").get("id"), personId));
            return root.get("id").in(subquery);
        };
    }

    public static Specification<Contract> expiresWithin(Integer expirationDays, LocalDate today) {
        if (expirationDays == null) {
            return null;
        }
        return (root, query, builder) -> builder.between(
                root.get("endDate"), today, today.plusDays(expirationDays));
    }

    public static Specification<Contract> companyContains(String company) {
        if (company == null || company.isBlank()) {
            return null;
        }
        String pattern = "%" + company.trim().toLowerCase() + "%";
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("companyName")), pattern);
    }

    public static Specification<Contract> endDateBetween(LocalDate start, LocalDate end) {
        if (start == null && end == null) return null;
        if (start == null) return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("endDate"), end);
        if (end == null) return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("endDate"), start);
        return (root, query, builder) -> builder.between(root.get("endDate"), start, end);
    }

    public static Specification<Contract> expiredBefore(LocalDate today) {
        return (root, query, builder) -> builder.lessThan(root.get("endDate"), today);
    }
}
