/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass.AttributeChanged;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;

/**
 * Tests cases for {@link org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass}.
 *
 * <p>
 * How it works:
 *
 * <ol>
 * <li>A DTO (data transfer object) is defined, here it is {@link Attributes}, which extends
 * {@link org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass}.
 * <li>The createSubscriber method is mocked so that no real MQTTConnection interaction happens.
 * <li>The subscribeAndReceive method is called.
 * </ol>
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MqttTopicClassMapperTests {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ FIELD })
    private @interface TestValue {
        String value() default "";
    }

    @TopicPrefix
    public static class Attributes extends AbstractMqttAttributeClass {
        public transient String ignoreTransient = "";
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

    private @Mock @NonNullByDefault({}) MqttBrokerConnection connectionMock;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService executorMock;
    private @Mock @NonNullByDefault({}) AttributeChanged fieldChangedObserverMock;
    private @Spy Object countInjectedFields = new Object();

    int injectedFields = 0;

    // A completed future is returned for a subscribe call to the attributes
    final CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);

    @BeforeEach
    public void setUp() {
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).unsubscribe(any(), any());
        injectedFields = (int) Stream.of(countInjectedFields.getClass().getDeclaredFields())
                .filter(AbstractMqttAttributeClass::filterField).count();
    }

    public Object createSubscriberAnswer(InvocationOnMock invocation) {
        final AbstractMqttAttributeClass attributes = (AbstractMqttAttributeClass) invocation.getMock();
        final ScheduledExecutorService scheduler = (ScheduledExecutorService) invocation.getArguments()[0];
        final Field field = (Field) invocation.getArguments()[1];
        final String topic = (String) invocation.getArguments()[2];
        final boolean mandatory = (boolean) invocation.getArguments()[3];
        final SubscribeFieldToMQTTtopic s = spy(
                new SubscribeFieldToMQTTtopic(scheduler, field, attributes, topic, mandatory));
        doReturn(CompletableFuture.completedFuture(true)).when(s).subscribeAndReceive(any(), anyInt());
        return s;
    }

    @Test
    public void subscribeToCorrectFields() {
        Attributes attributes = spy(new Attributes());

        doAnswer(this::createSubscriberAnswer).when(attributes).createSubscriber(any(), any(), anyString(),
                anyBoolean());

        // Subscribe now to all fields
        CompletableFuture<@Nullable Void> future = attributes.subscribeAndReceive(connectionMock, executorMock,
                "homie/device123", null, 10);
        assertThat(future.isDone(), is(true));
        assertThat(attributes.subscriptions.size(), is(10 + injectedFields));
    }

    // TODO timeout
    @SuppressWarnings({ "null", "unused" })
    @Test
    public void subscribeAndReceive() throws Exception {
        final Attributes attributes = spy(new Attributes());

        doAnswer(this::createSubscriberAnswer).when(attributes).createSubscriber(any(), any(), anyString(),
                anyBoolean());

        verify(connectionMock, times(0)).subscribe(anyString(), any());

        // Subscribe now to all fields
        CompletableFuture<@Nullable Void> future = attributes.subscribeAndReceive(connectionMock, executorMock,
                "homie/device123", fieldChangedObserverMock, 10);
        assertThat(future.isDone(), is(true));

        // We expect 10 subscriptions now
        assertThat(attributes.subscriptions.size(), is(10 + injectedFields));

        int loopCounter = 0;

        // Assign each field the value of the test annotation via the processMessage method
        for (SubscribeFieldToMQTTtopic f : attributes.subscriptions) {
            @Nullable
            TestValue annotation = f.field.getAnnotation(TestValue.class);
            // A non-annotated field means a Mockito injected field.
            // Ignore that and complete the corresponding future.
            if (annotation == null) {
                f.future.complete(null);
                continue;
            }

            verify(f).subscribeAndReceive(any(), anyInt());

            // Simulate a received MQTT value and use the annotation data as input.
            f.processMessage(f.topic, annotation.value().getBytes());
            verify(fieldChangedObserverMock, times(++loopCounter)).attributeChanged(any(), any(), any(), any(),
                    anyBoolean());

            // Check each value if the assignment worked
            if (!f.field.getType().isArray()) {
                assertNotNull(f.field.get(attributes), f.field.getName() + " is null");
                // Consider if a mapToField was used that would manipulate the received value
                MQTTvalueTransform mapToField = f.field.getAnnotation(MQTTvalueTransform.class);
                String prefix = mapToField != null ? mapToField.prefix() : "";
                String suffix = mapToField != null ? mapToField.suffix() : "";
                assertThat(f.field.get(attributes).toString(), is(prefix + annotation.value() + suffix));
            } else {
                String[] attributeArray = (String[]) f.field.get(attributes);
                assertNotNull(attributeArray);
                Objects.requireNonNull(attributeArray);
                assertThat(Stream.of(attributeArray).reduce((v, i) -> v + "," + i).orElse(""), is(annotation.value()));
            }
        }

        assertThat(future.isDone(), is(true));
    }

    @Test
    public void ignoresInvalidEnum() throws Exception {
        final Attributes attributes = spy(new Attributes());

        doAnswer(this::createSubscriberAnswer).when(attributes).createSubscriber(any(), any(), anyString(),
                anyBoolean());

        verify(connectionMock, times(0)).subscribe(anyString(), any());

        // Subscribe now to all fields
        CompletableFuture<@Nullable Void> future = attributes.subscribeAndReceive(connectionMock, executorMock,
                "homie/device123", fieldChangedObserverMock, 10);
        assertThat(future.isDone(), is(true));

        SubscribeFieldToMQTTtopic field = attributes.subscriptions.stream().filter(f -> f.field.getName() == "state")
                .findFirst().get();
        field.processMessage(field.topic, "garbage".getBytes());
        verify(fieldChangedObserverMock, times(0)).attributeChanged(any(), any(), any(), any(), anyBoolean());
        assertThat(attributes.state.toString(), is("unknown"));
    }
}
