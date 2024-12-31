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
package org.openhab.binding.mqtt.generic;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.util.ColorUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Tests the {@link ChannelState} class.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class ChannelStateTests {

    private @Mock @NonNullByDefault({}) MqttBrokerConnection connectionMock;
    private @Mock @NonNullByDefault({}) ChannelStateUpdateListener channelStateUpdateListenerMock;
    private @Mock @NonNullByDefault({}) ChannelUID channelUIDMock;
    private @Spy @NonNullByDefault({}) TextValue textValue;

    private @NonNullByDefault({}) ScheduledExecutorService scheduler;

    private ChannelConfig config = ChannelConfigBuilder.create("state", "command").build();

    @BeforeEach
    public void setUp() {
        CompletableFuture<@Nullable Void> voidFutureComplete = new CompletableFuture<>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connectionMock).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).publish(any(), any(), anyInt(),
                anyBoolean());

        scheduler = new ScheduledThreadPoolExecutor(1);
    }

    @AfterEach
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    public void noInteractionTimeoutTest() throws Exception {
        ChannelState c = spy(new ChannelState(config, channelUIDMock, textValue, channelStateUpdateListenerMock));
        c.start(connectionMock, scheduler, 50).get(100, TimeUnit.MILLISECONDS);
        verify(connectionMock).subscribe(eq("state"), eq(c));
        c.stop().get();
        verify(connectionMock).unsubscribe(eq("state"), eq(c));
    }

    @Test
    public void publishFormatTest() throws Exception {
        ChannelState c = spy(new ChannelState(config, channelUIDMock, textValue, channelStateUpdateListenerMock));

        c.start(connectionMock, scheduler, 0).get(50, TimeUnit.MILLISECONDS);
        verify(connectionMock).subscribe(eq("state"), eq(c));

        c.publishValue(new StringType("UPDATE")).get();
        verify(connectionMock).publish(eq("command"), argThat(p -> Arrays.equals(p, "UPDATE".getBytes())), anyInt(),
                eq(false));

        c.config.formatBeforePublish = "prefix%s";
        c.publishValue(new StringType("UPDATE")).get();
        verify(connectionMock).publish(eq("command"), argThat(p -> Arrays.equals(p, "prefixUPDATE".getBytes())),
                anyInt(), eq(false));

        c.config.formatBeforePublish = "%1$s-%1$s";
        c.publishValue(new StringType("UPDATE")).get();
        verify(connectionMock).publish(eq("command"), argThat(p -> Arrays.equals(p, "UPDATE-UPDATE".getBytes())),
                anyInt(), eq(false));

        c.config.formatBeforePublish = "%s";
        c.config.retained = true;
        c.publishValue(new StringType("UPDATE")).get();
        verify(connectionMock).publish(eq("command"), any(), anyInt(), eq(true));

        c.stop().get();
        verify(connectionMock).unsubscribe(eq("state"), eq(c));
    }

    @Test
    public void publishStopTest() throws Exception {
        ChannelConfig config = ChannelConfigBuilder.create("state", "command").build();
        config.stop = "STOP";
        ChannelState c = spy(new ChannelState(config, channelUIDMock, textValue, channelStateUpdateListenerMock));

        c.start(connectionMock, scheduler, 0).get(50, TimeUnit.MILLISECONDS);
        verify(connectionMock).subscribe(eq("state"), eq(c));

        c.publishValue(StopMoveType.STOP).get();
        verify(connectionMock).publish(eq("command"), argThat(p -> Arrays.equals(p, "STOP".getBytes())), anyInt(),
                eq(false));

        c.stop().get();
        verify(connectionMock).unsubscribe(eq("state"), eq(c));
    }

    @Test
    public void publishStopSeparateTopicTest() throws Exception {
        ChannelConfig config = ChannelConfigBuilder.create("state", "command").withStopCommandTopic("stopCommand")
                .build();
        config.stop = "STOP";
        ChannelState c = spy(new ChannelState(config, channelUIDMock, textValue, channelStateUpdateListenerMock));

        c.start(connectionMock, scheduler, 0).get(50, TimeUnit.MILLISECONDS);
        verify(connectionMock).subscribe(eq("state"), eq(c));

        c.publishValue(StopMoveType.STOP).get();
        verify(connectionMock).publish(eq("stopCommand"), argThat(p -> Arrays.equals(p, "STOP".getBytes())), anyInt(),
                eq(false));

        c.stop().get();
        verify(connectionMock).unsubscribe(eq("state"), eq(c));
    }

    @Test
    public void receiveWildcardTest() throws Exception {
        ChannelState c = spy(new ChannelState(ChannelConfigBuilder.create("state/+/topic", "command").build(),
                channelUIDMock, textValue, channelStateUpdateListenerMock));

        CompletableFuture<@Nullable Void> future = c.start(connectionMock, scheduler, 100);
        c.processMessage("state/bla/topic", "A TEST".getBytes());
        future.get(300, TimeUnit.MILLISECONDS);

        assertThat(textValue.getChannelState().toString(), is("A TEST"));
        verify(channelStateUpdateListenerMock).updateChannelState(eq(channelUIDMock), any());
    }

    @Test
    public void receiveStringTest() throws Exception {
        ChannelState c = spy(new ChannelState(config, channelUIDMock, textValue, channelStateUpdateListenerMock));

        CompletableFuture<@Nullable Void> future = c.start(connectionMock, scheduler, 100);
        c.processMessage("state", "A TEST".getBytes());
        future.get(300, TimeUnit.MILLISECONDS);

        assertThat(textValue.getChannelState().toString(), is("A TEST"));
        verify(channelStateUpdateListenerMock).updateChannelState(eq(channelUIDMock), any());
    }

    @Test
    public void receiveDecimalTest() {
        NumberValue value = new NumberValue(null, null, new BigDecimal(10), null);
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "15".getBytes());
        assertThat(value.getChannelState().toString(), is("15"));

        c.processMessage("state", "INCREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("25"));

        c.processMessage("state", "DECREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("15"));

        verify(channelStateUpdateListenerMock, times(3)).updateChannelState(eq(channelUIDMock), any());
    }

    @Test
    public void receiveDecimalFractionalTest() {
        NumberValue value = new NumberValue(null, null, new BigDecimal(10.5), null);
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "5.5".getBytes());
        assertThat(value.getChannelState().toString(), is("5.5"));

        c.processMessage("state", "INCREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("16.0"));
    }

    @Test
    public void receiveDecimalUnitTest() {
        NumberValue value = new NumberValue(null, null, new BigDecimal(10), Units.WATT);
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "15".getBytes());
        assertThat(value.getChannelState().toString(), is("15 W"));

        c.processMessage("state", "INCREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("25 W"));

        c.processMessage("state", "DECREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("15 W"));

        verify(channelStateUpdateListenerMock, times(3)).updateChannelState(eq(channelUIDMock), any());
    }

    @Test
    public void receiveDecimalAsPercentageUnitTest() {
        NumberValue value = new NumberValue(null, null, new BigDecimal(10), Units.PERCENT);
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "63.7".getBytes());
        assertThat(value.getChannelState().toString(), is("63.7 %"));

        verify(channelStateUpdateListenerMock, times(1)).updateChannelState(eq(channelUIDMock), any());
    }

    @Test
    public void receivePercentageTest() {
        PercentageValue value = new PercentageValue(new BigDecimal(-100), new BigDecimal(100), new BigDecimal(10), null,
                null, null);
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "-100".getBytes()); // 0%
        assertThat(value.getChannelState().toString(), is("0"));

        c.processMessage("state", "100".getBytes()); // 100%
        assertThat(value.getChannelState().toString(), is("100"));

        c.processMessage("state", "0".getBytes()); // 50%
        assertThat(value.getChannelState().toString(), is("50"));

        c.processMessage("state", "INCREASE".getBytes());
        assertThat(value.getChannelState().toString(), is("55"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("10"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), "%03.0f"), is("010"));
    }

    @Test
    public void receiveRGBColorTest() {
        ColorValue value = new ColorValue(ColorMode.RGB, "FON", "FOFF", 10);
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "ON".getBytes()); // Normal on state
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("26,26,26"));

        c.processMessage("state", "FOFF".getBytes()); // Custom off state
        assertThat(value.getChannelState().toString(), is("0,0,0"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("0,0,0"));

        c.processMessage("state", "10".getBytes()); // Brightness only
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("26,26,26"));

        HSBType t = HSBType.fromRGB(12, 18, 231);

        c.processMessage("state", "12,18,231".getBytes());
        assertThat(value.getChannelState(), is(t)); // HSB
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("12,18,231"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), "%3$d,%2$d,%1$d"), is("231,18,12"));
    }

    @Test
    public void receiveHSBColorTest() {
        ColorValue value = new ColorValue(ColorMode.HSB, "FON", "FOFF", 10);
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "ON".getBytes()); // Normal on state
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("0,0,10"));

        c.processMessage("state", "FOFF".getBytes()); // Custom off state
        assertThat(value.getChannelState().toString(), is("0,0,0"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("0,0,0"));

        c.processMessage("state", "10".getBytes()); // Brightness only
        assertThat(value.getChannelState().toString(), is("0,0,10"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("0,0,10"));

        c.processMessage("state", "12,18,100".getBytes());
        assertThat(value.getChannelState().toString(), is("12,18,100"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("12,18,100"));
    }

    @Test
    public void receiveXYYColorTest() {
        ColorValue value = new ColorValue(ColorMode.XYY, "FON", "FOFF", 10);
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        // incoming messages
        c.processMessage("state", "ON".getBytes()); // Normal on state
        assertThat(value.getChannelState().toString(), is("0,0,10"));

        c.processMessage("state", "FOFF".getBytes()); // Custom off state
        // note we don't care what color value is currently stored, just that brightness is off
        assertThat(((HSBType) value.getChannelState()).getBrightness(), is(PercentType.ZERO));

        c.processMessage("state", "10".getBytes()); // Brightness only
        assertThat(value.getChannelState().toString(), is("0,0,10"));

        HSBType t = ColorUtil.xyToHsb(new double[] { 0.3f, 0.6f });
        c.processMessage("state", "0.3,0.6,100".getBytes());
        assertTrue(((HSBType) value.getChannelState()).closeTo(t, 0.001)); // HSB

        // outgoing messages
        // these use the 0.3,0.6,100 from above, but care more about proper formatting of the outgoing message
        // than about the precise value (since color conversions have happened)
        assertCloseTo(value.getMQTTpublishValue((Command) value.getChannelState(), null), "0.300000,0.600000,100.00");
        assertCloseTo(value.getMQTTpublishValue((Command) value.getChannelState(), "%3$.1f,%2$.2f,%1$.2f"),
                "100.0,0.60,0.30");
    }

    // also ensures the string elements are the same _length_, i.e. the correct precision for each element
    private void assertCloseTo(String aString, String bString) {
        String[] aElements = aString.split(",");
        String[] bElements = bString.split(",");
        double[] a = Arrays.stream(aElements).mapToDouble(Double::parseDouble).toArray();
        double[] b = Arrays.stream(bElements).mapToDouble(Double::parseDouble).toArray();
        for (int i = 0; i < a.length; i++) {
            assertThat(aElements[i].length(), is(bElements[i].length()));
            assertThat(a[i], closeTo(b[i], 0.002));
        }
    }

    @Test
    public void receiveLocationTest() {
        LocationValue value = new LocationValue();
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        c.processMessage("state", "46.833974, 7.108433".getBytes());
        assertThat(value.getChannelState().toString(), is("46.833974,7.108433"));
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is("46.833974,7.108433"));
    }

    @Test
    public void receiveDateTimeTest() {
        DateTimeValue value = new DateTimeValue();
        ChannelState subject = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        subject.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        ZonedDateTime zd = ZonedDateTime.now();
        String datetime = zd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        subject.processMessage("state", datetime.getBytes());

        String channelState = value.getChannelState().toString();
        assertTrue(channelState.startsWith(datetime),
                "Expected '" + channelState + "' to start with '" + datetime + "'");
        assertThat(value.getMQTTpublishValue((Command) value.getChannelState(), null), is(datetime));
    }

    @Test
    public void receiveImageTest() {
        ImageValue value = new ImageValue();
        ChannelState c = spy(new ChannelState(config, channelUIDMock, value, channelStateUpdateListenerMock));
        c.start(connectionMock, mock(ScheduledExecutorService.class), 100);

        byte[] payload = { (byte) 0xFF, (byte) 0xD8, 0x01, 0x02, (byte) 0xFF, (byte) 0xD9 };
        c.processMessage("state", payload);
        assertThat(value.getChannelState(), is(instanceOf(RawType.class)));
        assertThat(((RawType) value.getChannelState()).getMimeType(), is("image/jpeg"));
    }

    @Nested
    public class TransformationTests {
        // Copied from org.openhab.core.thing.binding.generic.ChannelTransformationTest
        private static final String T1_NAME = "TRANSFORM1";
        private static final String T1_PATTERN = "T1Pattern";
        private static final String T1_INPUT = "T1Input";
        private static final String T1_RESULT = "T1Result";

        private static final String NULL_INPUT = "nullInput";
        private static final @Nullable String NULL_RESULT = null;

        private @Mock @NonNullByDefault({}) TransformationService transformationService1Mock;

        private @Mock @NonNullByDefault({}) BundleContext bundleContextMock;
        private @Mock @NonNullByDefault({}) ServiceReference<TransformationService> serviceRef1Mock;

        private @NonNullByDefault({}) TransformationHelper transformationHelper;

        @BeforeEach
        public void init() throws TransformationException {
            Mockito.when(transformationService1Mock.transform(eq(T1_PATTERN), eq(T1_INPUT)))
                    .thenAnswer(answer -> T1_RESULT);
            Mockito.when(transformationService1Mock.transform(eq(T1_PATTERN), eq(NULL_INPUT)))
                    .thenAnswer(answer -> NULL_RESULT);

            Mockito.when(serviceRef1Mock.getProperty(any())).thenReturn("TRANSFORM1");

            Mockito.when(bundleContextMock.getService(serviceRef1Mock)).thenReturn(transformationService1Mock);

            transformationHelper = new TransformationHelper(bundleContextMock);
            transformationHelper.setTransformationService(serviceRef1Mock);
        }

        @AfterEach
        public void tearDown() {
            transformationHelper.deactivate();
        }

        @Test
        public void transformationPatternTest() throws Exception {
            ChannelConfig config = ChannelConfigBuilder.create("state", "command")
                    .withTransformationPattern(List.of(T1_NAME + ":" + T1_PATTERN)).build();
            ChannelState c = spy(new ChannelState(config, channelUIDMock, textValue, channelStateUpdateListenerMock));

            CompletableFuture<@Nullable Void> future = c.start(connectionMock, scheduler, 100);
            c.processMessage("state", T1_INPUT.getBytes());
            future.get(300, TimeUnit.MILLISECONDS);

            assertThat(textValue.getChannelState().toString(), is(T1_RESULT));
            verify(channelStateUpdateListenerMock).updateChannelState(eq(channelUIDMock), any());
        }

        @Test
        public void transformationPatternReturningNullTest() throws Exception {
            ChannelConfig config = ChannelConfigBuilder.create("state", "command")
                    .withTransformationPattern(List.of(T1_NAME + ":" + T1_PATTERN)).build();
            ChannelState c = spy(new ChannelState(config, channelUIDMock, textValue, channelStateUpdateListenerMock));

            // First, test with an input that doesn't get transformed to null
            CompletableFuture<@Nullable Void> future = c.start(connectionMock, scheduler, 100);
            c.processMessage("state", T1_INPUT.getBytes());
            future.get(300, TimeUnit.MILLISECONDS);

            assertThat(textValue.getChannelState().toString(), is(T1_RESULT));
            verify(channelStateUpdateListenerMock).updateChannelState(eq(channelUIDMock), any());

            clearInvocations(channelStateUpdateListenerMock);

            // now test with an input that gets transformed to null
            future = c.start(connectionMock, scheduler, 100);
            c.processMessage("state", NULL_INPUT.getBytes());
            future.get(300, TimeUnit.MILLISECONDS);

            // textValue should not have been updated
            assertThat(textValue.getChannelState().toString(), is(T1_RESULT));
            verify(channelStateUpdateListenerMock, never()).updateChannelState(eq(channelUIDMock), any());
        }

        @Test
        public void transformationPatternOutTest() throws Exception {
            ChannelConfig config = ChannelConfigBuilder.create("state", "command")
                    .withTransformationPatternOut(List.of(T1_NAME + ":" + T1_PATTERN)).build();
            ChannelState c = spy(new ChannelState(config, channelUIDMock, textValue, channelStateUpdateListenerMock));

            c.start(connectionMock, scheduler, 0).get(50, TimeUnit.MILLISECONDS);
            verify(connectionMock).subscribe(eq("state"), eq(c));

            c.publishValue(new StringType(T1_INPUT)).get();
            verify(connectionMock).publish(eq("command"), argThat(p -> Arrays.equals(p, T1_RESULT.getBytes())),
                    anyInt(), eq(false));
        }
    }
}
