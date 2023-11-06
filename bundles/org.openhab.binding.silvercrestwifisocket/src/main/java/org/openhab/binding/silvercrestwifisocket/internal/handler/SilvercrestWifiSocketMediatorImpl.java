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
package org.openhab.binding.silvercrestwifisocket.internal.handler;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.silvercrestwifisocket.internal.SilvercrestWifiSocketBindingConstants;
import org.openhab.binding.silvercrestwifisocket.internal.discovery.SilvercrestWifiSocketDiscoveryService;
import org.openhab.binding.silvercrestwifisocket.internal.entities.SilvercrestWifiSocketResponse;
import org.openhab.binding.silvercrestwifisocket.internal.runnable.SilvercrestWifiSocketUpdateReceiverRunnable;
import org.openhab.core.thing.Thing;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SilvercrestWifiSocketMediatorImpl} is responsible for receiving all the UDP packets and route correctly to
 * each handler.
 *
 * @author Jaime Vaz - Initial contribution
 */
@Component(service = SilvercrestWifiSocketMediator.class)
public class SilvercrestWifiSocketMediatorImpl implements SilvercrestWifiSocketMediator {

    private final Logger logger = LoggerFactory.getLogger(SilvercrestWifiSocketMediatorImpl.class);

    private final Map<Thing, SilvercrestWifiSocketHandler> handlersRegistredByThing = new HashMap<>();

    private SilvercrestWifiSocketUpdateReceiverRunnable receiver;
    private Thread receiverThread;

    private SilvercrestWifiSocketDiscoveryService silvercrestDiscoveryService;

    /**
     * Called at the service activation.
     *
     * @param componentContext the componentContext
     */
    protected void activate(final ComponentContext componentContext) {
        logger.debug("Mediator has been activated by OSGI.");
        this.initMediatorWifiSocketUpdateReceiverRunnable();
    }

    /**
     * Called at the service deactivation.
     *
     * @param componentContext the componentContext
     */
    protected void deactivate(final ComponentContext componentContext) {
        if (this.receiver != null) {
            this.receiver.shutdown();
        }
    }

    /**
     * This method is called by the {@link SilvercrestWifiSocketUpdateReceiverRunnable}, when one new message has been
     * received.
     *
     * @param receivedMessage the {@link SilvercrestWifiSocketResponse} message.
     */
    @Override
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
            logger.debug("There is no handler registered for mac address:{}", receivedMessage.getMacAddress());
            // notify discovery service of thing found!
            this.silvercrestDiscoveryService.discoveredWifiSocket(receivedMessage.getMacAddress(),
                    receivedMessage.getHostAddress());
        }
    }

    /**
     * Regists one new {@link Thing} and the corresponding {@link SilvercrestWifiSocketHandler}.
     *
     * @param thing the {@link Thing}.
     * @param handler the {@link SilvercrestWifiSocketHandler}.
     */
    @Override
    public void registerThingAndWifiSocketHandler(final Thing thing, final SilvercrestWifiSocketHandler handler) {
        this.handlersRegistredByThing.put(thing, handler);
    }

    /**
     * Unregists one {@link SilvercrestWifiSocketHandler} by the corresponding {@link Thing}.
     *
     * @param thing the {@link Thing}.
     */
    @Override
    public void unregisterWifiSocketHandlerByThing(final Thing thing) {
        SilvercrestWifiSocketHandler handler = this.handlersRegistredByThing.get(thing);
        if (handler != null) {
            this.handlersRegistredByThing.remove(thing);
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
        for (SilvercrestWifiSocketHandler handler : this.handlersRegistredByThing.values()) {
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
     * @return all the {@link Thing}.
     */
    @Override
    public Set<Thing> getAllThingsRegistred() {
        return this.handlersRegistredByThing.keySet();
    }

    @Override
    public void setDiscoveryService(final SilvercrestWifiSocketDiscoveryService discoveryService) {
        this.silvercrestDiscoveryService = discoveryService;
    }
}
