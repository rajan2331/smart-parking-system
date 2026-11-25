package com.parking.common.util;

import org.hibernate.exception.ConstraintViolationException;

/** Helpers for turning database constraint violations into business errors. */
public final class ConstraintViolations {

    private ConstraintViolations() {
    }

    /** Walks the cause chain and returns the violated constraint/index name, or null. */
    public static String findConstraintName(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof ConstraintViolationException cve) {
                return cve.getConstraintName();
            }
            throwable = throwable.getCause();
        }
        return null;
    }
}
