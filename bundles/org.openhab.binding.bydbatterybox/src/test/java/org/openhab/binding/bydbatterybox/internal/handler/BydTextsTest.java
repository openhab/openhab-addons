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
package org.openhab.binding.bydbatterybox.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bydbatterybox.internal.Fixtures;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;
import org.openhab.binding.bydbatterybox.internal.protocol.BydCommunicationException;
import org.openhab.binding.bydbatterybox.internal.protocol.BydDecoder;

/**
 * Tests {@link BydTexts} with the texts of the binding's default i18n file.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class BydTextsTest {

    private final Properties properties = new Properties();
    private final Set<String> missingKeys = new HashSet<>();
    private final BydTexts texts = new BydTexts(this::lookup);

    @BeforeEach
    void loadTranslations() throws IOException {
        try (InputStream in = BydTextsTest.class.getResourceAsStream("/OH-INF/i18n/bydbatterybox.properties")) {
            assertNotNull(in);
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    private String lookup(String key, String defaultText, Object... arguments) {
        String text = properties.getProperty(key);
        if (text == null) {
            missingKeys.add(key);
            text = defaultText;
        }
        return MessageFormat.format(text, arguments);
    }

    @Test
    void systemErrors() {
        assertEquals("None", texts.systemErrors(0));
        assertEquals("Cell imbalance, Cell overvoltage", texts.systemErrors(1 << 6 | 1 << 10));
        assertTrue(missingKeys.isEmpty(), () -> "missing i18n keys " + missingKeys);
    }

    @Test
    void towerWarnings() {
        assertEquals("None", texts.towerWarnings(0, 0, 0));
        assertEquals("Cell imbalance", texts.towerWarnings(1 << 4, 0, 0));
        assertEquals("Battery overvoltage, Air switch abnormal", texts.towerWarnings(1 | 1 << 15, 0, 0));
        // the second word has the meaning of the first, the third its own; each text is listed once
        assertEquals("Battery overvoltage, Cell imbalance, Main circuit failure",
                texts.towerWarnings(1, 1 | 1 << 4, 1 | 1 << 12));
        assertTrue(missingKeys.isEmpty(), () -> "missing i18n keys " + missingKeys);
    }

    @Test
    void towerErrors() {
        assertEquals("None", texts.towerErrors(0));
        assertEquals("Main relay failure, Functional safety failure", texts.towerErrors(1 << 8 | 1 << 15));
        assertTrue(missingKeys.isEmpty(), () -> "missing i18n keys " + missingKeys);
    }

    private static EventRecord event(int unit, int code, String hex) {
        return new EventRecord(unit, code, LocalDateTime.of(2026, 11, 22, 8, 0), HexFormat.of().parseHex(hex));
    }

    @Test
    void bmsEventDetailsOfSocCalibrationStopAndAdditionalInfo() {
        // recorded on an HVS 7.7 on 2026-11-16 and 2026-11-22
        assertEquals("BMS 1: SoC calibration stop (SoC 9 %, 309.5 V, -0.5 A)",
                texts.event(event(1, 10, "000000000000000001090a170c1d0cfbff9d0c910c1a16")));
        assertEquals("BMS 1: Events additional info (SoC 5 %, 307.2 V, 0.0 A)",
                texts.event(event(1, 21, "000000000000000018055e000c050c00003205011e0000")));
        assertTrue(missingKeys.isEmpty(), () -> "missing i18n keys " + missingKeys);
    }

    @Test
    void bmuEventsRecordWithWarningBitmap() {
        // error number 23 carries a warning bitmap in bytes 2..3: cell voltage too low (bit 3), battery imbalance (14)
        assertEquals("BMU: Events record (Cell voltage too low, Battery imbalance, appeared)",
                texts.event(event(EventRecord.UNIT_BMU, 2, "01174008ffffffffffffffffffffffffffffffffffffff")));
        assertTrue(missingKeys.isEmpty(), () -> "missing i18n keys " + missingKeys);
    }

    @Test
    void newEventCodesHaveTexts() {
        assertEquals("BMU: Firmware update failed", texts.event(event(EventRecord.UNIT_BMU, 103, "00".repeat(23))));
        assertEquals("BMU: Parameter table update finished",
                texts.event(event(EventRecord.UNIT_BMU, 120, "00".repeat(23))));
        assertTrue(missingKeys.isEmpty(), () -> "missing i18n keys " + missingKeys);
    }

    @Test
    void bmsEvents() throws BydCommunicationException {
        List<EventRecord> records = BydDecoder.decodeEventLog(1, Fixtures.chunkPayloads("event-bms1", 5));

        assertEquals("BMS 1: Stop discharging (SoC 7 %, 305.3 V, 0.0 A)", texts.event(records.get(0)));
        assertEquals("BMS 1: Start charging (SoC 10 %, 311.3 V, -1.8 A)", texts.event(records.get(4)));
        assertEquals("BMS 1: SoC calibration stop (SoC 9 %, 309.5 V, -0.5 A)", texts.event(records.get(8)));
        assertEquals("BMS 1: Pre-charge successful (SoC 5 %, 306.7 V, 0.0 A)", texts.event(records.get(16)));
        assertTrue(missingKeys.isEmpty(), () -> "missing i18n keys " + missingKeys);
    }

    @Test
    void bmuEvents() throws BydCommunicationException {
        List<EventRecord> records = BydDecoder.decodeEventLog(EventRecord.UNIT_BMU,
                Fixtures.chunkPayloads("event-bmu", 5));

        assertEquals("BMU: SOP info", texts.event(records.get(0)));
        assertEquals("BMU: System status changed (Pre-charge → Active)", texts.event(records.get(2)));
        assertEquals("BMU: System status changed (Standby → Inactive)", texts.event(records.get(9)));
        assertEquals("BMU: Functional safety info", texts.event(records.get(10)));
        assertTrue(missingKeys.isEmpty(), () -> "missing i18n keys " + missingKeys);
    }

    @Test
    void unknownEventCodeFallsBack() {
        EventRecord record = new EventRecord(EventRecord.UNIT_BMU, 200, java.time.LocalDateTime.of(2026, 1, 1, 0, 0),
                new byte[23]);
        assertEquals("BMU: Event 200", texts.event(record));
    }
}
