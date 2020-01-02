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
package org.openhab.binding.wizlighting.internal.handler;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.wizlighting.internal.WizLightingBindingConstants;
import org.openhab.binding.wizlighting.internal.discovery.WizLightingDiscoveryService;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.entities.WizResponseParam;
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;
import org.openhab.binding.wizlighting.internal.runnable.WizLightingUpdateReceiverRunnable;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WizLightingMediatorImpl} is responsible for receiving all the sync
 * packets and route correctly to each handler.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
@Component(configurationPid = "WizLightingMediator", service = WizLightingMediator.class)
@NonNullByDefault
public class WizLightingMediatorImpl implements WizLightingMediator {

    private final Logger logger = LoggerFactory.getLogger(WizLightingMediatorImpl.class);

    private final Map<Thing, WizLightingHandler> handlersRegisteredByThing = new HashMap<>();

    private @Nullable WizLightingUpdateReceiverRunnable receiver;
    private @Nullable Thread receiverThread;

    private @Nullable WizLightingDiscoveryService wizlightingDiscoveryService;

    /**
     * Called at the service activation.
     *
     * @param componentContext the componentContext
     */
    protected void activate(final ComponentContext componentContext) {
        logger.trace("Mediator has been activated by OSGI.");
        this.initMediatorWizBulbUpdateReceiverRunnable();
    }

    /**
     * Called at the service deactivation.
     *
     * @param componentContext the componentContext
     */
    protected void deactivate(final ComponentContext componentContext) {
        WizLightingUpdateReceiverRunnable receiver = this.receiver;
        if (receiver != null) {
            receiver.shutdown();
        }
    }

    /**
     * This method is called by the {@link WizLightingUpdateReceiverRunnable}, when
     * one new message has been received.
     *
     * @param receivedMessage the {@link WizLightingResponse} message.
     */
    @Override
    public void processReceivedPacket(final WizLightingResponse receivedMessage) {
        logger.debug("Received packet from: {} with method: [{}]", receivedMessage.getWizResponseIpAddress(),
                receivedMessage.getMethod());

        @Nullable
        String bulbIp = receivedMessage.getWizResponseIpAddress();
        @Nullable
        String bulbMac = receivedMessage.getWizResponseMacAddress();
        WizLightingMethodType bulbMethod = receivedMessage.getMethod();

        if (bulbMac != null) {
            @Nullable
            WizLightingHandler handler = this.getHandlerRegisteredByMac(bulbMac);

            if (handler != null) {
                // deliver message to handler.
                handler.newReceivedResponseMessage(receivedMessage);
                logger.debug("Received message delivered with success to handler of mac {}",
                        receivedMessage.getWizResponseIpAddress());
            } else if (bulbIp != null) {
                logger.debug("There is no handler registered for mac address: {}",
                        receivedMessage.getWizResponseIpAddress());
                WizLightingDiscoveryService discoveryServe = this.wizlightingDiscoveryService;
                if (discoveryServe != null) {
                    WizResponseParam incomingParam = receivedMessage.getParams();
                    if (bulbMethod == WizLightingMethodType.firstBeat && incomingParam != null) {
                        int homeId = incomingParam.homeId;
                        discoveryServe.discoveredLight(bulbMac, bulbIp, homeId);
                        logger.trace("Sending a new thing to the discovery service.  MAC: {}  IP: {}  Home ID: {}",
                                bulbMac, bulbIp, homeId);
                    } else if (receivedMessage.getMethod() != WizLightingMethodType.firstBeat) {
                        logger.trace("Not a firstBeat, method is {}.  Ignoring.", receivedMessage.getMethod());
                    } else if (incomingParam == null) {
                        logger.trace("Appears to be a firstBeat, but params not received.");
                    }
                } else {
                    logger.trace("There is no discovery service registered to receive the new bulb!");
                }
            }
        } else {
            logger.warn("The sync response did not contain a mac address, it cannot be processed.");
        }
    }

    /**
     * Register one new {@link Thing} and the corresponding
     * {@link WizLightingHandler}.
     *
     * @param thing the {@link Thing}.
     * @param handler the {@link WizLightingHandler}.
     */
    @Override
    public void registerThingAndWizBulbHandler(final Thing thing, final WizLightingHandler handler) {
        this.handlersRegisteredByThing.put(thing, handler);
    }

    /**
     * Unregister one {@link WizLightingHandler} by the corresponding {@link Thing}.
     *
     * @param thing the {@link Thing}.
     */
    @Override
    public void unregisterWizBulbHandlerByThing(final Thing thing) {
        this.handlersRegisteredByThing.remove(thing);
    }

    /**
     * Utilitary method to get the registered thing handler in mediator by the mac
     * address.
     *
     * @param bulbMacAddress the mac address of the thing of the handler.
     * @return {@link WizLightingHandler} if found.
     */
    private @Nullable WizLightingHandler getHandlerRegisteredByMac(final String bulbMacAddress) {
        WizLightingHandler searchedHandler = null;
        for (WizLightingHandler handler : this.handlersRegisteredByThing.values()) {
            if (bulbMacAddress.equalsIgnoreCase(handler.getBulbMacAddress())) {
                searchedHandler = handler;
                // don't spend more computation. Found the handler.
                break;
            }
        }
        return searchedHandler;
    }

    /**
     * Inits the mediator WizBulbUpdateReceiverRunnable thread. This thread is
     * responsible to receive all packets from Wiz Bulbs, and redirect the messages
     * to mediator.
     */
    private void initMediatorWizBulbUpdateReceiverRunnable() {
        WizLightingUpdateReceiverRunnable receiver = this.receiver;
        Thread receiverThread = this.receiverThread;
        // try with handler port if is null
        if ((receiver == null)
                || ((receiverThread != null) && (receiverThread.isInterrupted() || !receiverThread.isAlive()))) {
            try {
                logger.trace("Receiver thread is either null, interrupted, or dead.");
                WizLightingUpdateReceiverRunnable newReceiver = new WizLightingUpdateReceiverRunnable(this,
                        WizLightingBindingConstants.LISTENER_DEFAULT_UDP_PORT);
                Thread newThread = new Thread(newReceiver);
                newThread.start();
                this.receiver = newReceiver;
                this.receiverThread = newThread;
                logger.trace("A new receiver thread has been started.");
            } catch (SocketException e) {
                logger.debug("Cannot start the socket with default port {}...", e);
            }
        }
    }

    /**
     * Returns all the {@link Thing} registered.
     *
     * @returns all the {@link Thing}.
     */
    @Override
    public Set<Thing> getAllThingsRegistered() {
        return this.handlersRegisteredByThing.keySet();
    }

    @Override
    public void setDiscoveryService(final @Nullable WizLightingDiscoveryService discoveryService) {
        this.wizlightingDiscoveryService = discoveryService;
    }

    public @Nullable WizLightingDiscoveryService getDiscoveryService() {
        return this.wizlightingDiscoveryService;
    }
}
