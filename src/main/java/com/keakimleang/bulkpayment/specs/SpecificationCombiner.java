package com.keakimleang.bulkpayment.specs;

import java.util.Arrays;
import java.util.List;
import org.springframework.data.relational.core.query.Criteria;

public final class SpecificationCombiner {

    private SpecificationCombiner() {
    }

    public static Criteria and(final Criteria... specifications) {
        return Arrays.stream(specifications)
                .reduce(Criteria.empty(), Criteria::and);
    }

    public static Criteria and(final List<Criteria> specifications) {
        return specifications
                .stream()
                .reduce(Criteria.empty(), Criteria::and);
    }

    public static Criteria or(final Criteria... specifications) {
        return Arrays.stream(specifications)
                .reduce(Criteria.empty(), Criteria::or);
    }

    public static Criteria or(final List<Criteria> specifications) {
        return specifications
                .stream()
                .reduce(Criteria.empty(), Criteria::or);
    }
}
