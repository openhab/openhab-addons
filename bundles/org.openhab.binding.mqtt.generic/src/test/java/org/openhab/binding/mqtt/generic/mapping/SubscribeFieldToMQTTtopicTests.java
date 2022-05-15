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
package org.openhab.binding.mqtt.generic.mapping;

import static java.lang.annotation.ElementType.FIELD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.mapping.SubscribeFieldToMQTTtopic.FieldChanged;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;

/**
 * Tests cases for {@link org.openhab.binding.mqtt.generic.mapping.SubscribeFieldToMQTTtopic}.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class SubscribeFieldToMQTTtopicTests {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ FIELD })
    private @interface TestValue {
        String value() default "";
    }

    @TopicPrefix
    public static class Attributes extends AbstractMqttAttributeClass {
        @SuppressWarnings("unused")
        public transient String ignoreTransient = "";
        @SuppressWarnings("unused")
        public final String ignoreFinal = "";

        public @TestValue("string") @Nullable String aString;
        public @TestValue("false") @Nullable Boolean aBoolean;
        public @TestValue("10") @Nullable Long aLong;
        public @TestValue("10") @Nullable Integer aInteger;
        public @TestValue("10") @Nullable BigDecimal aDecimal;

        public @TestValue("10") @TopicPrefix("a") int aInt = 24;
        public @TestValue("false") boolean aBool = true;
        public @TestValue("abc,def") @MQTTvalueTransform(splitCharacter = ",") String @Nullable [] properties;

        public enum ReadyState {
            unknown,
            init,
            ready,
        }

        public @TestValue("init") ReadyState state = ReadyState.unknown;

        public enum DataTypeEnum {
            unknown,
            integer_,
            float_,
        }

        public @TestValue("integer") @MQTTvalueTransform(suffix = "_") DataTypeEnum datatype = DataTypeEnum.unknown;

        @Override
        public Object getFieldsOf() {
            return this;
        }
    }

    Attributes attributes = new Attributes();

    private @Mock @NonNullByDefault({}) MqttBrokerConnection connectionMock;
    private @Mock @NonNullByDefault({}) FieldChanged fieldChangedMock;

    @BeforeEach
    public void setUp() {
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).subscribe(any(), any());
    }

    @Test
    public void timeoutIfNoMessageReceive() throws Exception {
        final Field field = Attributes.class.getField("aInt");
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        SubscribeFieldToMQTTtopic subscriber = new SubscribeFieldToMQTTtopic(scheduler, field, fieldChangedMock,
                "homie/device123", false);
        assertThrows(TimeoutException.class,
                () -> subscriber.subscribeAndReceive(connectionMock, 1000).get(50, TimeUnit.MILLISECONDS));
    }

    @Test
    public void mandatoryMissing() throws Exception {
        final Field field = Attributes.class.getField("aInt");
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        SubscribeFieldToMQTTtopic subscriber = new SubscribeFieldToMQTTtopic(scheduler, field, fieldChangedMock,
                "homie/device123", true);
        assertThrows(ExecutionException.class, () -> subscriber.subscribeAndReceive(connectionMock, 50).get());
    }

    @Test
    public void messageReceive() throws Exception {
        final FieldChanged changed = (field, value) -> {
            try {
                field.set(attributes.getFieldsOf(), value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                fail(e.getMessage());
            }
        };
        final Field field = Attributes.class.getField("aInt");
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        SubscribeFieldToMQTTtopic subscriber = new SubscribeFieldToMQTTtopic(scheduler, field, changed,
                "homie/device123", false);
        CompletableFuture<@Nullable Void> future = subscriber.subscribeAndReceive(connectionMock, 1000);

        // Simulate a received MQTT message
        subscriber.processMessage("ignored", "10".getBytes());
        // No timeout should happen
        future.get(50, TimeUnit.MILLISECONDS);
        assertThat(attributes.aInt, is(10));
    }
}
