/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.energidataservice.internal.provider;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.energidataservice.internal.provider.listener.SubscriptionListener;
import org.openhab.binding.energidataservice.internal.provider.subscription.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractProvider} is responsible for managing subscriptions.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractProvider<L extends SubscriptionListener> {

    protected final Map<L, Set<Subscription>> listenerToSubscriptions = new ConcurrentHashMap<>();
    protected final Map<Subscription, Set<L>> subscriptionToListeners = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(AbstractProvider.class);

    protected boolean subscribeInternal(L listener, Subscription subscription) {
        Set<Subscription> subscriptionsForListener = Objects
                .requireNonNull(listenerToSubscriptions.computeIfAbsent(listener, k -> ConcurrentHashMap.newKeySet()));

        if (subscriptionsForListener.contains(subscription)) {
            throw new IllegalArgumentException(
                    "Duplicate listener registration for " + listener.getClass().getName() + ": " + subscription);
        }

        subscriptionsForListener.add(subscription);

        Set<L> listenersForSubscription = subscriptionToListeners.get(subscription);
        boolean isFirstDistinctSubscription = false;
        if (listenersForSubscription == null) {
            isFirstDistinctSubscription = true;
            listenersForSubscription = ConcurrentHashMap.newKeySet();
            subscriptionToListeners.put(subscription, listenersForSubscription);
        }

        listenersForSubscription.add(listener);
        logger.debug("Listener {} started {}", listener, subscription);

        return isFirstDistinctSubscription;
    }

    protected boolean unsubscribeInternal(L listener, Subscription subscription) {
        Set<Subscription> listenerSubscriptions = listenerToSubscriptions.get(listener);

        if (listenerSubscriptions == null || !listenerSubscriptions.contains(subscription)) {
            throw new IllegalArgumentException(
                    "Listener is not subscribed to the specified subscription: " + subscription);
        }

        listenerSubscriptions.remove(subscription);

        if (listenerSubscriptions.isEmpty()) {
            listenerToSubscriptions.remove(listener);
        }

        Set<L> listenersForSubscription = subscriptionToListeners.get(subscription);
        boolean isLastDistinctSubscription = false;
        if (listenersForSubscription != null) {
            listenersForSubscription.remove(listener);

            if (listenersForSubscription.isEmpty()) {
                subscriptionToListeners.remove(subscription);
                isLastDistinctSubscription = true;
            }
        }

        logger.debug("Listener {} stopped {}", listener, subscription);

        return isLastDistinctSubscription;
    }

    public void unsubscribe(L listener) {
        Set<Subscription> listenerSubscriptions = listenerToSubscriptions.get(listener);
        if (listenerSubscriptions == null) {
            return;
        }
        for (Subscription subscription : listenerSubscriptions) {
            unsubscribeInternal(listener, subscription);
        }
    }

    protected Set<L> getListeners(Subscription subscription) {
        return subscriptionToListeners.getOrDefault(subscription, ConcurrentHashMap.newKeySet());
    }
}
