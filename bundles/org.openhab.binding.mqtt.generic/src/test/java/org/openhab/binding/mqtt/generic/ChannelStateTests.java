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
package org.openhab.binding.mqtt.generic;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.DateTimeValue;
import org.openhab.binding.mqtt.generic.values.ImageValue;
import org.openhab.binding.mqtt.generic.values.LocationValue;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;

/**
 * Tests the {@link ChannelState} class.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class ChannelStateTests {

    private @Mock MqttBrokerConnection connection;
    private @Mock ChannelStateUpdateListener channelStateUpdateListener;
    private @Mock ChannelUID channelUID;
    private @Spy TextValue textValue;

    private ScheduledExecutorService scheduler;

    private ChannelConfig config = ChannelConfigBuilder.create("state", "command").build();

    @BeforeEach
    public void setUp() {
        CompletableFuture<Void> voidFutureComplete = new CompletableFuture<>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any(), anyInt(),
                anyBoolean());

        scheduler = new ScheduledThreadPoolExecutor(1);
    }

    @AfterEach
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    public void noInteractionTimeoutTest() throws InterruptedException, ExecutionException, TimeoutException {
        ChannelState c = spy(new ChannelState(config, channelUID, textValue, channelStateUpdateListener));
        c.start(connection, scheduler, 50).get(100, TimeUnit.MILLISECONDS);
        verify(connection).subscribe(eq("state"), eq(c));
        c.stop().get();
        verify(connection).unsubscribe(eq("state"), eq(c));
    }

    @Test
    public void publishFormatTest() throws InterruptedException, ExecutionException, TimeoutException {
        ChannelState c = spy(new ChannelState(config, channelUID, textValue, channelStateUpdateListener));

        c.start(connection, scheduler, 0).get(50, TimeUnit.MILLISECONDS);
        verify(connection).subscribe(eq("state"), eq(c));

        c.publishValue(new StringType("UPDATE")).get();
        verify(connection).publish(eq("command"), argThat(p -> Arrays.equals(p, "UPDATE".getBytes())), anyInt(),
                eq(false));

        c.config.formatBeforePublish = "prefix%s";
        c.publishValue(new StringType("UPDATE")).get();
        verify(connection).publish(eq("command"), argThat(p -> Arrays.equals(p, "prefixUPDATE".getBytes())), anyInt(),
                eq(false));

        c.config.formatBeforePublish = "%1$s-%1$s";
        c.publishValue(new StringType("UPDATE")).get();
        verify(connection).publish(eq("command"), argThat(p -> Arrays.equals(p, "UPDATE-UPDATE".getBytes())), anyInt(),
                eq(false));

        c.config.formatBeforePublish = "%s";
        c.config.retained = true;
        c.publishValue(new StringType("UPDATE")).get();
        verify(connection).publish(eq("command"), any(), anyInt(), eq(true));

        c.stop().get();
        verify(connection).unsubscribe(eq("state"), eq(c));
    }

    @Test
    public void receiveWildcardTest() throws InterruptedException, ExecutionException, TimeoutException {
        ChannelState c = spy(new ChannelState(ChannelConfigBuilder.create("state/+/topic", "command").build(),
                channelUID, textValue, channelStateUpdateListener));

        CompletableFuture<@Nullable Void> future = c.start(connection, scheduler, 100);
        c.processMessage("state/bla/topic", "A TEST".getBytes());
        future.get(300, TimeUnit.MILLISECONDS);

        assertThat(textValue.getChannelState().toString(), is("A TEST"));
        verify(channelStateUpdateListener).updateChannelState(eq(channelUID), any());
    }

    @Test
    public void receiveStringTest() throws InterruptedException, ExecutionException, TimeoutException {
        ChannelState c = spy(new ChannelState(config, channelUID, textValue, channelStateUpdateListener));

        CompletableFuture<@Nullable Void> future = c.start(connection, scheduler, 100);
        c.processMessage("state", "A TEST".getBytes());
        future.get(300, TimeUnit.MILLISECONDS);

        assertThat(textValue.getChannelState().toString(), is("A TEST"));
        verify(channelStateUpdateListener).updateChannelState(eq(channelUID), any());
    }

    @Test
    public void receiveDecimalTest() {
        NumberValue value = new NumberValue(null, null, new BigDecimal(10), null);
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "15".getBytes());
        assertThat(value.getChannelState().toString(), is("15"));

        c.processMessage("state", "INCREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("25"));

        c.processMessage("state", "DECREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("15"));

        verify(channelStateUpdateListener, times(3)).updateChannelState(eq(channelUID), any());
    }

    @Test
    public void receiveDecimalFractionalTest() {
        NumberValue value = new NumberValue(null, null, new BigDecimal(10.5), null);
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "5.5".getBytes());
        assertThat(value.getChannelState().toString(), is("5.5"));

        c.processMessage("state", "INCREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("16.0"));
    }

    @Test
    public void receiveDecimalUnitTest() {
        NumberValue value = new NumberValue(null, null, new BigDecimal(10), Units.WATT);
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "15".getBytes());
        assertThat(value.getChannelState().toString(), is("15 W"));

        c.processMessage("state", "INCREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("25 W"));

        c.processMessage("state", "DECREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("15 W"));

        verify(channelStateUpdateListener, times(3)).updateChannelState(eq(channelUID), any());
    }

    @Test
    public void receiveDecimalAsPercentageUnitTest() {
        NumberValue value = new NumberValue(null, null, new BigDecimal(10), Units.PERCENT);
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "63.7".getBytes());
        assertThat(value.getChannelState().toString(), is("63.7 %"));

        verify(channelStateUpdateListener, times(1)).updateChannelState(eq(channelUID), any());
    }

    @Test
    public void receivePercentageTest() {
        PercentageValue value = new PercentageValue(new BigDecimal(-100), new BigDecimal(100), new BigDecimal(10), null,
                null);
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "-100".getBytes()); // 0%
        assertThat(value.getChannelState().toString(), is("0"));

        c.processMessage("state", "100".getBytes()); // 100%
        assertThat(value.getChannelState().toString(), is("100"));

        c.processMessage("state", "0".getBytes()); // 50%
        assertThat(value.getChannelState().toString(), is("50"));

        c.processMessage("state", "INCREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("55"));
        assertThat(value.getMQTTpublishValue(null), is("10"));
        assertThat(value.getMQTTpublishValue("%03.0f"), is("010"));
    }

    @Test
    public void receiveRGBColorTest() {
        ColorValue value = new ColorValue(ColorMode.RGB, "FON", "FOFF", 10);
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "ON".getBytes()); // Normal on state
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue(null), is("25,25,25"));

        c.processMessage("state", "FOFF".getBytes()); // Custom off state
        assertThat(value.getChannelState().toString(), is("0,0,0"));
        assertThat(value.getMQTTpublishValue(null), is("0,0,0"));

        c.processMessage("state", "10".getBytes()); // Brightness only
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue(null), is("25,25,25"));

        HSBType t = HSBType.fromRGB(12, 18, 231);

        c.processMessage("state", "12,18,231".getBytes());
        assertThat(value.getChannelState(), is(t)); // HSB
        // rgb -> hsv -> rgb is quite lossy
        assertThat(value.getMQTTpublishValue(null), is("13,20,225"));
        assertThat(value.getMQTTpublishValue("%3$d,%2$d,%1$d"), is("225,20,13"));
    }

    @Test
    public void receiveHSBColorTest() {
        ColorValue value = new ColorValue(ColorMode.HSB, "FON", "FOFF", 10);
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "ON".getBytes()); // Normal on state
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue(null), is("0,0,10"));

        c.processMessage("state", "FOFF".getBytes()); // Custom off state
        assertThat(value.getChannelState().toString(), is("0,0,0"));
        assertThat(value.getMQTTpublishValue(null), is("0,0,0"));

        c.processMessage("state", "10".getBytes()); // Brightness only
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue(null), is("0,0,10"));

        c.processMessage("state", "12,18,100".getBytes());
        assertThat(value.getChannelState().toString(), is("12,18,100"));
        assertThat(value.getMQTTpublishValue(null), is("12,18,100"));
    }

    @Test
    public void receiveXYYColorTest() {
        ColorValue value = new ColorValue(ColorMode.XYY, "FON", "FOFF", 10);
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "ON".getBytes()); // Normal on state
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue(null), is("0.312716,0.329002,10.00"));

        c.processMessage("state", "FOFF".getBytes()); // Custom off state
        assertThat(value.getChannelState().toString(), is("0,0,0"));
        assertThat(value.getMQTTpublishValue(null), is("0.312716,0.329002,0.00"));

        c.processMessage("state", "10".getBytes()); // Brightness only
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue(null), is("0.312716,0.329002,10.00"));

        HSBType t = HSBType.fromXY(0.3f, 0.6f);

        c.processMessage("state", "0.3,0.6,100".getBytes());
        assertThat(value.getChannelState(), is(t)); // HSB
        assertThat(value.getMQTTpublishValue(null), is("0.300000,0.600000,100.00"));
        assertThat(value.getMQTTpublishValue("%3$.1f,%2$.4f,%1$.4f"), is("100.0,0.6000,0.3000"));
    }

    @Test
    public void receiveLocationTest() {
        LocationValue value = new LocationValue();
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "46.833974, 7.108433".getBytes());
        assertThat(value.getChannelState().toString(), is("46.833974,7.108433"));
        assertThat(value.getMQTTpublishValue(null), is("46.833974,7.108433"));
    }

    @Test
    public void receiveDateTimeTest() {
        DateTimeValue value = new DateTimeValue();
        ChannelState subject = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        subject.start(connection, mock(ScheduledExecutorService.class), 100);

        ZonedDateTime zd = ZonedDateTime.now();
        String datetime = zd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        subject.processMessage("state", datetime.getBytes());

        String channelState = value.getChannelState().toString();
        assertTrue(channelState.startsWith(datetime),
                "Expected '" + channelState + "' to start with '" + datetime + "'");
        assertThat(value.getMQTTpublishValue(null), is(datetime));
    }

    @Test
    public void receiveImageTest() {
        ImageValue value = new ImageValue();
        ChannelState c = spy(new ChannelState(config, channelUID, value, channelStateUpdateListener));
        c.start(connection, mock(ScheduledExecutorService.class), 100);

        byte[] payload = new byte[] { (byte) 0xFF, (byte) 0xD8, 0x01, 0x02, (byte) 0xFF, (byte) 0xD9 };
        c.processMessage("state", payload);
        assertThat(value.getChannelState(), is(instanceOf(RawType.class)));
        assertThat(((RawType) value.getChannelState()).getMimeType(), is("image/jpeg"));
    }
}
