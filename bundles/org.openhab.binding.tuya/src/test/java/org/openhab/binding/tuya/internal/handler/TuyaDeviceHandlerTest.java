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
package org.openhab.binding.tuya.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.BINDING_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_DEVICE_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_DP;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_LOCAL_KEY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_PRODUCT_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_RELOAD_SCHEMA;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_DEVICE;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.SchemaReloadException;
import org.openhab.binding.tuya.internal.TuyaDynamicCommandDescriptionProvider;
import org.openhab.binding.tuya.internal.TuyaDynamicStateDescriptionProvider;
import org.openhab.binding.tuya.internal.TuyaSchemaDB;
import org.openhab.binding.tuya.internal.TuyaSchemaService;
import org.openhab.binding.tuya.internal.local.UdpDiscoveryListener;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

import com.google.gson.Gson;

import io.netty.channel.EventLoopGroup;

/**
 * The {@link TuyaDeviceHandlerTest} verifies channel reconciliation and the schema reload trigger of the device
 * handler.
 *
 * @author Carlo Dischler - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class TuyaDeviceHandlerTest {
    private static final String PRODUCT_ID = "handlertestprod";
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_TUYA_DEVICE, "handlertest");

    private @Mock @NonNullByDefault({}) TuyaDynamicCommandDescriptionProvider dynamicCommandDescriptionProviderMock;
    private @Mock @NonNullByDefault({}) TuyaDynamicStateDescriptionProvider dynamicStateDescriptionProviderMock;
    private @Mock @NonNullByDefault({}) EventLoopGroup eventLoopGroupMock;
    private @Mock @NonNullByDefault({}) UdpDiscoveryListener udpDiscoveryListenerMock;
    private @Mock @NonNullByDefault({}) TuyaSchemaService schemaServiceMock;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callbackMock;

    private @NonNullByDefault({}) TuyaDeviceHandler handler;

    @BeforeEach
    public void setUp() {
        Map<String, SchemaDp> schema = new LinkedHashMap<>();
        schema.put("switch", schemaDp(1, "switch", "bool", false));
        schema.put("fault", schemaDp(22, "fault", "bitmap", true));
        TuyaSchemaDB.cache.put(PRODUCT_ID, schema);

        lenient().when(callbackMock.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class)))
                .thenAnswer(invocation -> {
                    ChannelUID channelUID = invocation.getArgument(0);
                    ChannelTypeUID channelTypeUID = invocation.getArgument(1);
                    return ChannelBuilder.create(channelUID).withType(channelTypeUID);
                });
    }

    @AfterEach
    public void tearDown() {
        handler.dispose();
        TuyaSchemaDB.cache.remove(PRODUCT_ID);
    }

    @Test
    public void initializeDropsGeneratedChannelsOfRemovedDatapointsAndKeepsOtherChannels() {
        // "fault" was generated before with a different type, "old_code" no longer exists in the schema
        Channel staleFault = generatedChannel("fault", 22);
        Channel removed = generatedChannel("old_code", 23);
        Channel otherProduct = channel("other_code", new ChannelTypeUID(BINDING_ID, "otherprod_other_code"), 24);
        Channel customId = channel("my_fault", new ChannelTypeUID(BINDING_ID, PRODUCT_ID + "_fault"), 22);
        Channel manual = channel("manual", new ChannelTypeUID(BINDING_ID, "switch"), 5);
        createHandler(List.of(staleFault, removed, otherProduct, customId, manual), Map.of());

        handler.initialize();

        List<Channel> channels = handler.getThing().getChannels();
        assertEquals(List.of("switch", "fault", "other_code", "my_fault", "manual"),
                channels.stream().map(channel -> channel.getUID().getId()).toList());
        assertEquals(new ChannelTypeUID(BINDING_ID, PRODUCT_ID + "_fault"), channels.get(1).getChannelTypeUID());
    }

    @Test
    public void initializeKeepsAllChannelsWithoutSchema() {
        TuyaSchemaDB.cache.remove(PRODUCT_ID);
        createHandler(List.of(generatedChannel("old_code", 23)), Map.of());

        handler.initialize();

        assertEquals(List.of("old_code"),
                handler.getThing().getChannels().stream().map(channel -> channel.getUID().getId()).toList());
    }

    @Test
    public void reloadOptionResetsItselfAndReloadsSchemaOfProduct() throws Exception {
        CountDownLatch reinitialized = new CountDownLatch(1);
        when(schemaServiceMock.reloadSchema(PRODUCT_ID, "device"))
                .thenReturn(CompletableFuture.completedFuture(List.of()));
        when(schemaServiceMock.reinitializeThings(PRODUCT_ID)).thenAnswer(invocation -> {
            reinitialized.countDown();
            return List.of(THING_UID);
        });
        createHandler(List.of(), Map.of(CONFIG_RELOAD_SCHEMA, true));

        handler.initialize();

        assertEquals(false, handler.getThing().getConfiguration().get(CONFIG_RELOAD_SCHEMA));
        // The thing keeps working with the current schema until the thing manager replaces the handler.
        verify(udpDiscoveryListenerMock).registerListener(eq("device"), any());
        assertTrue(reinitialized.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void reloadOptionKeepsThingRunningWhenReloadFails() {
        when(schemaServiceMock.reloadSchema(PRODUCT_ID, "device")).thenReturn(
                CompletableFuture.failedFuture(new SchemaReloadException("no Tuya cloud project is connected")));
        createHandler(List.of(), Map.of(CONFIG_RELOAD_SCHEMA, true));

        handler.initialize();

        assertEquals(false, handler.getThing().getConfiguration().get(CONFIG_RELOAD_SCHEMA));
        assertEquals(List.of("switch", "fault"),
                handler.getThing().getChannels().stream().map(channel -> channel.getUID().getId()).toList());
        verify(udpDiscoveryListenerMock).registerListener(eq("device"), any());
        verify(schemaServiceMock, never()).reinitializeThings(anyString());
    }

    private void createHandler(List<Channel> channels, Map<String, Object> additionalConfiguration) {
        Map<String, Object> configuration = new HashMap<>(
                Map.of(CONFIG_PRODUCT_ID, PRODUCT_ID, CONFIG_DEVICE_ID, "device", CONFIG_LOCAL_KEY, "key"));
        configuration.putAll(additionalConfiguration);
        Thing thing = ThingBuilder.create(THING_TYPE_TUYA_DEVICE, THING_UID)
                .withConfiguration(new Configuration(configuration)).withChannels(channels).build();
        handler = new TuyaDeviceHandler(thing, new Gson(), dynamicCommandDescriptionProviderMock,
                dynamicStateDescriptionProviderMock, eventLoopGroupMock, udpDiscoveryListenerMock, schemaServiceMock);
        handler.setCallback(callbackMock);
    }

    private static Channel generatedChannel(String channelId, int dp) {
        return channel(channelId, new ChannelTypeUID(BINDING_ID, PRODUCT_ID + "_" + channelId), dp);
    }

    private static Channel channel(String channelId, ChannelTypeUID channelTypeUID, int dp) {
        return ChannelBuilder.create(new ChannelUID(THING_UID, channelId)).withType(channelTypeUID)
                .withConfiguration(new Configuration(Map.of(CONFIG_DP, dp))).build();
    }

    private static SchemaDp schemaDp(int id, String code, String type, boolean readOnly) {
        SchemaDp schemaDp = new SchemaDp();
        schemaDp.id = id;
        schemaDp.code = code;
        schemaDp.type = type;
        schemaDp.readOnly = readOnly;
        return schemaDp;
    }
}
