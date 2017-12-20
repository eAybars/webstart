package com.eaybars.webstart.service.artifact.entity;

import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Objects;
import java.util.function.BiConsumer;

public interface FieldValidator extends BiConsumer<String, Object>, Serializable {

    FieldValidator ANY = (k, v) -> {
    };

    FieldValidator READ_ONLY = (k, v) -> {
        throw new UnsupportedOperationException("Field " + k + " is read only");
    };

    FieldValidator NON_NULL = (k, v) -> {
        if (v == null || JsonValue.NULL.equals(v)) {
            throw new IllegalArgumentException("Field " + k + " must be non null");
        }
    };

    FieldValidator INTEGRAL_VALUE = (k, v) -> {
        try {
            if (v == null ||
                    JsonValue.NULL.equals(v) ||
                    v.getClass().equals(Integer.class) ||
                    v.getClass().equals(Long.class) ||
                    v.getClass().equals(BigInteger.class) ||
                    (v.getClass().equals(BigDecimal.class) && ((BigDecimal)v).scale() == 0) ||
                    ((JsonNumber) v).isIntegral()) {
                return;
            }
        } catch (ClassCastException e) {
        }
        throw new IllegalArgumentException("Field " + k + " must be an Integral number, suggested value: " + v);
    };

    FieldValidator NUMBER_VALUE = (k, v) -> {
        if (!(v == null || JsonValue.NULL.equals(v) || Number.class.isAssignableFrom(v.getClass()) || v instanceof JsonNumber)) {
            throw new IllegalArgumentException("Field " + k + " must be a number, suggested value: " + v);
        }
    };


    FieldValidator BOOLEAN_VALUE = (k, v) -> {
        if (!(v == null || JsonValue.NULL.equals(v) || Boolean.class.equals(v.getClass()) || JsonValue.TRUE.equals(v) || JsonValue.FALSE.equals(v))) {
            throw new IllegalArgumentException("Field " + k + " must be a boolean value, suggested value: " + v);
        }
    };


    FieldValidator STRING_VALUE = (k, v) -> {
        if (!(v == null || String.class.equals(v.getClass()) || v instanceof JsonString || v.equals(JsonValue.NULL))) {
            throw new IllegalArgumentException("Field " + k + " must be a String value, suggested value: " + v);
        }
    };

    FieldValidator NON_EMPTY_STRING_VALUE = (k, v) -> {
        NON_NULL.andThen(STRING_VALUE).accept(k,v);
        if ("".equals(v)) {
            throw new IllegalArgumentException("Field " + k + " must be a non empty String value,");
        }
    };


    FieldValidator URI_VALUE = (k, v) -> {
        if (v != null && !JsonValue.NULL.equals(v)) {
            String str = v instanceof JsonString ? ((JsonString)v).getString() : v.toString();
            URI.create(str);
        }
    };

    @Override
    default FieldValidator andThen(BiConsumer<? super String, ? super Object> after) {
        Objects.requireNonNull(after);

        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }
}
