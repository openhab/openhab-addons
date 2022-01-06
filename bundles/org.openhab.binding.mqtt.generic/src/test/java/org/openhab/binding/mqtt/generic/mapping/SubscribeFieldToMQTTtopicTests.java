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

import org.eclipse.jdt.annotation.NonNull;
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
@MockitoSettings(strictness = Strictness.WARN)
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

        public @TestValue("string") String aString;
        public @TestValue("false") Boolean aBoolean;
        public @TestValue("10") Long aLong;
        public @TestValue("10") Integer aInteger;
        public @TestValue("10") BigDecimal aDecimal;

        public @TestValue("10") @TopicPrefix("a") int Int = 24;
        public @TestValue("false") boolean aBool = true;
        public @TestValue("abc,def") @MQTTvalueTransform(splitCharacter = ",") String[] properties;

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
        public @NonNull Object getFieldsOf() {
            return this;
        }
    }

    Attributes attributes = new Attributes();

    @Mock
    MqttBrokerConnection connection;

    @Mock
    SubscribeFieldToMQTTtopic fieldSubscriber;

    @Mock
    FieldChanged fieldChanged;

    @BeforeEach
    public void setUp() {
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
    }

    @Test
    public void TimeoutIfNoMessageReceive()
            throws InterruptedException, NoSuchFieldException, ExecutionException, TimeoutException {
        final Field field = Attributes.class.getField("Int");
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        SubscribeFieldToMQTTtopic subscriber = new SubscribeFieldToMQTTtopic(scheduler, field, fieldChanged,
                "homie/device123", false);
        assertThrows(TimeoutException.class,
                () -> subscriber.subscribeAndReceive(connection, 1000).get(50, TimeUnit.MILLISECONDS));
    }

    @Test
    public void MandatoryMissing()
            throws InterruptedException, NoSuchFieldException, ExecutionException, TimeoutException {
        final Field field = Attributes.class.getField("Int");
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        SubscribeFieldToMQTTtopic subscriber = new SubscribeFieldToMQTTtopic(scheduler, field, fieldChanged,
                "homie/device123", true);
        assertThrows(ExecutionException.class, () -> subscriber.subscribeAndReceive(connection, 50).get());
    }

    @Test
    public void MessageReceive()
            throws InterruptedException, NoSuchFieldException, ExecutionException, TimeoutException {
        final FieldChanged changed = (field, value) -> {
            try {
                field.set(attributes.getFieldsOf(), value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                fail(e.getMessage());
            }
        };
        final Field field = Attributes.class.getField("Int");
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        SubscribeFieldToMQTTtopic subscriber = new SubscribeFieldToMQTTtopic(scheduler, field, changed,
                "homie/device123", false);
        CompletableFuture<@Nullable Void> future = subscriber.subscribeAndReceive(connection, 1000);

        // Simulate a received MQTT message
        subscriber.processMessage("ignored", "10".getBytes());
        // No timeout should happen
        future.get(50, TimeUnit.MILLISECONDS);
        assertThat(attributes.Int, is(10));
    }
}
