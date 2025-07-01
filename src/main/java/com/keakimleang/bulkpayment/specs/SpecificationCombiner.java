package com.keakimleang.bulkpayment.specs;

import java.util.*;
import org.springframework.data.relational.core.query.*;

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
