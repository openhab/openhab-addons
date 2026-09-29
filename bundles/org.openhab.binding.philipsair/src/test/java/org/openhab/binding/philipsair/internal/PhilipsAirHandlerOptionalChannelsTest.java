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
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIConnection;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIException;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
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

    private static Set<String> channelIds(List<Channel> channels) {
        return channels.stream().map(channel -> channel.getUID().getId()).collect(Collectors.toSet());
    }
}
