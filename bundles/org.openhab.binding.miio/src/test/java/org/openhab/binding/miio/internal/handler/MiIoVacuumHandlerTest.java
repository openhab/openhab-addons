/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.miio.internal.MiIoBindingConstants;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.cloud.MiCloudException;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelTypeRegistry;

import com.google.gson.JsonParser;

/**
 * Test case for {@link MiIoVacuumHandler}
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MiIoVacuumHandlerTest {

    private @NonNullByDefault({}) MiIoVacuumHandler miIoHandler;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private @Mock @NonNullByDefault({}) CloudConnector cloudConnector;
    private @Mock @NonNullByDefault({}) MiIoDatabaseWatchService miIoDatabaseWatchService;
    private @Mock @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistry;
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) MiIoAsyncCommunication connection;
    private @NonNullByDefault({}) @Mock TranslationProvider translationProvider;
    private @NonNullByDefault({}) @Mock LocaleProvider localeProvider;

    private final Configuration configuration = new Configuration();
    private ThingUID thingUID = new ThingUID(MiIoBindingConstants.THING_TYPE_VACUUM, "TestThing");

    @BeforeEach
    public void setUp() throws IOException, MiCloudException {
        configuration.put(MiIoBindingConstants.PROPERTY_HOST_IP, "localhost");
        configuration.put(MiIoBindingConstants.PROPERTY_TOKEN, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        configuration.put(MiIoBindingConstants.PROPERTY_DID, "AABBCCDDEEFF");
        configuration.put(MiIoBindingConstants.PROPERTY_CLOUDSERVER, "fake");
        configuration.put("communication", "cloud");

        when(thing.getConfiguration()).thenReturn(configuration);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getThingTypeUID()).thenReturn(MiIoBindingConstants.THING_TYPE_VACUUM);
        when(cloudConnector.sendRPCCommand(any(), any(), any())).thenReturn("{\"result\":\"triggerError\"}");
        lenient().when(callback.isChannelLinked(any())).thenReturn(true);

        miIoHandler = new MiIoVacuumHandler(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry,
                translationProvider, localeProvider);

        miIoHandler.setCallback(callback);
    }

    @AfterEach
    public void after() {
        miIoHandler.dispose();
    }

    @Test
    public void testInitializeShouldCallTheCallback() throws InterruptedException {
        miIoHandler.initialize();
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        ThingStatusInfo thingStatusInfo = statusInfoCaptor.getValue();
        assertEquals(ThingStatus.OFFLINE, thingStatusInfo.getStatus(), "Device should be OFFLINE");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void TestCleanRecord() {
        miIoHandler.initialize();
        // prepare a CLEAN_RECORD_GET response object
        String cmdString = "{\"id\":7028,\"method\":\"get_clean_record\",\"params\":[1699081963]}";
        String jsonResponseTxt = "{\"result\":[[1724174413,1724174459,246,770000,0,0,2,3,60]],\"id\":7028}";

        MiIoSendCommand response = new MiIoSendCommand(13, MiIoCommand.CLEAN_RECORD_GET,
                JsonParser.parseString(cmdString).getAsJsonObject(), "", "");
        response.setResponse(JsonParser.parseString(jsonResponseTxt).getAsJsonObject());
        miIoHandler.onMessageReceived(response);

        verify(callback, description("Test the start time parsing")).stateUpdated(
                eq(new ChannelUID(thingUID, MiIoBindingConstants.CHANNEL_HISTORY_START_TIME)),
                eq(new DateTimeType(ZonedDateTime.parse("2024-08-20T19:20:13+02:00")).toZone(ZoneId.systemDefault())));

        verify(callback, description("Test the end time parsing")).stateUpdated(
                eq(new ChannelUID(thingUID, MiIoBindingConstants.CHANNEL_HISTORY_END_TIME)),
                eq(new DateTimeType(ZonedDateTime.parse("2024-08-20T19:20:59+02:00")).toZone(ZoneId.systemDefault())));

        verify(callback, description("Test the duration parsing")).stateUpdated(
                eq(new ChannelUID(thingUID, MiIoBindingConstants.CHANNEL_HISTORY_DURATION)),
                eq(new QuantityType<>(4, Units.MINUTE)));
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void TestCleanSummary() {
        miIoHandler.initialize();

        ThingStatusInfo ts = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "I fake to be online");
        when(thing.getStatusInfo()).thenReturn(ts);

        // prepare a CLEAN_SUMMARY_GET response object
        String cmdString = "{\"id\":114,\"method\":\"get_clean_summary\",\"params\":[]}";
        String jsonResponseTxt = "{\"id\":114,\"result\":{\"clean_time\":109968,\"clean_area\":1694875000,\"clean_count\":51,\"dust_collection_count\":48,\"records\":[1699081963,1698999875,1698126572,1697463736,1697031817,1696486642,1696320557,1696253060,1695833343,1695821201,1695619374,1695476013,1695457865,1695274110,1695014622,1694876238,1694860994,1694755927,1694526730,1694237806]}}";

        MiIoSendCommand response = new MiIoSendCommand(13, MiIoCommand.CLEAN_SUMMARY_GET,
                JsonParser.parseString(cmdString).getAsJsonObject(), "", "");
        response.setResponse(JsonParser.parseString(jsonResponseTxt).getAsJsonObject());
        miIoHandler.onMessageReceived(response);

        verify(callback, description("Test clean time")).stateUpdated(
                eq(new ChannelUID(thingUID, MiIoBindingConstants.CHANNEL_HISTORY_TOTALTIME)),
                eq(new QuantityType<>(TimeUnit.MINUTES.convert(109968, TimeUnit.SECONDS), Units.MINUTE)));

        verify(callback, description("Test the area parsing")).stateUpdated(
                eq(new ChannelUID(thingUID, MiIoBindingConstants.CHANNEL_HISTORY_TOTALAREA)),
                eq(new QuantityType<>(1694.875, SIUnits.SQUARE_METRE)));
    }
}
