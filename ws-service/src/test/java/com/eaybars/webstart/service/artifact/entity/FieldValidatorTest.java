package com.eaybars.webstart.service.artifact.entity;

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.fail;

public class FieldValidatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void notNullTest() {
        FieldValidator.NON_NULL.accept("field", null);
        FieldValidator.NON_NULL.accept("field", JsonValue.NULL);

        FieldValidator.NON_NULL.accept("field", "text");
    }

    @Test
    public void integralValueTest() {
        FieldValidator.INTEGRAL_VALUE.accept("field", 1);
        FieldValidator.INTEGRAL_VALUE.accept("field", 1L);
        FieldValidator.INTEGRAL_VALUE.accept("field", BigInteger.ONE);
        FieldValidator.INTEGRAL_VALUE.accept("field", new BigDecimal(1.0).setScale(0));
        FieldValidator.INTEGRAL_VALUE.accept("field", Json.createObjectBuilder().add("field", 1).build().getJsonNumber("field"));
        FieldValidator.INTEGRAL_VALUE.accept("field", null);
        FieldValidator.INTEGRAL_VALUE.accept("field", JsonValue.NULL);

        try {
            FieldValidator.INTEGRAL_VALUE.accept("field", 1.0);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            FieldValidator.INTEGRAL_VALUE.accept("field", "text");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void numberValueTest() {
        FieldValidator.NUMBER_VALUE.accept("field", 1);
        FieldValidator.NUMBER_VALUE.accept("field", 1L);
        FieldValidator.NUMBER_VALUE.accept("field", 1.0);
        FieldValidator.NUMBER_VALUE.accept("field", 1.5);
        FieldValidator.NUMBER_VALUE.accept("field", BigInteger.ONE);
        FieldValidator.NUMBER_VALUE.accept("field", BigDecimal.ONE);
        FieldValidator.NUMBER_VALUE.accept("field", new BigDecimal(1.0).setScale(0));
        FieldValidator.NUMBER_VALUE.accept("field", new BigDecimal(1.3));
        FieldValidator.NUMBER_VALUE.accept("field", Json.createObjectBuilder().add("field", 1).build().getJsonNumber("field"));
        FieldValidator.NUMBER_VALUE.accept("field", Json.createObjectBuilder().add("field", 1.3).build().getJsonNumber("field"));
        FieldValidator.NUMBER_VALUE.accept("field", null);
        FieldValidator.NUMBER_VALUE.accept("field", JsonValue.NULL);

        try {
            FieldValidator.NUMBER_VALUE.accept("field", "text");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void booleanValuetest() {
        FieldValidator.BOOLEAN_VALUE.accept("field", true);
        FieldValidator.BOOLEAN_VALUE.accept("field", Boolean.TRUE);
        FieldValidator.BOOLEAN_VALUE.accept("field", false);
        FieldValidator.BOOLEAN_VALUE.accept("field", Boolean.FALSE);
        FieldValidator.BOOLEAN_VALUE.accept("field", null);
        FieldValidator.BOOLEAN_VALUE.accept("field", JsonValue.NULL);

        try {
            FieldValidator.BOOLEAN_VALUE.accept("field", 1);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            FieldValidator.BOOLEAN_VALUE.accept("field", "text");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void stringValuetest() {
        FieldValidator.STRING_VALUE.accept("field", "");
        FieldValidator.STRING_VALUE.accept("field", null);
        FieldValidator.STRING_VALUE.accept("field", JsonValue.NULL);
        FieldValidator.STRING_VALUE.accept("field", "text");
        FieldValidator.STRING_VALUE.accept("field", Json.createObjectBuilder().add("field", "text").build().get("field"));

        try {
            FieldValidator.STRING_VALUE.accept("field", 1);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            FieldValidator.STRING_VALUE.accept("field", true);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void nonEmptyStringValuetest() {
        try {
            FieldValidator.NON_EMPTY_STRING_VALUE.accept("field", "");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            FieldValidator.NON_EMPTY_STRING_VALUE.accept("field", null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            FieldValidator.NON_EMPTY_STRING_VALUE.accept("field", JsonValue.NULL);
            fail();
        } catch (IllegalArgumentException e) {
        }

        FieldValidator.NON_EMPTY_STRING_VALUE.accept("field", "text");
        FieldValidator.NON_EMPTY_STRING_VALUE.accept("field", Json.createObjectBuilder().add("field", "text").build().get("field"));

        try {
            FieldValidator.NON_EMPTY_STRING_VALUE.accept("field", 1);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            FieldValidator.NON_EMPTY_STRING_VALUE.accept("field", true);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void uriValueTest() {
        FieldValidator.URI_VALUE.accept("field", null);
        FieldValidator.URI_VALUE.accept("field", "");
        FieldValidator.URI_VALUE.accept("field", "/");
        FieldValidator.URI_VALUE.accept("field", "text");
        FieldValidator.URI_VALUE.accept("field", "/text");
        FieldValidator.URI_VALUE.accept("field", "/text?q=1&t=2");
        FieldValidator.URI_VALUE.accept("field", Json.createObjectBuilder().add("field", "/text?q=1&t=2").build().get("field"));

        try {
            FieldValidator.URI_VALUE.accept("field", "invalid uri");
            fail();
        } catch (IllegalArgumentException e) {
        }

    }

}