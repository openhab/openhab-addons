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
package org.openhab.binding.goecharger.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.goecharger.internal.api.GoEStatusResponseBaseDTO;
import org.openhab.binding.goecharger.internal.api.GoEStatusResponseV2DTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link GoEChargerV2Handler}.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
public class GoEChargerV2HandlerTest {

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_GOE, "test");

    private final Thing thing = mock(Thing.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private @Nullable GoEChargerBaseHandler handler;

    @BeforeEach
    public void setUp() {
        when(thing.getUID()).thenReturn(THING_UID);
        when(thing.getConfiguration())
                .thenReturn(new Configuration(Map.of("ip", "127.0.0.1", "refreshInterval", 1, "apiVersion", 2)));
        Channel channel = ChannelBuilder.create(new ChannelUID(THING_UID, TEMPERATURE_TYPE2_PORT), "Number:Temperature")
                .build();
        when(thing.getChannels()).thenReturn(List.of(channel));
    }

    @AfterEach
    public void tearDown() {
        GoEChargerBaseHandler handler = this.handler;
        if (handler != null) {
            handler.dispose();
        }
    }

    private GoEChargerV2Handler createHandler() {
        GoEChargerV2Handler handler = new GoEChargerV2Handler(thing, mock(HttpClient.class));
        handler.setCallback(callback);
        this.handler = handler;
        return handler;
    }

    @Test
    public void nullArrayElementsResultInUndef() {
        GoEChargerV2Handler handler = createHandler();
        GoEStatusResponseV2DTO response = new GoEStatusResponseV2DTO();
        response.temperatures = new Double[] { null, null };
        response.energy = new Double[] { 230d, null, 231d };

        assertThat(handler.getValue(TEMPERATURE_TYPE2_PORT, response), is(UnDefType.UNDEF));
        assertThat(handler.getValue(TEMPERATURE_CIRCUIT_BOARD, response), is(UnDefType.UNDEF));
        assertThat(handler.getValue(VOLTAGE_L2, response), is(UnDefType.UNDEF));
        assertThat(handler.getValue(VOLTAGE_L1, response), is(new QuantityType<>(230d, Units.VOLT)));
        assertThat(handler.getValue(VOLTAGE_L3, response), is(new QuantityType<>(231d, Units.VOLT)));
    }

    @Test
    public void tooShortArraysResultInUndef() {
        GoEChargerV2Handler handler = createHandler();
        GoEStatusResponseV2DTO response = new GoEStatusResponseV2DTO();
        response.temperatures = new Double[] { 25.5 };
        response.energy = new Double[] { 230d };

        assertThat(handler.getValue(TEMPERATURE_TYPE2_PORT, response), is(UnDefType.UNDEF));
        assertThat(handler.getValue(TEMPERATURE_CIRCUIT_BOARD, response), is(UnDefType.UNDEF));
        assertThat(handler.getValue(VOLTAGE_L2, response), is(UnDefType.UNDEF));
        assertThat(handler.getValue(POWER_ALL, response), is(UnDefType.UNDEF));
    }

    @Test
    public void nullFieldsResultInUndef() {
        GoEChargerV2Handler handler = createHandler();
        GoEStatusResponseV2DTO response = new GoEStatusResponseV2DTO();

        assertThat(handler.getValue(SESSION_CHARGE_CONSUMPTION_LIMIT, response), is(UnDefType.UNDEF));
        assertThat(handler.getValue(TEMPERATURE_TYPE2_PORT, response), is(UnDefType.UNDEF));
        assertThat(handler.getValue(VOLTAGE_L1, response), is(UnDefType.UNDEF));
    }

    @Test
    public void validValuesAreConverted() {
        GoEChargerV2Handler handler = createHandler();
        GoEStatusResponseV2DTO response = new GoEStatusResponseV2DTO();
        response.temperatures = new Double[] { 25.5, 30d };

        assertThat(handler.getValue(TEMPERATURE_TYPE2_PORT, response), is(new QuantityType<>(25.5, SIUnits.CELSIUS)));
        assertThat(handler.getValue(TEMPERATURE_CIRCUIT_BOARD, response), is(new QuantityType<>(30d, SIUnits.CELSIUS)));
    }

    @Test
    public void refreshJobSurvivesRuntimeException() throws InterruptedException {
        CountDownLatch refreshed = new CountDownLatch(2);
        GoEChargerV2Handler handler = new GoEChargerV2Handler(thing, mock(HttpClient.class)) {
            @Override
            protected @Nullable GoEStatusResponseBaseDTO getGoEData() {
                refreshed.countDown();
                throw new IllegalStateException("simulated unexpected response");
            }
        };
        handler.setCallback(callback);
        this.handler = handler;

        handler.initialize();

        assertTrue(refreshed.await(10, TimeUnit.SECONDS), "refresh job stopped after an unexpected exception");
        verify(callback, timeout(10_000).atLeastOnce()).statusUpdated(any(),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
        verify(callback, timeout(10_000).atLeastOnce()).stateUpdated(new ChannelUID(THING_UID, TEMPERATURE_TYPE2_PORT),
                UnDefType.UNDEF);
    }
}
