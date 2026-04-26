/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.mcp.internal.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;

/**
 * Tests for {@link FuzzyItemMatcher}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class FuzzyItemMatcherTest {

    @Mock
    @Nullable
    MetadataRegistry metadataRegistry;

    private Item mockItem(String name, String label) {
        Item item = mock(Item.class);
        lenient().when(item.getName()).thenReturn(name);
        lenient().when(item.getLabel()).thenReturn(label);
        return item;
    }

    @Test
    void testExactNameScoresHigh() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(metadataRegistry);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double score = matcher.score(item, "Kitchen_Light");
        assertTrue(score > 0.9, "Exact name match should score high, got: " + score);
    }

    @Test
    void testExactLabelScoresHigh() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(metadataRegistry);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double score = matcher.score(item, "kitchen light");
        assertTrue(score > 0.9, "Exact label match should score high, got: " + score);
    }

    @Test
    void testTypoTolerance() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(metadataRegistry);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double score = matcher.score(item, "kichen light");
        assertTrue(score > 0.7, "Typo should still match with reasonable score, got: " + score);
    }

    @Test
    void testWordReordering() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(metadataRegistry);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double score = matcher.score(item, "light kitchen");
        assertTrue(score > 0.7, "Word reordering should still score well, got: " + score);
    }

    @Test
    void testUnrelatedScoresLow() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(metadataRegistry);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double score = matcher.score(item, "garage door");
        assertTrue(score < 0.6, "Unrelated query should score low, got: " + score);
    }

    @Test
    void testEmptyQueryReturnsOne() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        assertEquals(1.0, matcher.score(item, ""));
        assertEquals(1.0, matcher.score(item, "  "));
    }

    @Test
    void testSynonymMatch() {
        MetadataRegistry registry = metadataRegistry;
        assertNotNull(registry);
        MetadataKey key = new MetadataKey("synonyms", "Kitchen_Light");
        when(registry.get(key)).thenReturn(new Metadata(key, "overhead light, ceiling lamp", null));

        FuzzyItemMatcher matcher = new FuzzyItemMatcher(registry);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double score = matcher.score(item, "overhead light");
        assertTrue(score > 0.9, "Synonym match should score high, got: " + score);
    }

    @Test
    void testNullMetadataRegistry() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double score = matcher.score(item, "kitchen light");
        assertTrue(score > 0.9, "Should still match on name/label without registry, got: " + score);
    }

    @Test
    void testCaseInsensitive() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(metadataRegistry);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double score = matcher.score(item, "KITCHEN LIGHT");
        assertTrue(score > 0.9, "Case should not matter, got: " + score);
    }

    @Test
    void testNameWithUnderscoresTokenized() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(metadataRegistry);
        Item item = mockItem("Living_Room_Dimmer", "Living Room Dimmer");
        double score = matcher.score(item, "living room");
        assertTrue(score > 0.7, "Underscored name should tokenize, got: " + score);
    }

    @Test
    void testScoreBounds() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(metadataRegistry);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        for (String query : new String[] { "kitchen", "xyz", "light", "garage door sensor" }) {
            double score = matcher.score(item, query);
            assertTrue(score >= 0.0 && score <= 1.0, "Score should be in [0,1], got: " + score + " for: " + query);
        }
    }

    @Test
    void testMultiTokenAveraging() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(metadataRegistry);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double twoToken = matcher.score(item, "kitchen light");
        double oneMatchOneNot = matcher.score(item, "kitchen garage");
        assertTrue(twoToken > oneMatchOneNot, "Two matching tokens should beat one matching + one unrelated");
    }

    @Test
    void testCommaSeparatedSynonyms() {
        MetadataRegistry registry = metadataRegistry;
        assertNotNull(registry);
        MetadataKey key = new MetadataKey("synonyms", "Guest_Room");
        when(registry.get(key)).thenReturn(new Metadata(key, "casita, spare room, guest suite", null));

        FuzzyItemMatcher matcher = new FuzzyItemMatcher(registry);
        Item item = mockItem("Guest_Room", "Guest Room");
        double score = matcher.score(item, "casita");
        assertTrue(score > 0.8, "Comma-separated synonym should match, got: " + score);
    }

    @Test
    void testNaturalLanguagePartialRoom() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("F1_DansOffice_Light", "Dan's Office Light");
        double score = matcher.score(item, "office light");
        assertTrue(score > 0.7, "Partial room name should match, got: " + score);
    }

    @Test
    void testNaturalLanguageAbbreviated() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("LivingRoom_Temperature", "Living Room Temperature");
        double score = matcher.score(item, "living room temp");
        assertTrue(score > 0.65, "Abbreviated word should partially match, got: " + score);
    }

    @Test
    void testNaturalLanguageSwappedLetters() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("Bedroom_Light", "Bedroom Light");
        double score = matcher.score(item, "bedrom light");
        assertTrue(score > 0.7, "Swapped/missing letter should still match, got: " + score);
    }

    @Test
    void testNaturalLanguageColloquialName() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("MasterBedroom_Light", "Master Bedroom Light");
        double score = matcher.score(item, "master bedroom");
        assertTrue(score > 0.65, "Room name without device type should partially match, got: " + score);
    }

    @Test
    void testNaturalLanguagePossessive() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("F1_DansOffice_Light", "Dan's Office Light");
        double score = matcher.score(item, "dan's office");
        assertTrue(score > 0.7, "Possessive form should match label, got: " + score);
    }

    @Test
    void testNaturalLanguagePossessiveWithoutApostrophe() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("F1_DansOffice_Light", "Dan's Office Light");
        double score = matcher.score(item, "dans office light");
        assertTrue(score > 0.7, "Possessive without apostrophe should match via token split, got: " + score);
    }

    @Test
    void testNaturalLanguagePluralVsSingular() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("Kitchen_Lights", "Kitchen Lights");
        double score = matcher.score(item, "kitchen light");
        assertTrue(score > 0.8, "Singular vs plural should score well with JW similarity, got: " + score);
    }

    @Test
    void testNaturalLanguageExtraWords() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("Porch_Light", "Porch Light");
        double extraWords = matcher.score(item, "the front porch light");
        double exact = matcher.score(item, "porch light");
        assertTrue(extraWords > 0.5, "Extra words should still produce a reasonable score, got: " + extraWords);
        assertTrue(exact > extraWords, "Exact should beat query with extra noise words");
    }

    @Test
    void testNaturalLanguageSingleWordDevice() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("Thermostat_Temperature", "Thermostat Temperature");
        double score = matcher.score(item, "thermostat");
        assertTrue(score > 0.65, "Single device word should partially match, got: " + score);
    }

    @Test
    void testCorrectItemRanksHigherThanWrongRoom() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item kitchenLight = mockItem("Kitchen_Light", "Kitchen Light");
        Item bedroomLight = mockItem("Bedroom_Light", "Bedroom Light");
        double kitchenScore = matcher.score(kitchenLight, "kitchen light");
        double bedroomScore = matcher.score(bedroomLight, "kitchen light");
        assertTrue(kitchenScore > bedroomScore,
                "Kitchen light should rank higher than bedroom light for 'kitchen light' query");
    }

    @Test
    void testCorrectItemRanksHigherWithTypo() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item kitchenLight = mockItem("Kitchen_Light", "Kitchen Light");
        Item garageLight = mockItem("Garage_Light", "Garage Light");
        double kitchenScore = matcher.score(kitchenLight, "kichen lite");
        double garageScore = matcher.score(garageLight, "kichen lite");
        assertTrue(kitchenScore > garageScore,
                "Kitchen light should still rank higher than garage light even with typos");
    }

    @Test
    void testSynonymBeatsUnrelatedItem() {
        MetadataRegistry registry = metadataRegistry;
        assertNotNull(registry);
        MetadataKey tvKey = new MetadataKey("synonyms", "LivingRoom_TV");
        when(registry.get(tvKey)).thenReturn(new Metadata(tvKey, "television, telly, the tube", null));

        FuzzyItemMatcher matcher = new FuzzyItemMatcher(registry);
        Item tv = mockItem("LivingRoom_TV", "Living Room TV");
        Item light = mockItem("LivingRoom_Light", "Living Room Light");
        double tvScore = matcher.score(tv, "telly");
        double lightScore = matcher.score(light, "telly");
        assertTrue(tvScore > lightScore, "Synonym 'telly' should favor TV over light");
    }

    @Test
    void testFloorPrefixedItemName() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("F2_Bathroom_Fan", "Upstairs Bathroom Fan");
        double labelMatch = matcher.score(item, "bathroom fan");
        assertTrue(labelMatch > 0.7, "Should match label ignoring floor prefix, got: " + labelMatch);
    }

    @Test
    void testHyphenatedLabel() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("HVAC_SetPoint", "HVAC Set-Point");
        double score = matcher.score(item, "hvac setpoint");
        assertTrue(score > 0.7, "Hyphenated label should tokenize and match, got: " + score);
    }

    @Test
    void testCompletelyWrongQueryScoresBelowThreshold() {
        FuzzyItemMatcher matcher = new FuzzyItemMatcher(null);
        Item item = mockItem("Kitchen_Light", "Kitchen Light");
        double score = matcher.score(item, "play spotify");
        assertTrue(score < 0.65, "Completely unrelated query should be below MIN_SCORE 0.65, got: " + score);
    }
}
