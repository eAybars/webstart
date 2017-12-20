package com.eaybars.webstart.service.artifact.entity;

import org.junit.Test;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.Assert.assertSame;

public class FieldValidationTest {

    @Test
    public void test() {
        FieldValidation.install(Collection.class, "col", FieldValidator.NON_NULL);
        FieldValidation.install(Set.class, "col", FieldValidator.NON_EMPTY_STRING_VALUE);
        FieldValidation.install(Set.class, "set", FieldValidator.NON_EMPTY_STRING_VALUE);
        FieldValidation.install(SortedSet.class, "col", FieldValidator.INTEGRAL_VALUE);
        FieldValidation.install(SortedSet.class, "set", FieldValidator.INTEGRAL_VALUE);

        assertSame(FieldValidator.NON_NULL, FieldValidation.getValidator(Collection.class, "col"));
        assertSame(FieldValidator.ANY, FieldValidation.getValidator(Collection.class, "col2"));

        assertSame(FieldValidator.NON_NULL, FieldValidation.getValidator(Set.class, "col"));
        assertSame(FieldValidator.NON_EMPTY_STRING_VALUE, FieldValidation.getValidator(Set.class, "set"));
        assertSame(FieldValidator.ANY, FieldValidation.getValidator(Set.class, "col2"));

        assertSame(FieldValidator.NON_NULL, FieldValidation.getValidator(SortedSet.class, "col"));
        assertSame(FieldValidator.NON_EMPTY_STRING_VALUE, FieldValidation.getValidator(SortedSet.class, "set"));
        assertSame(FieldValidator.ANY, FieldValidation.getValidator(SortedSet.class, "col2"));
    }

}