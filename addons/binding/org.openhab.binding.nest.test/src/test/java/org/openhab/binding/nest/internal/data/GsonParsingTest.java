/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonParsingTest {

    String rawThermostatData = "{\"humidity\":25,\"locale\":\"en-GB\",\"temperature_scale\":\"F\","
            + "\"is_using_emergency_heat\":false,\"has_fan\":true,\"software_version\":\"5.6-7\","
            + "\"has_leaf\":true,\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKQrCrjN0yXiw\","
            + "\"device_id\":\"G1jouHN5yl6mXFaQw5iGwXOu-iQr8PMV\",\"name\":\"Living Room (Living Room)\","
            + "\"can_heat\":true,\"can_cool\":false,\"target_temperature_c\":15.5,\"target_temperature_f\":60,"
            + "\"target_temperature_high_c\":24.0,\"target_temperature_high_f\":75,\"target_temperature_low_c\":20.0,"
            + "\"target_temperature_low_f\":68,\"ambient_temperature_c\":19.0,\"ambient_temperature_f\":66,"
            + "\"away_temperature_high_c\":24.0,\"away_temperature_high_f\":76,"
            + "\"away_temperature_low_c\":12.5,\"away_temperature_low_f\":55,\"eco_temperature_high_c\":24.0,"
            + "\"eco_temperature_high_f\":76,\"eco_temperature_low_c\":12.5,\"eco_temperature_low_f\":55,"
            + "\"is_locked\":false,\"locked_temp_min_c\":20.0,\"locked_temp_min_f\":68,\"locked_temp_max_c\":22.0,"
            + "\"locked_temp_max_f\":72,\"sunlight_correction_active\":false,\"sunlight_correction_enabled\":true,"
            + "\"structure_id\":\"ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A\",\"fan_timer_active\":false,"
            + "\"fan_timer_timeout\":\"1970-01-01T00:00:00.000Z\",\"fan_timer_duration\":15,\"previous_hvac_mode\":\"\","
            + "\"hvac_mode\":\"heat\",\"time_to_target\":\"~0\",\"time_to_target_training\":\"ready\","
            + "\"where_name\":\"Living Room\",\"label\":\"Living Room\",\"name_long\":\"Living Room Thermostat (Living Room)\","
            + "\"is_online\":true,\"last_connection\":\"2017-02-02T21:00:06.000Z\",\"hvac_state\":\"off\"}";

    String rawSmokeDetectorData = "{\"name\":\"Downstairs\",\"locale\":\"en-US\","
            + "\"structure_id\":\"ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A\","
            + "\"software_version\":\"3.1rc9\",\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIm5E0NfJPeeg\","
            + "\"device_id\":\"p1b1oySOcs_sbi4iczruW3Ou-iQr8PMV\",\"where_name\":\"Downstairs\","
            + "\"name_long\":\"Downstairs Nest Protect\",\"is_online\":true,\"last_connection\":\"2017-02-02T20:53:05.338Z\","
            + "\"battery_health\":\"ok\",\"co_alarm_state\":\"ok\",\"smoke_alarm_state\":\"ok\",\"ui_color_state\":\"green\","
            + "\"is_manual_test_active\":false}";

    String rawCameraData = "{\"name\":\"Upstairs\",\"software_version\":\"205-600052\","
            + "\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKCxvyZfxNpKA\","
            + "\"device_id\":\"_LK8j9rRXwCKEBOtDo7JskNxzWfHBOIm3CLouCT3FQZzrvokK_DzFQ\","
            + "\"structure_id\":\"ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A\","
            + "\"is_online\":false,\"is_streaming\":false,\"is_audio_input_enabled\":true,"
            + "\"last_is_online_change\":\"2017-01-22T08:19:20.000Z\",\"is_video_history_enabled\":false,"
            + "\"is_public_share_enabled\":false,\"last_event\":{\"has_sound\":false,\"has_motion\":true,"
            + "\"has_person\":false,\"start_time\":\"2017-01-22T07:40:19.020Z\","
            + "\"end_time\":\"2017-01-22T07:40:38.680Z\",\"activity_zone_ids\":[\"id1\",\"id2\"],\"web_url\":\"myurl\","
            + "\"app_url\":\"app_url\",\"image_url\":\"image_url\",\"animated_image_url\":\"animeted\"},"
            + "\"name_long\":\"Upstairs Camera\","
            + "\"web_url\":\"https://home.nest.com/cameras/CjZfTEs4ajlyUlh3Q0tFQk90RG83SnNrTnh6V2ZIQk9JbTNDTG91Q1QzR"
            + "lFaenJ2b2tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSW"
            + "x2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39z"
            + "X1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc\","
            + "\"app_url\":\"nestmobile://cameras/CjZfTEs4ajlyUlh3Q0tFQk90RG83SnNrTnh6V2ZIQk9JbTNDTG91Q1QzRlFaenJ2b2"
            + "tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSWx2QlpxN1g"
            + "yeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39zX1vIOsWrv"
            + "8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc\","
            + "\"snapshot_url\":\"https://www.dropcam.com/api/wwn.get_snapshot/CjZfTEs4ajlyUlh3Q0tFQk90RG83SnNrTnh6V"
            + "2ZIQk9JbTNDTG91Q1QzRlFaenJ2b2tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQj"
            + "c2ejhSWFl3SFFxWXFrSWx2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9E"
            + "SvlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc\"}";

    private String rawStructureData = "{"
            + "\"smoke_co_alarms\":[\"p1b1oySOcs-OJHIgmgeMkHOu-iQr8PMV\",\"p1b1oySOcs8Qu7IAJVrQ7XOu-iQr8PMV\",\"p1b1oySOcs8W9WwaNu80oXOu-iQr8PMV\","
            + "\"p1b1oySOcs_sbi4iczruW3Ou-iQr8PMV\"],"
            + "\"name\":\"Home\",\"country_code\":\"US\",\"postal_code\":\"98056\",\"time_zone\":\"America/Los_Angeles\",\"away\":\"home\","
            + "\"thermostats\":[\"G1jouHN5yl6mXFaQw5iGwXOu-iQr8PMV\"],"
            + "\"structure_id\":\"ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A\",\"rhr_enrollment\":false,\"co_alarm_state\":\"ok\","
            + "\"smoke_alarm_state\":\"ok\",\"eta_begin\":\"2017-02-02T03:10:08.000Z\",\"wheres\":{\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIYpqdaXnYjUg\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIYpqdaXnYjUg\",\"name\":\"Basement\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsK-nCnEjccnMQ\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsK-nCnEjccnMQ\",\"name\":\"Bedroom\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsJyRQEOtmKqkw\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsJyRQEOtmKqkw\",\"name\":\"Den\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKZphUIYeW39g\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKZphUIYeW39g\",\"name\":\"Dining Room\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIm5E0NfJPeeg\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIm5E0NfJPeeg\",\"name\":\"Downstairs\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsK2kdsXRP3IFg\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsK2kdsXRP3IFg\",\"name\":\"Entryway\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIAYVvcpN1cOA\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIAYVvcpN1cOA\",\"name\":\"Family Room\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIB7GULj0y7Rw\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIB7GULj0y7Rw\",\"name\":\"Hallway\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIbTUmML4Q6xA\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIbTUmML4Q6xA\",\"name\":\"Kids Room\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIB2f05cPKRBA\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIB2f05cPKRBA\",\"name\":\"Kitchen\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKQrCrjN0yXiw\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKQrCrjN0yXiw\",\"name\":\"Living Room\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIebdVzhA62Iw\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIebdVzhA62Iw\",\"name\":\"Master Bedroom\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKtUyRb3je64Q\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKtUyRb3je64Q\",\"name\":\"Office\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKCxvyZfxNpKA\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKCxvyZfxNpKA\",\"name\":\"Upstairs\"},\"6UAWzz8czKpFrH6EK3AcjDiTjbRgts8x5MJxEnn1yKKQpYTBO7n2UQ\":"
            + "{\"where_id\":\"6UAWzz8czKpFrH6EK3AcjDiTjbRgts8x5MJxEnn1yKKQpYTBO7n2UQ\",\"name\":\"Downstairs Kitchen\"},\"qpWvTu89Knhn6GRFM-VtGoE4KYwbzbJg9INR6WyPfhW1EJ04GRyYbQ\":"
            + "{\"where_id\":\"qpWvTu89Knhn6GRFM-VtGoE4KYwbzbJg9INR6WyPfhW1EJ04GRyYbQ\",\"name\":\"Garage\"},\"8tH6YiXUAQDZFLD6AgMmQ14Sc5wTG0NxKfabPY0XKrqc47t3uSDZvQ\":"
            + "{\"where_id\":\"8tH6YiXUAQDZFLD6AgMmQ14Sc5wTG0NxKfabPY0XKrqc47t3uSDZvQ\",\"name\":\"Frog\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKfexoqPTcUVA\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKfexoqPTcUVA\",\"name\":\"Backyard\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsJv12iEHQ0hxA\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsJv12iEHQ0hxA\",\"name\":\"Driveway\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsLRu9lIioI47g\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsLRu9lIioI47g\",\"name\":\"Front Yard\"},\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKR8TWb9hTptQ\":"
            + "{\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKR8TWb9hTptQ\",\"name\":\"Outside\"}},"
            + "\"cameras\":[\"_LK8j9rRXwCKEBOtDo7JskNxzWfHBOIm3CLouCT3FQZzrvokK_DzFQ\",\"VG7C7BU6Zf8OjEfizmBCVnwnuKHSnOBIHgbQKa57xKJzrvokK_DzFQ\"]}";

    String rawData = "{\"devices\":" + "{\"thermostats\":{\"therm1\":" + rawThermostatData + "},"
            + "\"smoke_co_alarms\":{\"smoke1\":" + rawSmokeDetectorData + ","
            + "\"smoke2\":{\"name\":\"Upstairs\",\"locale\":\"en-US\",\"structure_id\":\"ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A\","
            + "\"software_version\":\"3.1rc9\",\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKCxvyZfxNpKA\",\"device_id\":\"p1b1oySOcs8W9WwaNu80oXOu-iQr8PMV\","
            + "\"where_name\":\"Upstairs\",\"name_long\":\"Upstairs Nest Protect\",\"is_online\":true,\"last_connection\":\"2017-02-02T20:35:50.051Z\",\"battery_health\":\"ok\","
            + "\"co_alarm_state\":\"ok\",\"smoke_alarm_state\":\"ok\",\"ui_color_state\":\"green\",\"is_manual_test_active\":false,\"last_manual_test_time\":"
            + "\"1970-01-01T00:00:00.000Z\"},"
            + "\"smoke3\":{\"name\":\"Downstairs Kitchen\",\"locale\":\"en-US\",\"structure_id\":\"ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A\","
            + "\"software_version\":\"3.1rc9\",\"where_id\":\"6UAWzz8czKpFrH6EK3AcjDiTjbRgts8x5MJxEnn1yKKQpYTBO7n2UQ\",\"device_id\":\"p1b1oySOcs-OJHIgmgeMkHOu-iQr8PMV\","
            + "\"where_name\":\"Downstairs Kitchen\",\"name_long\":\"Downstairs Kitchen Nest Protect\",\"is_online\":true,\"last_connection\":\"2017-02-02T11:04:18.804Z\","
            + "\"battery_health\":\"ok\",\"co_alarm_state\":\"ok\",\"smoke_alarm_state\":\"ok\",\"ui_color_state\":\"green\",\"is_manual_test_active\":false,"
            + "\"last_manual_test_time\":\"1970-01-01T00:00:00.000Z\"},"
            + "\"smoke4\":{\"name\":\"Living Room\",\"locale\":\"en-US\",\"structure_id\":\"ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A\","
            + "\"software_version\":\"3.1rc9\",\"where_id\":\"z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKQrCrjN0yXiw\",\"device_id\":\"p1b1oySOcs8Qu7IAJVrQ7XOu-iQr8PMV\","
            + "\"where_name\":\"Living Room\",\"name_long\":\"Living Room Nest Protect\",\"is_online\":true,\"last_connection\":\"2017-02-02T13:30:34.187Z\","
            + "\"battery_health\":\"ok\",\"co_alarm_state\":\"ok\",\"smoke_alarm_state\":\"ok\",\"ui_color_state\":\"green\",\"is_manual_test_active\":false,"
            + "\"last_manual_test_time\":\"1970-01-01T00:00:00.000Z\"}}," + "\"cameras\":{\"camera1\":" + rawCameraData
            + ","
            + "\"camera2\":{\"name\":\"Garage\",\"software_version\":\"205-600052\",\"where_id\":\"qpWvTu89Knhn6GRFM-VtGoE4KYwbzbJg9INR6WyPfhW1EJ04GRyYbQ\","
            + "\"device_id\":\"VG7C7BU6Zf8OjEfizmBCVnwnuKHSnOBIHgbQKa57xKJzrvokK_DzFQ\",\"structure_id\":\"ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A\","
            + "\"is_online\":false,\"is_streaming\":false,\"is_audio_input_enabled\":true,\"last_is_online_change\":\"2016-11-20T07:03:42.000Z\","
            + "\"is_video_history_enabled\":false,\"is_public_share_enabled\":false,\"last_event\":{\"has_sound\":false,\"has_motion\":true,"
            + "\"has_person\":false,\"start_time\":\"2016-11-20T07:02:27.260Z\",\"end_time\":\"2016-11-20T07:02:46.860Z\"},\"name_long\":\"Garage Camera\","
            + "\"web_url\":\"https://home.nest.com/cameras/CjZWRzdDN0JVNlpmOE9qRWZpem1CQ1Zud251S0hTbk9CSUhnYlFLYTU3eEtKenJ2b2tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMk"
            + "ZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSWx2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ES"
            + "vlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc\","
            + "\"app_url\":\"nestmobile://cameras/CjZWRzdDN0JVNlpmOE9qRWZpem1CQ1Zud251S0hTbk9CSUhnYlFLYTU3eEtKenJ2b2tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEE"
            + "aNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSWx2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7v"
            + "G8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc\","
            + "\"snapshot_url\":\"https://www.dropcam.com/api/wwn.get_snapshot/CjZWRzdDN0JVNlpmOE9qRWZpem1CQ1Zud251S0hTbk9CSUhnYlFLYTU3eEtKenJ2b2tLX0R6RlESFm"
            + "9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSWx2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7Gayz"
            + "LusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc\"}}},"
            + "\"structures\":{\"struct1\":" + rawStructureData + "},"
            + "\"metadata\":{\"access_token\":\"c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4"
            + "MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc\",\"client_version\":1}}";

    String accessTokenData = "{\"access_token\":\"access_token\",\"expires_in\":315360000}";

    @Test
    public void verifyCompleteInput() {
        String jsonInput = rawData;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        TopLevelData topLevel = gson.fromJson(jsonInput, TopLevelData.class);
        assertEquals(topLevel.getDevices().getThermostats().size(), 1);
        assertNotNull(topLevel.getDevices().getThermostats().get("therm1"));
        assertEquals(topLevel.getDevices().getCameras().size(), 2);
        assertNotNull(topLevel.getDevices().getCameras().get("camera1"));
        assertNotNull(topLevel.getDevices().getCameras().get("camera2"));
        assertEquals(topLevel.getDevices().getSmokeDetectors().size(), 4);
        assertNotNull(topLevel.getDevices().getSmokeDetectors().get("smoke1"));
        assertNotNull(topLevel.getDevices().getSmokeDetectors().get("smoke2"));
        assertNotNull(topLevel.getDevices().getSmokeDetectors().get("smoke3"));
        assertNotNull(topLevel.getDevices().getSmokeDetectors().get("smoke4"));
    }

    @Test
    public void verifyThermostat() {
        String jsonInput = rawThermostatData;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Thermostat thermostat = gson.fromJson(jsonInput, Thermostat.class);
        assertTrue(thermostat.isOnline());
        assertTrue(thermostat.isCanHeat());
        assertTrue(thermostat.isHasLeaf());
        assertFalse(thermostat.isCanCool());
        assertFalse(thermostat.isFanTimerActive());
        assertFalse(thermostat.isLocked());
        assertFalse(thermostat.isSunlightCorrectionActive());
        assertTrue(thermostat.isSunlightCorrectionEnabled());
        assertFalse(thermostat.isUsingEmergencyHeat());
        assertEquals("G1jouHN5yl6mXFaQw5iGwXOu-iQr8PMV", thermostat.getDeviceId());
        assertEquals(Integer.valueOf(15), thermostat.getFanTimerDuration());
        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCal.set(2017, 1, 2, 21, 0, 6);
        assertEquals(utcCal.getTime().toString(), thermostat.getLastConnection().toString());
        utcCal.set(1970, 0, 1, 0, 0, 0);
        assertEquals(utcCal.getTime().toString(), thermostat.getFanTimerTimeout().toString());
        assertEquals(Double.valueOf(22.0), thermostat.getLockedTemperatureHigh());
        assertEquals(Double.valueOf(20.0), thermostat.getLockedTemperatureLow());
        assertEquals("heat", thermostat.getMode());
        assertEquals("Living Room (Living Room)", thermostat.getName());
        assertEquals("Living Room Thermostat (Living Room)", thermostat.getNameLong());
        assertEquals("", thermostat.getPreviousMode());
        assertEquals("5.6-7", thermostat.getSoftwareVersion());
        assertEquals("off", thermostat.getState());
        assertEquals("ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A", thermostat.getStructureId());
        assertEquals(Double.valueOf(15.5), thermostat.getTargetTemperature());
        assertEquals(Double.valueOf(24.0), thermostat.getTargetTemperatureHigh());
        assertEquals(Double.valueOf(20.0), thermostat.getTargetTemperatureLow());
        assertEquals("F", thermostat.getTempScale());
        assertEquals(Integer.valueOf(0), thermostat.getTimeToTarget());
        assertEquals("z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKQrCrjN0yXiw", thermostat.getWhereId());
        assertEquals("Living Room", thermostat.getWhereName());
    }

    @Test
    public void verifyCamera() {
        String jsonInput = rawCameraData;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Camera camera = gson.fromJson(jsonInput, Camera.class);
        assertFalse(camera.isOnline());
        assertEquals("Upstairs", camera.getName());
        assertEquals("Upstairs Camera", camera.getNameLong());
        assertEquals("ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A", camera.getStructureId());
        assertEquals("z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKCxvyZfxNpKA", camera.getWhereId());
        assertTrue(camera.isAudioInputEnabled());
        assertFalse(camera.isPublicShareEnabled());
        assertFalse(camera.isStreaming());
        assertFalse(camera.isVideoHistoryEnabled());
        assertEquals("nestmobile://cameras/CjZfTEs4ajlyUlh3Q0tFQk90RG83SnNrTnh6V2ZIQk9JbTNDTG91Q1QzRlFaenJ2b2"
                + "tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSWx2QlpxN1g"
                + "yeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39zX1vIOsWrv"
                + "8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc", camera.getAppUrl());
        assertEquals("_LK8j9rRXwCKEBOtDo7JskNxzWfHBOIm3CLouCT3FQZzrvokK_DzFQ", camera.getDeviceId());
        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // 2017-01-22T08:19:20.000Z
        utcCal.set(2017, 0, 22, 8, 19, 20);
        assertNull(camera.getLastConnection());
        assertEquals(utcCal.getTime().toString(), camera.getLastIsOnlineChange().toString());
        assertNull(camera.getPublicShareUrl());
        assertEquals(
                "https://www.dropcam.com/api/wwn.get_snapshot/CjZfTEs4ajlyUlh3Q0tFQk90RG83SnNrTnh6V"
                        + "2ZIQk9JbTNDTG91Q1QzRlFaenJ2b2tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQj"
                        + "c2ejhSWFl3SFFxWXFrSWx2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9E"
                        + "SvlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc",
                camera.getSnapshotUrl());
        assertEquals("205-600052", camera.getSoftwareVersion());
        assertEquals(
                "https://home.nest.com/cameras/CjZfTEs4ajlyUlh3Q0tFQk90RG83SnNrTnh6V2ZIQk9JbTNDTG91Q1QzR"
                        + "lFaenJ2b2tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSW"
                        + "x2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39z"
                        + "X1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc",
                camera.getWebUrl());
        assertEquals("animeted", camera.getLastEvent().getAnimatedImageUrl());
        assertEquals(2, camera.getLastEvent().getActivityZones().size());
        assertEquals("id1", camera.getLastEvent().getActivityZones().get(0));
        assertEquals("app_url", camera.getLastEvent().getAppUrl());
        // 2017-01-22T07:40:38.680Z
        utcCal.set(2017, 0, 22, 7, 40, 38);
        assertEquals(utcCal.getTime().toString(), camera.getLastEvent().getEndTime().toString());
        assertEquals("image_url", camera.getLastEvent().getImageUrl());
        // 2017-01-22T07:40:19.020Z
        utcCal.set(2017, 0, 22, 7, 40, 19);
        assertEquals(utcCal.getTime().toString(), camera.getLastEvent().getStartTime().toString());
        assertNull(camera.getLastEvent().getUrlsExpireTime());
        assertEquals("myurl", camera.getLastEvent().getWebUrl());
        assertTrue(camera.getLastEvent().isHasMotion());
        assertFalse(camera.getLastEvent().isHasPerson());
        assertFalse(camera.getLastEvent().isHasSound());
    }

    @Test
    public void verifySmokeDetector() {
        String jsonInput = rawSmokeDetectorData;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        SmokeDetector smokeDetector = gson.fromJson(jsonInput, SmokeDetector.class);
        assertTrue(smokeDetector.isOnline());
        assertEquals("z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIm5E0NfJPeeg", smokeDetector.getWhereId());
        assertEquals("p1b1oySOcs_sbi4iczruW3Ou-iQr8PMV", smokeDetector.getDeviceId());
        assertEquals("Downstairs", smokeDetector.getName());
        assertEquals("Downstairs Nest Protect", smokeDetector.getNameLong());
        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // 2017-02-02T20:53:05.338Z
        utcCal.set(2017, 1, 2, 20, 53, 5);
        assertEquals(utcCal.getTime().toString(), smokeDetector.getLastConnection().toString());
        assertEquals(SmokeDetector.BatteryHealth.OK, smokeDetector.getBatteryHealth());
        assertEquals(SmokeDetector.AlarmState.OK, smokeDetector.getCoAlarmState());
        assertEquals(SmokeDetector.AlarmState.OK, smokeDetector.getSmokeAlarmState());
        assertEquals("3.1rc9", smokeDetector.getSoftwareVersion());
        assertEquals("ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A", smokeDetector.getStructureId());
        assertEquals(SmokeDetector.UiColorState.GREEN, smokeDetector.getUiColorState());
    }

    @Test
    public void verifyAccessToken() {
        String jsonInput = accessTokenData;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        AccessTokenData accessToken = gson.fromJson(jsonInput, AccessTokenData.class);
        assertEquals("access_token", accessToken.getAccessToken());
        assertEquals(Long.valueOf(315360000L), accessToken.getExpiresIn());
    }

    @Test
    public void verifyStructure() {
        String jsonInput = rawStructureData;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Structure structure = gson.fromJson(jsonInput, Structure.class);
        assertEquals("Home", structure.getName());
        assertEquals("US", structure.getCountryCode());
        assertEquals("98056", structure.getPostalCode());
        assertEquals(Structure.HomeAwayState.HOME, structure.getAway());
        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // 2017-02-02T03:10:08.000Z
        utcCal.set(2017, 1, 2, 3, 10, 8);
        assertEquals(utcCal.getTime().toString(), structure.getEtaBegin().toString());
        assertNull(structure.getEta());
        assertNull(structure.getPeakPeriodEndTime());
        assertNull(structure.getPeakPeriodStartTime());
        assertEquals("ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A", structure.getStructureId());
        assertEquals("America/Los_Angeles", structure.getTimeZone());
        assertFalse(structure.isRushHourRewardsEnrollement());
    }
}
