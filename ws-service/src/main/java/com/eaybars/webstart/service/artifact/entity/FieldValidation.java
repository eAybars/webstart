package com.eaybars.webstart.service.artifact.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

public class FieldValidation {
    private static Map<Class<?>, Map<String, FieldValidator>> VALIDATORS = new HashMap<>();

    public static void install(Class<?> type, String field, FieldValidator validator) {
        install(type, singletonMap(field, validator));
    }

    public static void install(Class<?> type, Map<String, FieldValidator> validators) {
        Map<String, FieldValidator> existingValidators = VALIDATORS.computeIfAbsent(type, k -> new HashMap<>());
        existingValidators.putAll(validators);

        LinkedList<Class<?>> stack = new LinkedList<>();
        addToStack(type, stack);

        while (!stack.isEmpty()) {
            Class<?> current = stack.pop();
            existingValidators.keySet().removeAll(
                    VALIDATORS.getOrDefault(current, emptyMap()).keySet());
            addToStack(current, stack);
        }
    }

    public static FieldValidator getValidator(Class<?> type, String field) {
        FieldValidator validator = null;

        LinkedList<Class<?>> stack = new LinkedList<>();
        stack.add(type);
        while (!stack.isEmpty() && validator == null) {
            Class<?> current = stack.pop();
            validator = VALIDATORS.getOrDefault(current, emptyMap()).get(field);
            if (validator == null) {
                addToStack(current, stack);
            }
        }
        return validator == null ? FieldValidator.ANY : validator;
    }

    private static void addToStack(Class<?> type, List<Class<?>> stack) {
        if (type != null && !Object.class.equals(type)) {
            stack.add(type.getSuperclass());
            Stream.of(type.getInterfaces()).collect(Collectors.toCollection(() -> stack));
        }
    }
}
