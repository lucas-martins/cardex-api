package com.cardex.api.deck;

import com.cardex.api.enumeration.DeckSection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeckListParserTest {

    private final DeckListParser parser = new DeckListParser();

    @Test
    void shouldParseHashSectionDeckList() {
        String list = """
                ##Pokémon - 16

                2 Shaymin-EX ROS 106
                3 Cosmog SUM 64
                1 Cosmoem SUM 65

                ##Trainer Cards - 33

                3 Professor Sycamore STS 114
                4 Ultra Ball PLF 122

                ##Energy - 11

                11 Psychic Energy HS 119
                """;

        List<DeckListLine> lines = parser.parse(list);

        assertEquals(6, lines.size());
        assertEquals(DeckSection.POKEMON, lines.get(0).section());
        assertEquals(2, lines.get(0).quantity());
        assertEquals("Shaymin EX", lines.get(0).name());
        assertEquals("ROS", lines.get(0).setAbbreviation());
        assertEquals("106", lines.get(0).cardNumber());
        assertEquals(DeckSection.TRAINER, lines.get(3).section());
        assertEquals(DeckSection.ENERGY, lines.get(5).section());
    }

    @Test
    void shouldParseColonSectionDeckListWithEnergySymbols() {
        String list = """
                Pokémon: 8
                3 Dreepy ASC 247
                1 Mega Golisopod ex DLR 11

                Trainer: 13
                3 Boss's Orders RCL 189
                1 N NVI 101

                Energy: 4
                4 Growing {G} Energy POR 86
                3 Basic {D} Energy MEE 15
                """;

        List<DeckListLine> lines = parser.parse(list);

        assertEquals(6, lines.size());
        assertEquals("Dreepy", lines.get(0).name());
        assertEquals("ASC", lines.get(0).setAbbreviation());
        assertEquals("N", lines.get(3).name());
        assertEquals("Growing Grass Energy", lines.get(4).name());
        assertEquals("Basic Darkness Energy", lines.get(5).name());
        assertEquals("15", lines.get(5).cardNumber());
        assertTrue(
                lines.stream().anyMatch(line ->
                        line.section() == DeckSection.ENERGY
                )
        );
    }
}
