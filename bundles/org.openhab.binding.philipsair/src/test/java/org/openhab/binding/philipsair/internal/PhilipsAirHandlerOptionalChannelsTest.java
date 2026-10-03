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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.StateOption;

import com.google.gson.Gson;

/**
 * Tests adding the optional channels reported by the device to the thing.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    private final Gson gson = new Gson();

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) PhilipsAirAPIConnection connection;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) PhilipsAirStateDescriptionOptionProvider stateDescriptionProvider;

    private @NonNullByDefault({}) PhilipsAirHandler handler;

    @BeforeEach
    public void setUp() {
        ThingUID thingUID = new ThingUID(THING_TYPE_COAP, "test");
        Thing thing = ThingBuilder.create(THING_TYPE_COAP, thingUID).withConfiguration(new Configuration())
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CONTROLS, POWER), "Switch").build())
                .build();
        when(callback.createChannelBuilder(any(ChannelUID.class), any()))
                .thenAnswer(invocation -> ChannelBuilder.create((ChannelUID) invocation.getArgument(0), "Number"));
        handler = new PhilipsAirHandler(thing, httpClient, stateDescriptionProvider);
        handler.setCallback(callback);
        when(connection.getConfig()).thenReturn(new PhilipsAirConfiguration());
    }

    @Test
    public void reportedOptionalChannelsAreAddedOnce() throws PhilipsAirAPIException {
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
    }

    @Test
    public void tvocAndRssiChannelsAndFilterTypesAreAdded() throws PhilipsAirAPIException {
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
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        verify(callback, never()).thingUpdated(any());
        assertEquals(Set.of("controls#power"), channelIds(handler.getThing().getChannels()));
    }

    @Test
    public void wickFilterChannelIsAddedWithoutLinkedFilterChannels() throws PhilipsAirAPIException {
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
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);
        handler.updateData(connection);

        verify(stateDescriptionProvider, times(2)).setStateOptions(any(), any());
        assertEquals(List.of("0", "1", "2"), options(CONTROLS_UI, DISPLAYED_INDEX));
        assertEquals(List.of("1", "4", "7", "10"), options(SENSORS, AIR_QUALITY_NOTIFICATION_THRESHOLD));
    }

    @Test
    public void modelOptionsAreNotDecidedBeforeTheDeviceInfoIsKnown() throws PhilipsAirAPIException {
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        verify(stateDescriptionProvider, never()).setStateOptions(any(), any());

        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDeviceDTO.class));

        handler.updateData(connection);

        verify(stateDescriptionProvider, times(2)).setStateOptions(any(), any());
    }

    @Test
    public void unicornOptionsAndChannelsAreSet() throws PhilipsAirAPIException {
        // the status as translated by the connection, see CoapProfile
        String status = """
                {"modelid":"AC3210/12","range":"Unicorn","pwr":"1","mode":"P","om":"1","dt":0,"dtrs":0,"aqit":7,\
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

        assertEquals(List.of("s", "1", "2", "3", "t"), lastOptions(fanSpeed));
        assertEquals(List.of("P", "A", "S", "M", "B", "N"), lastOptions(mode));
        assertEquals(List.of("0", "1", "2", "3", "4", "5"), lastOptions(timer));

        // and not again for the same profile
        clearInvocations(stateDescriptionProvider);
        handler.updateData(connection);
        verify(stateDescriptionProvider, never()).setStateOptions(eq(fanSpeed), any());
    }

    @Test
    public void classicDevicesHaveNoProfileOptionsOrProperty() throws PhilipsAirAPIException {
        when(connection.getDeviceProfile()).thenReturn(CoapProfile.CLASSIC);
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        verify(stateDescriptionProvider, never())
                .setStateOptions(eq(new ChannelUID(handler.getThing().getUID(), CONTROLS, FAN_MODE)), any());
        assertFalse(handler.getThing().getProperties().containsKey(PROPERTY_DEVICE_PROFILE));
    }

    @Test
    public void filterStatusIsRequestedAgainAfterAFailure() throws PhilipsAirAPIException {
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
    public void displayedIndexOptionsExcludeGasOnOtherModels() throws PhilipsAirAPIException {
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(HUMIDIFIER_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(HUMIDIFIER_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        assertEquals(List.of("0", "1"), options(CONTROLS_UI, DISPLAYED_INDEX));
    }

    @Test
    public void thresholdsOfAc4373AreSentAsText() throws PhilipsAirAPIException {
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
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson(AC5659_STATUS, PhilipsAirPurifierDataDTO.class));

        handler.updateData(connection);

        assertEquals("{\"aqit\":7}",
                gson.toJson(handler.prepareCommandData(AIR_QUALITY_NOTIFICATION_THRESHOLD, new DecimalType(7))));
    }

    @Test
    public void gasIndexOfUnknownModelsDependsOnTheGasSensor() {
        PhilipsAirPurifierDeviceDTO unknownModel = gson.fromJson("{\"modelid\":\"XY1234/10\"}",
                PhilipsAirPurifierDeviceDTO.class);

        assertTrue(PhilipsAirHandler.supportsGasIndex(unknownModel,
                gson.fromJson("{\"tvoc\":1}", PhilipsAirPurifierDataDTO.class)));
        assertFalse(PhilipsAirHandler.supportsGasIndex(unknownModel,
                gson.fromJson(PURIFIER_STATUS, PhilipsAirPurifierDataDTO.class)));
        assertTrue(PhilipsAirHandler
                .supportsGasIndex(gson.fromJson("{\"type\":\"AC4558\"}", PhilipsAirPurifierDeviceDTO.class), null));
    }

    @SuppressWarnings("unchecked")
    private List<String> options(String group, String channelId) {
        ArgumentCaptor<List<StateOption>> optionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(stateDescriptionProvider).setStateOptions(
                eq(new ChannelUID(handler.getThing().getUID(), group, channelId)), optionsCaptor.capture());
        return optionsCaptor.getValue().stream().map(StateOption::getValue).toList();
    }

    @SuppressWarnings("unchecked")
    private List<String> lastOptions(ChannelUID channelUID) {
        ArgumentCaptor<List<StateOption>> optionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(stateDescriptionProvider, atLeastOnce()).setStateOptions(eq(channelUID), optionsCaptor.capture());
        List<List<StateOption>> all = optionsCaptor.getAllValues();
        return all.get(all.size() - 1).stream().map(StateOption::getValue).toList();
    }

    private static Set<String> channelIds(List<Channel> channels) {
        return channels.stream().map(channel -> channel.getUID().getId()).collect(Collectors.toSet());
    }
}
