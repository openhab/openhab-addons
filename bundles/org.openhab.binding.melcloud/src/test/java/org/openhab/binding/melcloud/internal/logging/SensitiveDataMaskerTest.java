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
package org.openhab.binding.melcloud.internal.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.melcloud.internal.mock.FileReader;

/**
 * Unit tests for {@link SensitiveDataMasker}, including regression tests over the synthetic MELCloud Home response
 * fixtures {@code src/test/resources/ata.json} and {@code src/test/resources/atw-ftc7.json}.
 *
 * <p>
 * The fixtures mirror the API's response shape but deliberately contain recognizable, unmasked sentinel values, so
 * these tests fail if {@link SensitiveDataMasker#maskJson(String)} stops rewriting them. A fixture that already
 * carried masked placeholders could not tell masking apart from doing nothing.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SensitiveDataMaskerTest {

    private static final String ATA_FIXTURE = "src/test/resources/ata.json";
    private static final String ATW_FIXTURE = "src/test/resources/atw-ftc7.json";

    private static final String USER_ID = "11111111-2222-3333-4444-555566667777";
    private static final String BUILDING_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000";
    private static final String SYSTEM_ID = "99999999-8888-7777-6666-555544443333";
    private static final String UNIT_ID = "0f0e0d0c-0b0a-0908-0706-050403020100";
    private static final String MAC_ADDRESS = "001122aabbcc";
    private static final String FIRST_NAME = "SyntheticFirst";
    private static final String LAST_NAME = "SyntheticLast";
    private static final String EMAIL = "synthetic.owner@example.invalid";

    @Test
    void whenIdIsLongerThanFourCharactersThenMaskIdKeepsLastFourVisible() {
        // Arrange
        String id = "298f815e-79cc-45dc-81ff-5add6771ac62";

        // Act
        String masked = SensitiveDataMasker.maskId(id);

        // Assert
        assertEquals("...ac62", masked);
    }

    @ParameterizedTest
    @CsvSource({ "''", "a", "abcd" })
    void whenIdIsFourCharactersOrShorterThenMaskIdFullyRedacts(String id) {
        // Act
        String masked = SensitiveDataMasker.maskId(id);

        // Assert
        assertEquals("***", masked);
    }

    @Test
    void whenJsonIsBlankThenMaskJsonReturnsItUnchanged() {
        // Arrange
        String blank = "   ";

        // Act
        String masked = SensitiveDataMasker.maskJson(blank);

        // Assert
        assertEquals(blank, masked);
    }

    @ParameterizedTest
    @CsvSource({ "email, bernd.w@ymann.de", "firstname, Bernd", "lastname, Weymann", "ownerName, Bernd Weymann",
            "deviceName, Loft", "buildingName, Townhouse", "givenDisplayName, Loft", "contextKey, abc123SESSIONTOKEN",
            "hash, abc123WEBSOCKETHASH" })
    void whenJsonContainsAFullyRedactedKeyThenMaskJsonHidesTheValueCompletely(String key, String value) {
        // Arrange
        String json = "{\"" + key + "\": \"" + value + "\"}";

        // Act
        String masked = SensitiveDataMasker.maskJson(json);

        // Assert
        assertEquals("{\"" + key + "\": \"***\"}", masked);
        assertFalse(masked.contains(value));
    }

    @Test
    void whenJsonContainsAGenericNameKeyThenMaskJsonLeavesItUntouched() {
        // Arrange - "name" is deliberately excluded from the generic redaction: the MELCloud Home API reuses it
        // as a technical field-name in settings arrays (e.g. {"name": "OperationMode", "value": "Cool"}).
        String json = "{\"name\": \"OperationMode\", \"value\": \"Cool\"}";

        // Act
        String masked = SensitiveDataMasker.maskJson(json);

        // Assert
        assertEquals(json, masked);
    }

    @Test
    void whenMaskAdditionalFieldIsUsedThenTheGivenKeyIsRedactedOnTopOfMaskJson() {
        // Arrange - the legacy login response's "Name" (account holder's real name) is redacted this way, since
        // that response shape does not have the settings-array "name" collision.
        String json = "{\"Name\": \"Bernd Weymann\", \"ContextKey\": \"tok123456\"}";

        // Act
        String masked = SensitiveDataMasker.maskAdditionalField(SensitiveDataMasker.maskJson(json), "Name");

        // Assert
        assertEquals("{\"Name\": \"***\", \"ContextKey\": \"***\"}", masked);
    }

    @ParameterizedTest
    @CsvSource({ "id, 298f815e-79cc-45dc-81ff-5add6771ac62", "deviceID, 298f815e-79cc-45dc-81ff-5add6771ac62",
            "unitId, 298f815e-79cc-45dc-81ff-5add6771ac62", "systemId, 88376d24-d151-41f2-aacf-e28593144d91",
            "macAddress, b8b7f1c2fd5e", "serialNumber, SN1234567890", "connectedInterfaceIdentifier, b8b7f1c2fd5e" })
    void whenJsonContainsAPartiallyMaskedKeyThenMaskJsonKeepsOnlyTheLastFourCharacters(String key, String value) {
        // Arrange
        String json = "{\"" + key + "\": \"" + value + "\"}";

        // Act
        String masked = SensitiveDataMasker.maskJson(json);

        // Assert
        assertEquals("{\"" + key + "\": \"" + SensitiveDataMasker.maskId(value) + "\"}", masked);
        assertFalse(masked.contains(value));
    }

    @Test
    void whenJsonContainsANumericIdThenMaskJsonPartiallyMasksItToo() {
        // Arrange
        String json = "{\"BuildingID\": 65432}";

        // Act
        String masked = SensitiveDataMasker.maskJson(json);

        // Assert - BuildingID is a partial-mask (identifier) key, not a full-redact key, so the last 4 digits
        // stay visible, same as any other identifier.
        assertEquals("{\"BuildingID\": \"...5432\"}", masked);
    }

    @Test
    void whenJsonContainsOnlyUnrelatedKeysThenMaskJsonLeavesThemUntouched() {
        // Arrange
        String json = "{\"OperationMode\": \"Cool\", \"rssi\": -60, \"hasCoolOperationMode\": true}";

        // Act
        String masked = SensitiveDataMasker.maskJson(json);

        // Assert
        assertEquals(json, masked);
    }

    @Test
    void whenUrlContainsAGuidThenMaskGuidsInUrlMasksOnlyTheGuid() {
        // Arrange
        String url = "https://mobile.bff.melcloudhome.com/monitor/ataunit/298f815e-79cc-45dc-81ff-5add6771ac62";

        // Act
        String masked = SensitiveDataMasker.maskGuidsInUrl(url);

        // Assert
        assertEquals("https://mobile.bff.melcloudhome.com/monitor/ataunit/...ac62", masked);
    }

    @Test
    void whenUrlContainsNoGuidThenMaskGuidsInUrlReturnsItUnchanged() {
        // Arrange
        String url = "https://mobile.bff.melcloudhome.com/context";

        // Act
        String masked = SensitiveDataMasker.maskGuidsInUrl(url);

        // Assert
        assertEquals(url, masked);
    }

    @Test
    void whenMaskingTheSyntheticAtaFixtureThenNoPersonalDataOrIdentifiersRemain() {
        // Arrange
        String raw = FileReader.readFileInString(ATA_FIXTURE);
        assertTrue(raw.contains(FIRST_NAME), "the fixture must carry the unmasked sentinel this test asserts on");
        assertTrue(raw.contains(MAC_ADDRESS), "the fixture must carry the unmasked sentinel this test asserts on");

        // Act
        String masked = SensitiveDataMasker.maskJson(raw);

        // Assert - personal data is fully redacted, not merely absent from the fixture
        assertFalse(masked.contains(FIRST_NAME), "first name must not survive masking");
        assertFalse(masked.contains(LAST_NAME), "last name must not survive masking");
        assertFalse(masked.contains(EMAIL), "e-mail address must not survive masking");
        assertFalse(masked.contains("SyntheticLoft"), "device label must not survive masking");
        assertTrue(masked.contains("\"givenDisplayName\": \"***\""));

        // Assert - identifiers survive only as their masked suffix
        assertFalse(masked.contains(USER_ID), "user id must not survive masking");
        assertFalse(masked.contains(BUILDING_ID), "building id must not survive masking");
        assertFalse(masked.contains(SYSTEM_ID), "system id must not survive masking");
        assertFalse(masked.contains(UNIT_ID), "unit id must not survive masking");
        assertFalse(masked.contains(MAC_ADDRESS), "MAC address must not survive masking");
        assertTrue(masked.contains("\"id\": \"" + SensitiveDataMasker.maskId(USER_ID) + "\""));
        assertTrue(masked.contains("\"systemId\": \"" + SensitiveDataMasker.maskId(SYSTEM_ID) + "\""));
        assertTrue(masked
                .contains("\"connectedInterfaceIdentifier\": \"" + SensitiveDataMasker.maskId(MAC_ADDRESS) + "\""));

        // Assert - non-sensitive structural data is preserved for troubleshooting
        assertTrue(masked.contains("\"hasCoolOperationMode\": true"));
        assertTrue(masked.contains("\"numberOfFanSpeeds\": 5"));
        assertTrue(masked.contains("\"rssi\": -60"));
        assertTrue(masked.contains("\"OperationMode\""));
    }

    @Test
    void whenMaskingTheSyntheticAtwFixtureThenNoIdentifyingFieldValueRemains() {
        // Arrange
        String raw = FileReader.readFileInString(ATW_FIXTURE);
        assertTrue(raw.contains(FIRST_NAME), "the fixture must carry the unmasked sentinel this test asserts on");
        assertTrue(raw.contains(MAC_ADDRESS), "the fixture must carry the unmasked sentinel this test asserts on");

        // Act
        String masked = SensitiveDataMasker.maskJson(raw);

        // Assert - every recognized sensitive key was rewritten to a masked placeholder
        assertFalse(masked.contains(FIRST_NAME), "firstname must be redacted");
        assertFalse(masked.contains(LAST_NAME), "lastname must be redacted");
        assertFalse(masked.contains(EMAIL), "email must be redacted");
        assertFalse(masked.contains("SyntheticHeatPump"), "device label must be redacted");
        assertTrue(masked.contains("\"firstname\": \"***\""));
        assertTrue(masked.contains("\"lastname\": \"***\""));
        assertTrue(masked.contains("\"email\": \"***\""));
        assertTrue(masked.contains("\"givenDisplayName\": \"***\""));

        // Assert - identifiers keep only their masked suffix
        assertFalse(masked.contains(USER_ID), "user id must not survive masking");
        assertFalse(masked.contains(BUILDING_ID), "building id must not survive masking");
        assertFalse(masked.contains(UNIT_ID), "unit id must not survive masking");
        assertFalse(masked.contains(MAC_ADDRESS), "macAddress must not survive masking");
        assertTrue(masked.contains("\"macAddress\": \"" + SensitiveDataMasker.maskId(MAC_ADDRESS) + "\""));

        // Assert - non-sensitive structural/technical data is preserved
        assertTrue(masked.contains("\"ftcModel\": \"ftC7\""));
        assertTrue(masked.contains("\"hasHotWater\": true"));
        assertTrue(masked.contains("\"rssi\": -55"));
    }
}
