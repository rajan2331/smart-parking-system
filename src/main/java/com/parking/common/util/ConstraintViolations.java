package com.parking.common.util;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.exception.ConstraintViolationException;

/** Helpers for turning database constraint violations into business errors. */
public final class ConstraintViolations {

    /** PostgreSQL SQLSTATE for "exclusion constraint violated". */
    private static final String EXCLUSION_VIOLATION = "23P01";
    private static final Pattern EXCLUSION_NAME = Pattern.compile("exclusion constraint \"([^\"]+)\"");

    private ConstraintViolations() {
    }

    /** Walks the cause chain and returns the violated constraint/index name, or null. */
    public static String findConstraintName(Throwable throwable) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve && cve.getConstraintName() != null) {
                return cve.getConstraintName();
            }
            // Hibernate doesn't extract names for exclusion constraints; read it from PostgreSQL's message
            if (t instanceof SQLException sql && EXCLUSION_VIOLATION.equals(sql.getSQLState())) {
                Matcher m = EXCLUSION_NAME.matcher(String.valueOf(sql.getMessage()));
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        return null;
    }
}
