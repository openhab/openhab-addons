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
package org.openhab.io.homekit.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.StateChangeListener;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;

/**
 * Subscribes and unsubscribes from Item changes to enable notification to HomeKit
 * clients. Each item/key pair (key is optional) should be unique, as the underlying
 * HomeKit library takes care of insuring only a single subscription exists for
 * each accessory.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitAccessoryUpdater {
    private final Logger logger = LoggerFactory.getLogger(HomekitAccessoryUpdater.class);
    private final ConcurrentMap<ItemKey, StateChangeListener> subscriptionsByName = new ConcurrentHashMap<>();

    public void subscribe(GenericItem item, HomekitCharacteristicChangeCallback callback) {
        subscribe(item, null, callback);
    }

    public void subscribe(GenericItem item, String key, HomekitCharacteristicChangeCallback callback) {
        logger.trace("Received subscription request for {} / {}", item, key);
        if (item == null) {
            return;
        }
        if (callback == null) {
            logger.trace("The received subscription contains a null callback, skipping");
            return;
        }
        ItemKey itemKey = new ItemKey(item, key);
        subscriptionsByName.compute(itemKey, (k, v) -> {
            if (v != null) {
                logger.debug("Received duplicate subscription for {} / {}", item, key);
                unsubscribe(item, key);
            }
            logger.trace("Adding subscription for {} / {}", item, key);
            Subscription subscription = (changedItem, oldState, newState) -> callback.changed();
            item.addStateChangeListener(subscription);
            return subscription;
        });
    }

    public void subscribeToUpdates(GenericItem item, String key, Consumer<State> callback) {
        logger.trace("Received subscription request for {} / {}", item, key);
        if (item == null) {
            return;
        }
        if (callback == null) {
            logger.trace("The received subscription contains a null callback, skipping");
            return;
        }
        ItemKey itemKey = new ItemKey(item, key);
        subscriptionsByName.compute(itemKey, (k, v) -> {
            if (v != null) {
                logger.debug("Received duplicate subscription for {} / {}", item, key);
                unsubscribe(item, key);
            }
            logger.trace("Adding subscription for {} / {}", item, key);
            UpdateSubscription subscription = (changedItem, newState) -> callback.accept(newState);
            item.addStateChangeListener(subscription);
            return subscription;
        });
    }

    public void unsubscribe(GenericItem item) {
        unsubscribe(item, null);
    }

    public void unsubscribe(GenericItem item, String key) {
        if (item == null) {
            return;
        }
        subscriptionsByName.computeIfPresent(new ItemKey(item, key), (k, v) -> {
            logger.trace("Removing existing subscription for {} / {}", item, key);
            item.removeStateChangeListener(v);
            return null;
        });
    }

    @FunctionalInterface
    @NonNullByDefault
    private interface Subscription extends StateChangeListener {

        @Override
        void stateChanged(Item item, State oldState, State newState);

        @Override
        default void stateUpdated(Item item, State state) {
            // Do nothing on non-change update
        }
    }

    @FunctionalInterface
    @NonNullByDefault
    private interface UpdateSubscription extends StateChangeListener {

        @Override
        default void stateChanged(Item item, State oldState, State newState) {
            // Do nothing on change update
        }

        @Override
        void stateUpdated(Item item, State state);
    }

    private static class ItemKey {
        public final GenericItem item;
        public final String key;

        public ItemKey(GenericItem item, String key) {
            this.item = item;
            this.key = key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((item == null) ? 0 : item.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ItemKey other = (ItemKey) obj;
            if (item == null) {
                if (other.item != null) {
                    return false;
                }
            } else if (!item.equals(other.item)) {
                return false;
            }
            if (key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!key.equals(other.key)) {
                return false;
            }
            return true;
        }
    }
}
