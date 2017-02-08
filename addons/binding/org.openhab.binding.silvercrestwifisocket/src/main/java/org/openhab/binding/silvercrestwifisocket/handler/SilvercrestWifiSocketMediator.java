/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.silvercrestwifisocket.handler;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.silvercrestwifisocket.SilvercrestWifiSocketBindingConstants;
import org.openhab.binding.silvercrestwifisocket.discovery.SilvercrestWifiSocketDiscoveryService;
import org.openhab.binding.silvercrestwifisocket.internal.entities.SilvercrestWifiSocketResponse;
import org.openhab.binding.silvercrestwifisocket.internal.runnable.SilvercrestWifiSocketUpdateReceiverRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SilvercrestWifiSocketMediator} is responsible for receiving all the UDP packets and route correctly to
 * each handler.
 *
 * @author Jaime Vaz - Initial contribution
 */
public class SilvercrestWifiSocketMediator {

    private final Logger logger = LoggerFactory.getLogger(SilvercrestWifiSocketMediator.class);

    private final Map<Thing, SilvercrestWifiSocketHandler> handlersRegisteredByThing = new HashMap<>();

    private SilvercrestWifiSocketUpdateReceiverRunnable receiver;
    private Thread receiverThread;

    private final SilvercrestWifiSocketDiscoveryService silvercrestDiscoveryService;

    /**
     * Constructor of the mediator. The discovery service must be passed for notify when one new wifi socket has been
     * found.
     *
     * @param silvercrestDiscoveryService the {@link SilvercrestWifiSocketDiscoveryService}
     */
    public SilvercrestWifiSocketMediator(final SilvercrestWifiSocketDiscoveryService silvercrestDiscoveryService) {
        this.silvercrestDiscoveryService = silvercrestDiscoveryService;
        this.initMediatorWifiSocketUpdateReceiverRunnable();
    }

    /**
     * This method is called by the {@link SilvercrestWifiSocketUpdateReceiverRunnable}, when one new message has been
     * received.
     *
     * @param receivedMessage the {@link SilvercrestWifiSocketResponse} message.
     */
    public void processReceivedPacket(final SilvercrestWifiSocketResponse receivedMessage) {
        logger.debug("Received packet from: {} with content: [{}]", receivedMessage.getHostAddress(),
                receivedMessage.getType());

        SilvercrestWifiSocketHandler handler = this.getHandlerRegistredByMac(receivedMessage.getMacAddress());

        if (handler != null) {
            // deliver message to handler.
            handler.newReceivedResponseMessage(receivedMessage);
            logger.debug("Received message delivered with success to handler of mac {}",
                    receivedMessage.getMacAddress());
        } else {
            logger.debug("There is no handler registered for mac address: {}", receivedMessage.getMacAddress());
            // notify discovery service of thing found!
            this.silvercrestDiscoveryService.discoveredWifiSocket(receivedMessage.getMacAddress(),
                    receivedMessage.getHostAddress());
        }
    }

    /**
     * Registers a new {@link Thing} and the corresponding {@link SilvercrestWifiSocketHandler}.
     *
     * @param thing the {@link Thing}.
     * @param handler the {@link SilvercrestWifiSocketHandler}.
     */
    public void registerThingAndWifiSocketHandler(final Thing thing, final SilvercrestWifiSocketHandler handler) {
        this.handlersRegisteredByThing.put(thing, handler);
    }

    /**
     * Unregisters a {@link SilvercrestWifiSocketHandler} by the corresponding {@link Thing}.
     *
     * @param thing the {@link Thing}.
     */
    public void unregisterWifiSocketHandlerByThing(final Thing thing) {
        SilvercrestWifiSocketHandler handler = this.handlersRegisteredByThing.get(thing);
        if (handler != null) {
            this.handlersRegisteredByThing.remove(thing);
        }

    }

    /**
     * Utilitary method to get the registered thing handler in mediator by the mac address.
     *
     * @param macAddress the mac address of the thing of the handler.
     * @return {@link SilvercrestWifiSocketHandler} if found.
     */
    private SilvercrestWifiSocketHandler getHandlerRegistredByMac(final String macAddress) {
        SilvercrestWifiSocketHandler searchedHandler = null;
        for (SilvercrestWifiSocketHandler handler : this.handlersRegisteredByThing.values()) {
            if (macAddress.equals(handler.getMacAddress())) {
                searchedHandler = handler;
                // don't spend more computation. Found the handler.
                break;
            }
        }
        return searchedHandler;
    }

    /**
     * Inits the mediator WifiSocketUpdateReceiverRunnable thread. This thread is responsible to receive all
     * packets from Wifi Socket devices, and redirect the messages to mediator.
     */
    private void initMediatorWifiSocketUpdateReceiverRunnable() {
        // try with handler port if is null
        if ((this.receiver == null) || ((this.receiverThread != null)
                && (this.receiverThread.isInterrupted() || !this.receiverThread.isAlive()))) {
            try {
                this.receiver = new SilvercrestWifiSocketUpdateReceiverRunnable(this,
                        SilvercrestWifiSocketBindingConstants.WIFI_SOCKET_DEFAULT_UDP_PORT);
                this.receiverThread = new Thread(this.receiver);
                this.receiverThread.start();
                logger.debug("Invoked the start of receiver thread.");
            } catch (SocketException e) {
                logger.debug("Cannot start the socket with default port...");
            }
        }
    }

    /**
     * Returns all the {@link Thing} registered.
     *
     * @returns all the {@link Thing}.
     */
    public Set<Thing> getAllThingsRegistered() {
        return this.handlersRegisteredByThing.keySet();
    }
}
