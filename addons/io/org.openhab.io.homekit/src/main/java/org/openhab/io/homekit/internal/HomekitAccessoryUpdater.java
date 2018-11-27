/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

/**
 * Subscribes and unsubscribes from Item changes to enable notification to Homekit
 * clients. Each item/key pair (key is optional) should be unique, as the underlying
 * Homekit library takes care of insuring only a single subscription exists for
 * each accessory.
 *
 * @author Andy Lintner
 */
public class HomekitAccessoryUpdater {

    private Logger logger = LoggerFactory.getLogger(HomekitAccessoryUpdater.class);
    private final ConcurrentMap<ItemKey, Subscription> subscriptionsByName = new ConcurrentHashMap<>();

    public void subscribe(GenericItem item, HomekitCharacteristicChangeCallback callback) {
        subscribe(item, null, callback);
    }

    public void subscribe(GenericItem item, String key, HomekitCharacteristicChangeCallback callback) {
        if (item == null) {
            return;
        }
        ItemKey itemKey = new ItemKey(item, key);
        if (subscriptionsByName.containsKey(itemKey)) {
            logger.error("Received duplicate subscription on {}", item.getName());
        }
        subscriptionsByName.compute(itemKey, (k, v) -> {
            if (v != null) {
                logger.error("Received duplicate subscription on {}", item.getName());
                unsubscribe(item, key);
            }
            Subscription subscription = (changedItem, oldState, newState) -> callback.changed();
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
            item.removeStateChangeListener(v);
            return null;
        });
    }

    @FunctionalInterface
    private static interface Subscription extends StateChangeListener {

        @Override
        void stateChanged(Item item, State oldState, State newState);

        @Override
        default void stateUpdated(Item item, State state) {
            // Do nothing on non-change update
        }
    }

    private static class ItemKey {
        public GenericItem item;
        public String key;

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
