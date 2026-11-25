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
package org.openhab.binding.homie.internal.homie300;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.mapping.SubscribeFieldToMQTTtopic;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;

/**
 * Binds field mutations to the metadata subscription that created each subscriber.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
abstract class HomieAttributes extends AbstractMqttAttributeClass {
    private final transient AtomicReference<SubscriptionContext> fieldSubscription = new AtomicReference<>(
            new SubscriptionContext());

    @Override
    public CompletableFuture<@Nullable Void> subscribeAndReceive(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, String basetopic, @Nullable AttributeChanged listener, int timeout) {
        CompletableFuture<@Nullable Void> predecessor = new CompletableFuture<>();
        SubscriptionContext context = new SubscriptionContext(predecessor);
        SubscriptionContext previous = fieldSubscription.getAndSet(context);
        previous.stop(super::unsubscribe).whenComplete((v, error) -> {
            if (error == null) {
                predecessor.complete(null);
            } else {
                predecessor.completeExceptionally(error);
            }
        });
        return context.run(() -> super.subscribeAndReceive(connection, scheduler, basetopic, listener, timeout))
                .thenCompose(v -> context.run(() -> CompletableFuture.completedFuture(null)));
    }

    @Override
    public CompletableFuture<@Nullable Void> unsubscribe() {
        return fieldSubscription.get().stop(super::unsubscribe);
    }

    @Override
    public SubscribeFieldToMQTTtopic createSubscriber(ScheduledExecutorService scheduler, Field field, String topic,
            boolean mandatory) {
        SubscriptionContext context = fieldSubscription.get();
        // The shared mapper mutates fields before invoking its listener. Guard that mutation itself,
        // and drain any field mutation already running before installing the next mapper listener.
        return new SubscribeFieldToMQTTtopic(scheduler, field, (changedField, value) -> context.run(() -> {
            super.fieldChanged(changedField, value);
            return CompletableFuture.completedFuture(null);
        }), topic, mandatory);
    }
}
