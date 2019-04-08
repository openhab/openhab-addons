/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import static org.junit.Assert.*;
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
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.mapping.MQTTvalueTransform;
import org.openhab.binding.mqtt.generic.mapping.SubscribeFieldToMQTTtopic;
import org.openhab.binding.mqtt.generic.mapping.TopicPrefix;
import org.openhab.binding.mqtt.generic.mapping.SubscribeFieldToMQTTtopic.FieldChanged;

/**
 * Tests cases for {@link SubscribeFieldToMQTTtopic}.
 *
 * @author David Graeff - Initial contribution
 */
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
    };

    Attributes attributes = new Attributes();

    @Mock
    MqttBrokerConnection connection;

    @Mock
    SubscribeFieldToMQTTtopic fieldSubscriber;

    @Mock
    FieldChanged fieldChanged;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
    }

    @Test(expected = TimeoutException.class)
    public void TimeoutIfNoMessageReceive()
            throws InterruptedException, NoSuchFieldException, ExecutionException, TimeoutException {
        final Field field = Attributes.class.getField("Int");
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        SubscribeFieldToMQTTtopic subscriber = new SubscribeFieldToMQTTtopic(scheduler, field, fieldChanged,
                "homie/device123", false);
        subscriber.subscribeAndReceive(connection, 1000).get(50, TimeUnit.MILLISECONDS);
    }

    @Test(expected = ExecutionException.class)
    public void MandatoryMissing()
            throws InterruptedException, NoSuchFieldException, ExecutionException, TimeoutException {
        final Field field = Attributes.class.getField("Int");
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        SubscribeFieldToMQTTtopic subscriber = new SubscribeFieldToMQTTtopic(scheduler, field, fieldChanged,
                "homie/device123", true);
        subscriber.subscribeAndReceive(connection, 50).get();
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
