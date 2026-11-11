package com.cardex.api.pokemon;

import java.util.Arrays;
import java.util.List;

public final class CardTypes {

    private CardTypes() {
    }

    public static String join(List<String> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }

        List<String> normalized = types.stream()
                .filter(type -> type != null && !type.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (normalized.isEmpty()) {
            return null;
        }

        return String.join(",", normalized);
    }

    public static List<String> split(String types) {
        if (types == null || types.isBlank()) {
            return List.of();
        }

        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(type -> !type.isEmpty())
                .toList();
    }

    public static boolean contains(String storedTypes, String type) {
        if (type == null || type.isBlank()) {
            return true;
        }

        String needle = type.trim().toLowerCase();

        return split(storedTypes).stream()
                .anyMatch(value -> value.equalsIgnoreCase(needle));
    }
}
