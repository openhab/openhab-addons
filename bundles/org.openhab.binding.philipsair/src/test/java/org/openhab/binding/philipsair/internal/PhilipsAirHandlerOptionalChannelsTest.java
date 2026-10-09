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
package org.openhab.binding.philipsair.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.philipsair.internal.connection.CoapProfile;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIConnection;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIException;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateOption;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;

/**
 * Tests adding the optional channels reported by the device to the thing.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class PhilipsAirHandlerOptionalChannelsTest {

    private static final String HUMIDIFIER_STATUS = """
            {"name":"Living","type":"AC3829","modelid":"AC3829/50","swversion":"1.4.0","om":"s","pwr":"1",\
            "cl":false,"aqil":0,"uil":"0","dt":0,"dtrs":0,"mode":"P","func":"PH","rhset":50,"rh":54,"temp":28,\
            "pm25":2,"iaql":1,"aqit":7,"ddp":"1","err":0,"wl":100,"fltsts0":283,"fltsts1":1449,"fltsts2":1449,\
            "wicksts":1449}""";

    private static final String PURIFIER_STATUS = """
            {"om":"1","pwr":"1","cl":false,"aqil":100,"uil":"1","mode":"P","pm25":5,"iaql":1,"aqit":4,"ddp":"1",\
            "err":0,"fltsts0":100,"fltsts1":2000,"fltsts2":2000}""";

    private static final String AC5659_STATUS = """
            {"name":"Woonkamer","type":"AC5659","modelid":"AC5659/10","swversion":"2.0.0","rssi":-50,"om":"s",\
            "pwr":"1","cl":false,"aqil":0,"uil":"0","mode":"M","pm25":3,"iaql":1,"aqit":4,"aqit_ext":0,"tvoc":1,\
            "ddp":"0","rddp":"0","err":0,"fltt1":"A3","fltt2":"C7","fltsts0":344,"fltsts1":1985,"fltsts2":1985}""";

    // the channel type each optional channel has, as defined in channel-groups.xml and channels.xml
    private static final Map<String, String> OPTIONAL_CHANNEL_TYPES = Map.ofEntries(
            Map.entry("controls#timer", "philipsair:timer"),
            Map.entry("controls#timer-remaining", "philipsair:timer-remaining"),
            Map.entry("controls#target-humidity", "philipsair:target-humidity"),
            Map.entry("controls#function", "philipsair:function"),
            Map.entry("controls#standby-sensors", "philipsair:standby-sensors"),
            Map.entry("controls#allergy-sleep", "philipsair:allergy-sleep"),
            Map.entry("controls-ui#beep", "philipsair:beep"), Map.entry("controls-ui#display", "philipsair:display"),
            Map.entry("controls-ui#display-brightness", "philipsair:display-brightness"),
            Map.entry("controls-ui#lamp-mode", "philipsair:lamp-mode"),
            Map.entry("sensors#humidity", "philipsair:humidity"),
            Map.entry("sensors#temperature", "philipsair:temperature"),
            Map.entry("sensors#water-level", "philipsair:water-level"), Map.entry("sensors#tvoc", "philipsair:tvoc"),
            Map.entry("sensors#rssi", "philipsair:rssi"),
            Map.entry("filters#wick-filter-life", "philipsair:wick-filter-life"));

    private final Gson gson = new Gson();
    // the channel type the handler requested for each channel it added
    private final Map<ChannelUID, ChannelTypeUID> requestedChannelTypes = new HashMap<>();

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) PhilipsAirAPIConnection connection;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) PhilipsAirStateDescriptionOptionProvider stateDescriptionProvider;

    private @NonNullByDefault({}) PhilipsAirHandler handler;

    @BeforeEach
    public void setUp() {
        ThingUID thingUID = new ThingUID(THING_TYPE_COAP, "test");
        Thing thing = PhilipsAirHandlerFixture.thing(THING_TYPE_COAP, thingUID, new Configuration(), Map.of(),
                List.of(ChannelBuilder.create(new ChannelUID(thingUID, CONTROLS, POWER), "Switch").build()));
        handler = PhilipsAirHandlerFixture.handler(thing, httpClient, stateDescriptionProvider, callback);
    }

    /**
     * Makes the connection report its configuration, which the handler reads after each update that has data.
     */
    private void stubConnectionConfig() {
        when(connection.getConfig()).thenReturn(new PhilipsAirConfiguration());
    }

    /**
     * Makes the callback create the channels the handler adds, and records the channel type it requested for them.
     */
    private void stubChannelCreation() {
        when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class))).thenAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(0);
            requestedChannelTypes.put(channelUID, invocation.getArgument(1));
            return ChannelBuilder.create(channelUID, "Number");
        });
    }

    @Test
    public void reportedOptionalChannelsAreAddedOnce() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(HUMIDIFIER_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        ArgumentCaptor<Thing> thingCaptor = ArgumentCaptor.forClass(Thing.class);
        verify(callback, times(1)).thingUpdated(thingCaptor.capture());
        Set<String> channelIds = channelIds(thingCaptor.getValue().getChannels());
        assertEquals(
                Set.of("controls#power", "controls#timer", "controls#timer-remaining", "controls#target-humidity",
                        "controls#function", "sensors#humidity", "sensors#temperature", "sensors#water-level"),
                channelIds);

        handler.updateData(connection);

        verify(callback, times(1)).thingUpdated(any());
        // the channels were only requested once, each with its channel type
        assertEquals(
                Map.of("controls#timer", "philipsair:timer", "controls#timer-remaining", "philipsair:timer-remaining",
                        "controls#target-humidity", "philipsair:target-humidity", "controls#function",
                        "philipsair:function", "sensors#humidity", "philipsair:humidity", "sensors#temperature",
                        "philipsair:temperature", "sensors#water-level", "philipsair:water-level"),
                requestedChannelTypeIds());
    }

    private Map<String, String> requestedChannelTypeIds() {
        return requestedChannelTypes.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getId(), entry -> entry.getValue().toString()));
    }

    @Test
    public void everyOptionalChannelHasAChannelType() throws Exception {
        stubChannelCreation();
        stubConnectionConfig();
        // reports every value that adds an optional channel
        String status = """
                {"modelid":"AC3210/12","pwr":"1","dt":0,"dtrs":0,"rhset":50,"func":"PH","rh":50,"temp":20,"wl":50,\
                "tvoc":1,"rssi":-50,"beep":true,"standby":false,"allslp":false,"dispon":true,"dispbr":"115",\
                "lamp":"2","wicksts":100}""";
        when(connection.getAirPurifierStatus(any())).thenReturn(gson.fromJson(status, PhilipsAirPurifierDataDTO.class));
        when(connection.getAirPurifierFiltersStatus(any()))
                .thenReturn(gson.fromJson(status, PhilipsAirPurifierFiltersDTO.class));

        handler.updateData(connection);

        assertEquals(OPTIONAL_CHANNEL_TYPES.keySet(), channelIds(handler.getThing().getChannels()).stream()
                .filter(id -> !"controls#power".equals(id)).collect(Collectors.toSet()));
        assertEquals(OPTIONAL_CHANNEL_TYPES, requestedChannelTypeIds());
        Set<String> channelTypeIds = channelTypeIds();
        requestedChannelTypes.values()
                .forEach(channelTypeUID -> assertTrue(channelTypeIds.contains(channelTypeUID.getId()),
                        "no channel type " + channelTypeUID));
    }

    private Set<String> channelTypeIds() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        try (InputStream in = PhilipsAirHandlerOptionalChannelsTest.class
                .getResourceAsStream("/OH-INF/thing/channels.xml")) {
            assertNotNull(in);
            Document document = factory.newDocumentBuilder().parse(in);
            NodeList channelTypes = document.getElementsByTagName("channel-type");
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < channelTypes.getLength(); i++) {
                ids.add(channelTypes.item(i).getAttributes().getNamedItem("id").getNodeValue());
            }
            assertFalse(ids.isEmpty());
            return ids;
        }
    }

    @Test
    public void tvocAndRssiChannelsAndFilterTypesAreAdded() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        when(connection.isPushingStatus()).thenReturn(true);
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDataDTO.class));
        when(connection.getAirPurifierFiltersStatus(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierFiltersDTO.class));

        handler.updateData(connection);

        assertEquals(Set.of("controls#power", "sensors#tvoc", "sensors#rssi"),
                channelIds(handler.getThing().getChannels()));
        Map<String, String> properties = handler.getThing().getProperties();
        assertEquals("A3", properties.get(PROPERTY_HEPA_FILTER_TYPE));
        assertEquals("C7", properties.get(PROPERTY_CARBON_FILTER_TYPE));
        assertFalse(properties.containsKey(PROPERTY_PRE_FILTER_TYPE));
    }

    @Test
    public void unreportedOptionalChannelsAreNotAdded() throws PhilipsAirAPIException {
        stubConnectionConfig();
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        verify(callback, never()).thingUpdated(any());
        assertEquals(Set.of("controls#power"), channelIds(handler.getThing().getChannels()));
    }

    @Test
    public void wickFilterChannelIsAddedWithoutLinkedFilterChannels() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class));
        when(connection.getAirPurifierFiltersStatus(any()))
                .thenReturn(gson.fromJson(HUMIDIFIER_STATUS, PhilipsAirPurifierFiltersDTO.class));

        handler.updateData(connection);

        assertEquals(Set.of("controls#power", "filters#wick-filter-life"),
                channelIds(handler.getThing().getChannels()));
    }

    @Test
    public void modelOptionsIncludeGasOnGasModelsAndAreSetOnce() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);
        handler.updateData(connection);

        // each of the channels got its options once, and no other channel got any
        assertEquals(List.of(new StateOption("0", "Allergen Index"), new StateOption("1", "PM2.5"),
                new StateOption("2", "Gas")), stateOptions(CONTROLS_UI, DISPLAYED_INDEX));
        assertEquals(List.of("1", "4", "7", "10"), options(SENSORS, AIR_QUALITY_NOTIFICATION_THRESHOLD));
        verifyNoMoreInteractions(stateDescriptionProvider);
    }

    @Test
    public void modelOptionsAreNotDecidedBeforeTheDeviceInfoIsKnown() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        verify(stateDescriptionProvider, never()).setStateOptions(any(), any());

        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDeviceDTO.class));

        handler.updateData(connection);

        assertEquals(List.of("0", "1", "2"), options(CONTROLS_UI, DISPLAYED_INDEX));
        assertEquals(List.of("1", "4", "7", "10"), options(SENSORS, AIR_QUALITY_NOTIFICATION_THRESHOLD));
        verifyNoMoreInteractions(stateDescriptionProvider);
    }

    @Test
    public void unicornOptionsAndChannelsAreSet() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        // the status as translated by the connection, see CoapProfile
        String status = """
                {"modelid":"AC3210/12","pwr":"1","mode":"P","om":"1","dt":0,"dtrs":0,"aqit":7,\
                "err":0,"rh":46,"temp":24.1,"pm25":1,"iaql":1}""";
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.UNICORN);
        when(connection.isPushingStatus()).thenReturn(true);
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(status, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any())).thenReturn(gson.fromJson(status, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        assertEquals(Set.of("controls#power", "controls#timer", "controls#timer-remaining", "sensors#humidity",
                "sensors#temperature"), channelIds(handler.getThing().getChannels()));
        assertEquals(List.of("1", "2", "3", "4", "5", "m", "t"), options(CONTROLS, FAN_MODE));
        assertEquals(List.of("P", "S"), options(CONTROLS, MODE));
        assertEquals(List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"),
                options(CONTROLS, AUTO_TIMEOFF));
        assertEquals(List.of("0", "1"), options(CONTROLS_UI, DISPLAYED_INDEX));
        assertEquals(List.of("1", "4", "7", "10"), options(SENSORS, AIR_QUALITY_NOTIFICATION_THRESHOLD));
        assertEquals("UNICORN", handler.getThing().getProperties().get(PROPERTY_DEVICE_PROFILE));
    }

    @Test
    public void settingChannelsAreAddedAndControlled() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        // the status as translated by the connection, see CoapProfile
        String status = """
                {"modelid":"AC3210/12","pwr":"1","beep":true,"standby":false,"allslp":false,"dispon":true,\
                "dispbr":"115","lamp":"2"}""";
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.UNICORN);
        when(connection.isPushingStatus()).thenReturn(true);
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(status, PhilipsAirPurifierDeviceDTO.class));
        PhilipsAirPurifierDataDTO data = gson.fromJson(status, PhilipsAirPurifierDataDTO.class);
        when(connection.getAirPurifierStatus(any())).thenReturn(data);

        handler.updateData(connection);

        assertEquals(
                Set.of("controls#power", "controls-ui#beep", "controls-ui#display", "controls-ui#display-brightness",
                        "controls-ui#lamp-mode", "controls#standby-sensors", "controls#allergy-sleep"),
                channelIds(handler.getThing().getChannels()));
        assertEquals(List.of("0", "101", "115", "123"), options(CONTROLS_UI, DISPLAY_BRIGHTNESS));
        assertEquals(List.of("0", "1", "2"), options(CONTROLS_UI, LAMP_MODE));

        ThingUID thingUID = handler.getThing().getUID();
        assertEquals(OnOffType.ON, handler.getValue(new ChannelUID(thingUID, CONTROLS_UI, BEEP), data, null, null));
        assertEquals(OnOffType.OFF,
                handler.getValue(new ChannelUID(thingUID, CONTROLS, STANDBY_SENSORS), data, null, null));
        assertEquals(OnOffType.ON, handler.getValue(new ChannelUID(thingUID, CONTROLS_UI, DISPLAY), data, null, null));
        assertEquals("115",
                handler.getValue(new ChannelUID(thingUID, CONTROLS_UI, DISPLAY_BRIGHTNESS), data, null, null));
        assertEquals("2", handler.getValue(new ChannelUID(thingUID, CONTROLS_UI, LAMP_MODE), data, null, null));

        assertEquals("{\"beep\":false}", gson.toJson(handler.prepareCommandData(BEEP, OnOffType.OFF)));
        assertEquals("{\"standby\":true}", gson.toJson(handler.prepareCommandData(STANDBY_SENSORS, OnOffType.ON)));
        assertEquals("{\"allslp\":true}", gson.toJson(handler.prepareCommandData(ALLERGY_SLEEP, OnOffType.ON)));
        assertEquals("{\"dispon\":false}", gson.toJson(handler.prepareCommandData(DISPLAY, OnOffType.OFF)));
        assertEquals("{\"dispbr\":\"123\"}",
                gson.toJson(handler.prepareCommandData(DISPLAY_BRIGHTNESS, new StringType("123"))));
        assertEquals("{\"lamp\":\"1\"}", gson.toJson(handler.prepareCommandData(LAMP_MODE, new StringType("1"))));
        assertNull(handler.prepareCommandData(BEEP, new StringType("on")));
        assertNull(handler.prepareCommandData(LAMP_MODE, OnOffType.ON));
    }

    @Test
    public void defaultOptionsAreSetAgainWhenTheProfileChanges() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        String status = """
                {"modelid":"AC3210/12","pwr":"1","mode":"P","om":"1","dt":0,"dtrs":0}""";
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.UNICORN);
        when(connection.isPushingStatus()).thenReturn(true);
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(status, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any())).thenReturn(gson.fromJson(status, PhilipsAirPurifierDataDTO.class));
        ChannelUID fanSpeed = new ChannelUID(handler.getThing().getUID(), CONTROLS, FAN_MODE);
        ChannelUID mode = new ChannelUID(handler.getThing().getUID(), CONTROLS, MODE);
        ChannelUID timer = new ChannelUID(handler.getThing().getUID(), CONTROLS, AUTO_TIMEOFF);

        // the options of the profile are only set once for the same profile
        handler.updateData(connection);
        handler.updateData(connection);
        verify(stateDescriptionProvider, times(1)).setStateOptions(eq(fanSpeed), any());

        // a profile without options of its own gets the options of the channel types again
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.BASIC_GEN3);
        handler.updateData(connection);

        assertEquals(List.of(List.of("1", "2", "3", "4", "5", "m", "t"), List.of("s", "1", "2", "3", "t")),
                allOptions(fanSpeed));
        assertEquals(List.of(List.of("P", "S"), List.of("P", "A", "S", "M", "B", "N")), allOptions(mode));
        assertEquals(List.of(List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"),
                List.of("0", "1", "2", "3", "4", "5")), allOptions(timer));

        // and not again for the same profile
        clearInvocations(stateDescriptionProvider);
        handler.updateData(connection);
        verify(stateDescriptionProvider, never()).setStateOptions(eq(fanSpeed), any());
    }

    @Test
    public void classicDevicesHaveNoProfileOptionsButStoreTheProfile() throws PhilipsAirAPIException {
        stubConnectionConfig();
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.CLASSIC);
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        verify(stateDescriptionProvider, never())
                .setStateOptions(eq(new ChannelUID(handler.getThing().getUID(), CONTROLS, FAN_MODE)), any());
        assertEquals("CLASSIC", handler.getThing().getProperties().get(PROPERTY_DEVICE_PROFILE));
    }

    private PhilipsAirHandler handlerOf(String host, Map<String, String> properties) {
        Thing thing = PhilipsAirHandlerFixture.thing(THING_TYPE_COAP, new ThingUID(THING_TYPE_COAP, "stored"),
                PhilipsAirHandlerFixture.configuration(host), properties, List.of());
        return PhilipsAirHandlerFixture.handler(thing, httpClient, stateDescriptionProvider, callback);
    }

    private static PhilipsAirConfiguration configurationOf(String host) {
        PhilipsAirConfiguration config = new PhilipsAirConfiguration();
        config.setHost(host);
        return config;
    }

    @Test
    public void storedProfileIsOnlyUsedForTheHostItWasResolvedFor() {
        PhilipsAirHandler stored = handlerOf("1.1.1.1",
                Map.of(PROPERTY_DEVICE_PROFILE, "UNICORN", PROPERTY_DEVICE_PROFILE_HOST, "1.1.1.1"));

        assertEquals(CoapProfile.UNICORN, stored.getStoredProfile(configurationOf("1.1.1.1")));
        assertNull(stored.getStoredProfile(configurationOf("2.2.2.2")));

        assertNull(handlerOf("1.1.1.1", Map.of(PROPERTY_DEVICE_PROFILE, "UNICORN"))
                .getStoredProfile(configurationOf("1.1.1.1")));
        assertNull(handlerOf("1.1.1.1", Map.of()).getStoredProfile(configurationOf("1.1.1.1")));
    }

    @Test
    public void unknownStoredProfileIsIgnored() {
        PhilipsAirHandler stored = handlerOf("1.1.1.1",
                Map.of(PROPERTY_DEVICE_PROFILE, "FUTURE", PROPERTY_DEVICE_PROFILE_HOST, "1.1.1.1"));

        assertNull(stored.getStoredProfile(configurationOf("1.1.1.1")));
    }

    @Test
    public void statusWithAnotherProfileReplacesTheStoredProfile() throws PhilipsAirAPIException {
        stubConnectionConfig();
        PhilipsAirHandler stored = handlerOf("1.1.1.1",
                Map.of(PROPERTY_DEVICE_PROFILE, "BASIC_GEN3", PROPERTY_DEVICE_PROFILE_HOST, "1.1.1.1"));
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.UNICORN);
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class));

        stored.updateData(connection);

        Map<String, String> properties = stored.getThing().getProperties();
        assertEquals("UNICORN", properties.get(PROPERTY_DEVICE_PROFILE));
        assertEquals("1.1.1.1", properties.get(PROPERTY_DEVICE_PROFILE_HOST));
        verify(callback, times(1)).thingUpdated(any());
    }

    @Test
    public void statusWithTheStoredProfileDoesNotUpdateTheThing() throws PhilipsAirAPIException {
        stubConnectionConfig();
        PhilipsAirHandler stored = handlerOf("1.1.1.1", Map.of(PROPERTY_DEVICE_PROFILE, "UNICORN",
                PROPERTY_DEVICE_PROFILE_HOST, "1.1.1.1", Thing.PROPERTY_VENDOR, VENDOR));
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.UNICORN);
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class));

        stored.updateData(connection);
        stored.updateData(connection);

        verify(callback, never()).thingUpdated(any());
    }

    @Test
    public void filterStatusIsRequestedAgainAfterAFailure() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class));
        when(connection.getAirPurifierFiltersStatus(any())).thenThrow(new PhilipsAirAPIException("busy"))
                .thenReturn(gson.fromJson(HUMIDIFIER_STATUS, PhilipsAirPurifierFiltersDTO.class));

        handler.updateData(connection);
        assertEquals(Set.of("controls#power"), channelIds(handler.getThing().getChannels()));

        handler.updateData(connection);
        assertEquals(Set.of("controls#power", "filters#wick-filter-life"),
                channelIds(handler.getThing().getChannels()));

        // once the device answered, the filter status is not requested for the wick filter channel anymore
        handler.updateData(connection);
        verify(connection, times(2)).getAirPurifierFiltersStatus(any());
    }

    @Test
    public void filterStatusIsNoLongerRequestedAfterTheProbeAttemptsFailed() throws PhilipsAirAPIException {
        stubConnectionConfig();
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class));
        when(connection.getAirPurifierFiltersStatus(any())).thenThrow(new PhilipsAirAPIException("busy"));

        for (int i = 0; i < 5; i++) {
            handler.updateData(connection);
        }

        verify(connection, times(3)).getAirPurifierFiltersStatus(any());
        assertEquals(Set.of("controls#power"), channelIds(handler.getThing().getChannels()));
    }

    @Test
    public void displayedIndexOptionsExcludeGasOnOtherModels() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(HUMIDIFIER_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(HUMIDIFIER_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        assertEquals(List.of("0", "1"), options(CONTROLS_UI, DISPLAYED_INDEX));
    }

    @Test
    public void thresholdsOfAc4373AreSentAsText() throws PhilipsAirAPIException {
        stubConnectionConfig();
        String status = """
                {"modelid":"AC4373/10","type":"AC4373","pwr":"1","aqit":"19","ddp":"1"}""";
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(status, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any())).thenReturn(gson.fromJson(status, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        assertEquals(List.of("13", "19", "29", "40"), options(SENSORS, AIR_QUALITY_NOTIFICATION_THRESHOLD));
        assertEquals("{\"aqit\":\"29\"}",
                gson.toJson(handler.prepareCommandData(AIR_QUALITY_NOTIFICATION_THRESHOLD, new DecimalType(29))));
        assertEquals(19,
                handler.getValue(
                        new ChannelUID(handler.getThing().getUID(), SENSORS, AIR_QUALITY_NOTIFICATION_THRESHOLD),
                        gson.fromJson(status, PhilipsAirPurifierDataDTO.class), null, null));
    }

    @Test
    public void thresholdsOfOtherModelsAreSentAsNumber() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        assertEquals("{\"aqit\":7}",
                gson.toJson(handler.prepareCommandData(AIR_QUALITY_NOTIFICATION_THRESHOLD, new DecimalType(7))));
    }

    @Test
    public void optionLabelsOfGasModelsAreTheTranslatedTexts() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        assertEquals(Set.of(DISPLAYED_INDEX, AIR_QUALITY_NOTIFICATION_THRESHOLD), assertSetOptionLabelsAreTranslated());
    }

    @Test
    public void optionLabelsOfTextThresholdsAreTheTranslatedTexts() throws PhilipsAirAPIException {
        stubConnectionConfig();
        String status = """
                {"modelid":"AC4373/10","type":"AC4373","pwr":"1","aqit":"19","ddp":"1"}""";
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(status, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any())).thenReturn(gson.fromJson(status, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        assertEquals(Set.of(DISPLAYED_INDEX, AIR_QUALITY_NOTIFICATION_THRESHOLD), assertSetOptionLabelsAreTranslated());
    }

    @Test
    public void optionLabelsOfProfilesAndDefaultsAreTheTranslatedTexts() throws PhilipsAirAPIException {
        stubChannelCreation();
        stubConnectionConfig();
        String status = """
                {"modelid":"AC3210/12","pwr":"1","mode":"P","om":"1","dt":0,"dtrs":0,"beep":true,"dispbr":"115",\
                "lamp":"2"}""";
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.UNICORN);
        when(connection.isPushingStatus()).thenReturn(true);
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(status, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any())).thenReturn(gson.fromJson(status, PhilipsAirPurifierDataDTO.class));
        handler.updateData(connection);
        // the options of the channel types are set again for a profile without options of its own
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.BASIC_GEN3);
        handler.updateData(connection);

        assertEquals(Set.of(FAN_MODE, MODE, AUTO_TIMEOFF, DISPLAYED_INDEX, AIR_QUALITY_NOTIFICATION_THRESHOLD,
                DISPLAY_BRIGHTNESS, LAMP_MODE), assertSetOptionLabelsAreTranslated());
    }

    @SuppressWarnings("unchecked")
    private List<StateOption> stateOptions(String group, String channelId) {
        ArgumentCaptor<List<StateOption>> optionsCaptor = ArgumentCaptor.forClass(List.class);
        // exactly once for the channel
        verify(stateDescriptionProvider).setStateOptions(
                eq(new ChannelUID(handler.getThing().getUID(), group, channelId)), optionsCaptor.capture());
        return optionsCaptor.getValue();
    }

    private List<String> options(String group, String channelId) {
        return stateOptions(group, channelId).stream().map(StateOption::getValue).toList();
    }

    /**
     * @return the values of the options of each time the options of the channel were set, in order
     */
    @SuppressWarnings("unchecked")
    private List<List<String>> allOptions(ChannelUID channelUID) {
        ArgumentCaptor<List<StateOption>> optionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(stateDescriptionProvider, atLeastOnce()).setStateOptions(eq(channelUID), optionsCaptor.capture());
        return optionsCaptor.getAllValues().stream()
                .map(options -> options.stream().map(StateOption::getValue).toList()).toList();
    }

    /**
     * Asserts that the labels of all options that were set are the translated texts.
     *
     * @return the ids of the channels that were given options
     */
    @SuppressWarnings("unchecked")
    private Set<String> assertSetOptionLabelsAreTranslated() {
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<List<StateOption>> optionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(stateDescriptionProvider, atLeastOnce()).setStateOptions(channelCaptor.capture(),
                optionsCaptor.capture());
        Set<String> channelIds = new HashSet<>();
        for (int i = 0; i < channelCaptor.getAllValues().size(); i++) {
            String channelId = channelCaptor.getAllValues().get(i).getIdWithoutGroup();
            List<StateOption> options = optionsCaptor.getAllValues().get(i);
            OptionLabelTranslations.assertLabelsAreTranslated(channelId, options);
            // channels without default options are reset to none when their profile is not used anymore
            if (!options.isEmpty()) {
                channelIds.add(channelId);
            }
        }
        return channelIds;
    }

    private static Set<String> channelIds(List<Channel> channels) {
        return channels.stream().map(channel -> channel.getUID().getId()).collect(Collectors.toSet());
    }
}
