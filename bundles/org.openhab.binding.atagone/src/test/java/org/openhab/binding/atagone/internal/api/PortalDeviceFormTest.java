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
package org.openhab.binding.atagone.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.Fields;
import org.junit.jupiter.api.Test;
import org.openhab.binding.atagone.internal.dto.DeviceConfigDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;

/**
 * Tests {@link PortalDeviceForm} against a sanitised capture of the portal's device settings form.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
class PortalDeviceFormTest {

    private static final List<String> BROWSER_FIELDS = List.of("__RequestVerificationToken", "deviceId", "control_mode",
            "ch_heating_type", "isolation_level", "ch_building_size", "wdr_temps_influence", "climate_zone",
            "wdr_temps_offset", "summer_eco_mode", "summer_eco_temp", "frost_protection",
            "frost_protection_temp_inside", "frost_protection_temp_outside", "max_preheat", "ch_base_temp",
            "vacation_mode_temp", "ch_mode_vacation", "ch_mode_extend", "time_zone", "dhw_base_temp",
            "dhw_legion_enabled", "dhw_legion_enabled", "dhw_legion_day", "dhw_legion_time", "Location.Country",
            "Location.Postal_code", "Location.City", "Location.Street", "Location.Housenumber",
            "Location.HousenumberSuffix", "X-Requested-With");

    private static PortalDeviceForm form() throws IOException, AtagOneCommunicationException {
        try (InputStream in = PortalDeviceFormTest.class.getResourceAsStream("portal/device-form.html")) {
            assertNotNull(in);
            return PortalDeviceForm.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static String value(Fields fields, String name) {
        Fields.Field field = fields.get(name);
        assertNotNull(field, name);
        return field.getValue();
    }

    @Test
    void postsTheSameFieldsAsTheBrowser() throws Exception {
        List<String> names = form().names();

        assertEquals(BROWSER_FIELDS.stream().distinct().sorted().toList(), names.stream().sorted().toList());
        assertEquals(List.of("true", "false"), form().toFields().get("dhw_legion_enabled").getValues());
        assertEquals(List.of("false"), form().toFields().get("summer_eco_mode").getValues());
    }

    @Test
    void keepsTheAddressAndIdentifiersAsRead() throws Exception {
        Fields fields = form().toFields();

        assertEquals("tok123", value(fields, "__RequestVerificationToken"));
        assertEquals("1234-5678-9012_34-56-789-012", value(fields, "deviceId"));
        assertEquals("Teststraße", value(fields, "Location.Street"));
        assertEquals("Teststadt", value(fields, "Location.City"));
        assertEquals("NL", value(fields, "Location.Country"));
        assertEquals("XMLHttpRequest", value(fields, "X-Requested-With"));
    }

    @Test
    void changesOnlyWhatDiffersFromTheCurrentConfiguration() throws Exception {
        PortalDeviceForm form = form();
        Fields before = form.toFields();
        DeviceConfigDTO current = new DeviceConfigDTO();
        current.ch_vacation_temp = 14.0;
        current.ch_mode_extend = 3600;
        current.ch_heating_type = 5;
        DeviceConfigUpdateDTO update = new DeviceConfigUpdateDTO();
        update.ch_vacation_temp = 14.5;
        update.ch_mode_extend = 3600L;
        update.ch_heating_type = 5;

        form.apply(null, update, current);
        Fields after = form.toFields();

        assertEquals("14.5", value(after, "vacation_mode_temp"));
        for (Fields.Field field : before) {
            if (!"vacation_mode_temp".equals(field.getName())) {
                assertEquals(field.getValues(), after.get(field.getName()).getValues(), field.getName());
            }
        }
    }

    @Test
    void translatesConfigurationValuesToFormValues() throws Exception {
        PortalDeviceForm form = form();
        DeviceConfigUpdateDTO update = new DeviceConfigUpdateDTO();
        update.ch_heating_type = 4;
        update.wdr_temps_influence = 4;
        update.frost_prot_enabled = 3;
        update.max_preheat = 1440;
        update.ch_mode_vacation = 3 * 86400L;
        update.ch_mode_extend = 7200L;
        update.time_zone = 2;
        update.dhw_legion_time = 450;
        update.summer_eco_mode = 1;
        update.summer_eco_temp = 15.5;

        form.apply(1, update, null);
        Fields fields = form.toFields();

        assertEquals("wdr", value(fields, "control_mode"));
        assertEquals("radiator_undefloor", value(fields, "ch_heating_type"));
        assertEquals("room", value(fields, "wdr_temps_influence"));
        assertEquals("outside, room", value(fields, "frost_protection"));
        assertEquals("240", value(fields, "max_preheat"));
        assertEquals("3", value(fields, "ch_mode_vacation"));
        assertEquals("2", value(fields, "ch_mode_extend"));
        assertEquals("Brussels", value(fields, "time_zone"));
        assertEquals("07:30", value(fields, "dhw_legion_time"));
        assertEquals("15.5", value(fields, "summer_eco_temp"));
        assertEquals(List.of("true", "false"), fields.get("summer_eco_mode").getValues());
    }

    @Test
    void switchesAFlagOffWithoutTheCheckboxValue() throws Exception {
        PortalDeviceForm form = form();
        DeviceConfigUpdateDTO update = new DeviceConfigUpdateDTO();
        update.dhw_legion_enabled = 0;

        form.apply(null, update, null);

        assertEquals(List.of("false"), form.toFields().get("dhw_legion_enabled").getValues());
    }

    @Test
    void refusesSettingsTheFormDoesNotHave() throws Exception {
        DeviceConfigDTO current = new DeviceConfigDTO();
        current.language = 4;
        DeviceConfigUpdateDTO same = new DeviceConfigUpdateDTO();
        same.privacy_mode = 0;
        DeviceConfigUpdateDTO privacy = new DeviceConfigUpdateDTO();
        privacy.privacy_mode = 1;

        form().apply(null, same, current);
        assertThrows(AtagOneCommunicationException.class, () -> form().apply(null, privacy, current));
    }

    @Test
    void reportsWhetherAnythingChanged() throws Exception {
        DeviceConfigDTO current = new DeviceConfigDTO();
        current.ch_vacation_temp = 14.0;
        DeviceConfigUpdateDTO same = new DeviceConfigUpdateDTO();
        same.ch_vacation_temp = 14.0;
        DeviceConfigUpdateDTO different = new DeviceConfigUpdateDTO();
        different.ch_vacation_temp = 15.0;

        assertFalse(form().apply(null, same, current));
        assertTrue(form().apply(null, different, current));
    }

    @Test
    void refusesValuesThePortalDoesNotOffer() {
        DeviceConfigUpdateDTO preheat = new DeviceConfigUpdateDTO();
        preheat.max_preheat = 77;
        DeviceConfigUpdateDTO days = new DeviceConfigUpdateDTO();
        days.ch_mode_vacation = 90000L;

        assertThrows(AtagOneCommunicationException.class, () -> form().apply(null, preheat, null));
        assertThrows(AtagOneCommunicationException.class, () -> form().apply(null, days, null));
    }
}
