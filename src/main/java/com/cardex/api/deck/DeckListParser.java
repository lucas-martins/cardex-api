package com.cardex.api.deck;

import com.cardex.api.enumeration.DeckSection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DeckListParser {

    private static final Pattern SECTION_PATTERN = Pattern.compile(
            "^\\s*#*\\s*(pokemon|trainer(?:\\s+cards)?|energy)\\s*[:\\-–]?\\s*\\d*\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CARD_LINE_PATTERN = Pattern.compile(
            "^\\s*(\\d+)\\s+(.+?)\\s+([A-Za-z0-9]{2,6})\\s+([A-Za-z0-9]+)\\s*$"
    );

    public List<DeckListLine> parse(String listText) {
        if (listText == null || listText.isBlank()) {
            return List.of();
        }

        String[] lines = listText.replace("\r\n", "\n").split("\n");
        List<DeckListLine> parsed = new ArrayList<>();
        DeckSection currentSection = DeckSection.POKEMON;

        for (int index = 0; index < lines.length; index++) {
            String rawLine = lines[index];
            String trimmed = rawLine.trim();

            if (trimmed.isEmpty()) {
                continue;
            }

            Matcher sectionMatcher = SECTION_PATTERN.matcher(trimmed);
            if (sectionMatcher.matches()) {
                currentSection = resolveSection(sectionMatcher.group(1));
                continue;
            }

            String normalized = normalizeEnergySymbols(trimmed);
            Matcher cardMatcher = CARD_LINE_PATTERN.matcher(normalized);

            if (!cardMatcher.matches()) {
                continue;
            }

            int quantity = Integer.parseInt(cardMatcher.group(1));
            String name = normalizeCardName(cardMatcher.group(2));
            String setAbbreviation = cardMatcher.group(3).toUpperCase(Locale.ROOT);
            String cardNumber = normalizeCardNumber(cardMatcher.group(4));

            parsed.add(
                    new DeckListLine(
                            index + 1,
                            rawLine,
                            currentSection,
                            quantity,
                            name,
                            setAbbreviation,
                            cardNumber
                    )
            );
        }

        return parsed;
    }

    private DeckSection resolveSection(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);

        if (normalized.startsWith("trainer")) {
            return DeckSection.TRAINER;
        }

        if (normalized.startsWith("energy")) {
            return DeckSection.ENERGY;
        }

        return DeckSection.POKEMON;
    }

    private String normalizeEnergySymbols(String line) {
        return line
                .replace("{G}", "Grass")
                .replace("{R}", "Fire")
                .replace("{W}", "Water")
                .replace("{L}", "Lightning")
                .replace("{P}", "Psychic")
                .replace("{F}", "Fighting")
                .replace("{D}", "Darkness")
                .replace("{M}", "Metal")
                .replace("{Y}", "Fairy")
                .replace("{C}", "Colorless")
                .replace("{N}", "Dragon")
                .replace("{E}", "Energy");
    }

    private String normalizeCardName(String name) {
        return name
                .trim()
                .replace("-EX", " EX")
                .replace("-GX", " GX")
                .replace("-VMAX", " VMAX")
                .replace("-VSTAR", " VSTAR")
                .replace("-V", " V")
                .replaceAll("\\s+", " ");
    }

    private String normalizeCardNumber(String cardNumber) {
        String trimmed = cardNumber.trim();

        if (trimmed.matches("\\d+")) {
            return String.valueOf(Integer.parseInt(trimmed));
        }

        return trimmed;
    }
}
