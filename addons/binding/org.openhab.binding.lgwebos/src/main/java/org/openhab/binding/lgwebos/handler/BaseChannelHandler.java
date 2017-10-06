/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;

/**
 * An abstract implementation of ChannelHander which serves as a base class for all concrete instances.
 *
 * @author Sebastian Prehn
 * @since 1.8.0
 */
abstract class BaseChannelHandler<T> implements ChannelHandler {
    private Logger logger = LoggerFactory.getLogger(BaseChannelHandler.class);

    // IP to Subscriptions map
    private Map<String, ServiceSubscription<T>> subscriptions;

    // lazy init
    private synchronized Map<String, ServiceSubscription<T>> getSubscriptions() {
        if (subscriptions == null) {
            subscriptions = new ConcurrentHashMap<String, ServiceSubscription<T>>();
        }
        return subscriptions;
    }

    @Override
    public void onDeviceReady(final ConnectableDevice device, final String channelId, final LGWebOSHandler handler) {
        // NOP
    }

    @Override
    public void onDeviceRemoved(final ConnectableDevice device, final String channelId, final LGWebOSHandler handler) {
        // NOP
    }

    @Override
    public final synchronized void refreshSubscription(final ConnectableDevice device, final String channelId,
            final LGWebOSHandler handler) {
        removeAnySubscription(device);
        if (handler.isChannelInUse(channelId)) { // only listen if least one item is configured for this channel
            ServiceSubscription<T> listener = getSubscription(device, channelId, handler);
            if (listener != null) {
                logger.debug("Subscribed {} on IP: {}", this.getClass().getName(), device.getIpAddress());
                getSubscriptions().put(device.getIpAddress(), listener);
            }
        }
    }

    /**
     * Creates a subscription instance for this device. This may return <code>null</code> if no subscription is possible
     * or required.
     *
     * @param device device to which state changes to subscribe to
     * @param channelID channel ID
     * @param handler
     * @return instance or <code>null</code> if no subscription is possible or required
     */
    protected ServiceSubscription<T> getSubscription(final ConnectableDevice device, final String channelId,
            LGWebOSHandler handler) {
        return null;
    }

    @Override
    public final synchronized void removeAnySubscription(final ConnectableDevice device) {
        if (subscriptions != null) { // only if subscriptions was initialized (lazy loading)
            ServiceSubscription<T> l = subscriptions.remove(device.getIpAddress());
            if (l != null) {
                l.unsubscribe();
                logger.debug("Unsubscribed {} on IP: {}", this.getClass().getName(), device.getIpAddress());
            }
        }
    }

    protected <O> ResponseListener<O> createDefaultResponseListener() {
        return new ResponseListener<O>() {

            @Override
            public void onError(ServiceCommandError error) {
                logger.warn("{}: received error response: {}.", BaseChannelHandler.this.getClass().getName(), error);
            }

            @Override
            public void onSuccess(O object) {
                logger.debug("{}: {}.", BaseChannelHandler.this.getClass().getName(), object);
            }
        };
    }

}
