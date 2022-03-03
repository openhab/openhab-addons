/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.propertyhelper;

import static org.mockito.Mockito.*;

import java.time.ZoneId;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.netatmo.internal.NetatmoBindingConstants;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Gaël L'hopital - Initial contribution
 */
public class PropertyHelperTest {
    private static NADeserializer gson;

    @BeforeAll
    public static void init() {
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.systemDefault());
        gson = new NADeserializer(timeZoneProvider);
    }

    @Test
    public void temperatureChannel() throws NetatmoException {
        Bridge naMain = mock(Bridge.class);
        ApiBridge apiBridge = mock(ApiBridge.class);
        ThingTypeUID naMainThingTypeUID = new ThingTypeUID(NetatmoBindingConstants.BINDING_ID,
                ModuleType.NAMain.toString());
        Mockito.when(naMain.getThingTypeUID()).thenReturn(naMainThingTypeUID);
        // WeatherCapability naMainPropHelper = new WeatherCapability(naMain, apiBridge);

        // String naMainJson = "{\"_id\":\"70:ee:50:00:c1:72\",\"station_name\":\"Orgeval M\\\\u00e9t\\\\u00e9o (Station
        // M\\\\u00e9t\\\\u00e9o)\",\"date_setup\":1371889192,\"last_setup\":1371889192,\"type\":\"NAMain\",\"last_status_store\":1645881501,\"module_name\":\"Station
        // M\\\\u00e9t\\\\u00e9o\",\"firmware\":181,\"last_upgrade\":1474621072,\"wifi_status\":49,\"reachable\":true,\"co2_calibrating\":false,\"data_type\":[\"Temperature\",\"CO2\",\"Humidity\",\"Noise\",\"Pressure\"],\"place\":{\"altitude\":112,\"city\":\"Orgeval\",\"country\":\"FR\",\"timezone\":\"Europe\\\\/Paris\",\"location\":[1.9657212495803835,48.92362015282545]},\"home_id\":\"5842ac41ec135ebca48c078f\",\"home_name\":\"Orgeval
        // M\\\\u00e9t\\\\u00e9o\",\"dashboard_data\":{\"time_utc\":1645881485,\"Temperature\":19.6,\"CO2\":1357,\"Humidity\":54,\"Noise\":37,\"Pressure\":1033.7,\"AbsolutePressure\":1020.1,\"min_temp\":18,\"max_temp\":19.8,\"date_max_temp\":1645870318,\"date_min_temp\":1645842197,\"temp_trend\":\"up\",\"pressure_trend\":\"stable\"},\"modules\":[{\"_id\":\"02:00:00:00:bc:dc\",\"type\":\"NAModule1\",\"module_name\":\"Ext\\\\u00e9rieur\",\"last_setup\":1371889188,\"data_type\":[\"Temperature\",\"Humidity\"],\"battery_percent\":100,\"reachable\":true,\"firmware\":51,\"last_message\":1645881491,\"last_seen\":1645881472,\"rf_status\":67,\"battery_vp\":6200,\"dashboard_data\":{\"time_utc\":1645881472,\"Temperature\":11.2,\"Humidity\":47,\"min_temp\":-0.3,\"max_temp\":11.2,\"date_max_temp\":1645881472,\"date_min_temp\":1645858764,\"temp_trend\":\"up\"}},{\"_id\":\"05:00:00:03:3e:ee\",\"type\":\"NAModule3\",\"module_name\":\"Pluviom\\\\u00e8tre\",\"last_setup\":1480657948,\"data_type\":[\"Rain\"],\"battery_percent\":100,\"reachable\":true,\"firmware\":12,\"last_message\":1645881491,\"last_seen\":1645881485,\"rf_status\":63,\"battery_vp\":6262,\"dashboard_data\":{\"time_utc\":1645881485,\"Rain\":0,\"sum_rain_1\":0,\"sum_rain_24\":0}},{\"_id\":\"06:00:00:02:10:de\",\"type\":\"NAModule2\",\"module_name\":\"An\\\\u00e9mom\\\\u00e8tre\",\"last_setup\":1480660290,\"data_type\":[\"Wind\"],\"battery_percent\":100,\"reachable\":false,\"firmware\":25,\"last_message\":1594855691,\"last_seen\":1594855691,\"rf_status\":79,\"battery_vp\":65535},{\"_id\":\"03:00:00:05:d1:62\",\"type\":\"NAModule4\",\"module_name\":\"Salle
        // de
        // Bain\",\"last_setup\":1644568481,\"data_type\":[\"Temperature\",\"CO2\",\"Humidity\"],\"battery_percent\":100,\"reachable\":true,\"firmware\":51,\"last_message\":1645881491,\"last_seen\":1645881472,\"rf_status\":65,\"battery_vp\":6109,\"dashboard_data\":{\"time_utc\":1645881472,\"Temperature\":23.1,\"CO2\":1369,\"Humidity\":43,\"min_temp\":20.8,\"max_temp\":25.8,\"date_max_temp\":1645860303,\"date_min_temp\":1645842770,\"temp_trend\":\"stable\"}}]}";
        // NAMain object = gson.deserialize(NAMain.class, naMainJson);

        // naMainPropHelper.setNewData(object);
    }
}
