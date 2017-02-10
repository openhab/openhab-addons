package org.openhab.binding.nest.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.handler.NestBridgeHandler;
import org.openhab.binding.nest.internal.data.Thermostat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NestDiscoveryServiceTest {
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

    public static final ThingUID bridgeUID = new ThingUID("bridge", "id");

    @Test
    public void testStartScan() {
        NestBridgeHandler bridge = Mockito.mock(NestBridgeHandler.class);
        NestDiscoveryService service = new NestDiscoveryService(bridge);
        Mockito.doNothing().when(bridge).startDiscoveryScan();
        service.startScan();
        Mockito.verify(bridge, Mockito.only()).startDiscoveryScan();
    }

    @Test
    public void testOnThermostatAdded() {
        Thing thing = Mockito.mock(Thing.class);
        NestBridgeHandler bridge = Mockito.mock(NestBridgeHandler.class);
        DiscoveryListener listener = Mockito.mock(DiscoveryListener.class);
        NestDiscoveryService service = new NestDiscoveryService(bridge);
        service.addDiscoveryListener(listener);
        Mockito.doReturn(thing).when(bridge).getThing();
        Mockito.doReturn(bridgeUID).when(thing).getUID();

        ThingUID thingUID = new ThingUID(NestBindingConstants.THING_TYPE_THERMOSTAT, bridgeUID,
                "G1jouHN5yl6mXFaQw5iGwXOu-iQr8PMV");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Thermostat thermostat = gson.fromJson(rawThermostatData, Thermostat.class);
        service.onThermostatAdded(thermostat);
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(NestBindingConstants.PROPERTY_ID, "G1jouHN5yl6mXFaQw5iGwXOu-iQr8PMV");
        properties.put(NestBindingConstants.PROPERTY_FIRMWARE_VERSION, "5.6-7");
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(NestBindingConstants.THING_TYPE_THERMOSTAT)
                .withLabel("Living Room Thermostat (Living Room)").withBridge(bridgeUID).withProperties(properties)
                .build();
        Mockito.verify(listener, Mockito.only()).thingDiscovered(Matchers.eq(service), Matchers.eq(discoveryResult));
    }
}
