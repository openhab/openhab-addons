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
package org.openhab.binding.wiz.internal.handler;

import static org.openhab.binding.wiz.internal.WizBindingConstants.*;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wiz.internal.WizBindingConstants;
import org.openhab.binding.wiz.internal.discovery.WizDiscoveryService;
import org.openhab.binding.wiz.internal.entities.RegistrationRequestParam;
import org.openhab.binding.wiz.internal.entities.WizResponse;
import org.openhab.binding.wiz.internal.runnable.WizUpdateReceiverRunnable;
import org.openhab.binding.wiz.internal.utils.NetworkUtils;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WizMediatorImpl} is responsible for receiving all the sync
 * packets and route correctly to each handler.
 *
 * @author Sriram Balakrishnan - Initial contribution
 * @author Joshua Freeman - pass through NetworkAddressService
 */
@Component(configurationPid = "WizMediator", service = WizMediator.class)
@NonNullByDefault
public class WizMediatorImpl implements WizMediator {

    private final Logger logger = LoggerFactory.getLogger(WizMediatorImpl.class);

    private final Map<Thing, WizHandler> handlersRegisteredByThing = new HashMap<>();

    private @Nullable WizUpdateReceiverRunnable receiver;
    private @Nullable Thread receiverThread;

    private @Nullable WizDiscoveryService wizDiscoveryService;

    private final NetworkAddressService networkAddressService;

    /**
     * Constructor for the mediator implementation.
     *
     * @param IllegalArgumentException if the timeout < 0
     */
    @Activate
    public WizMediatorImpl(
            @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC) NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
        this.initMediatorWizBulbUpdateReceiverRunnable();
    }

    /**
     * Called at the service deactivation.
     *
     * @param componentContext the componentContext
     */
    protected void deactivate(final ComponentContext componentContext) {
        WizUpdateReceiverRunnable receiver = this.receiver;
        if (receiver != null) {
            receiver.shutdown();
        }
    }

    /**
     * This method is called by the {@link WizUpdateReceiverRunnable}, when
     * one new message has been received.
     *
     * @param receivedMessage the {@link WizResponse} message.
     */
    @Override
    public void processReceivedPacket(final WizResponse receivedMessage) {
        logger.debug("Received packet from: {} - {} with method: [{}]", receivedMessage.getWizResponseIpAddress(),
                receivedMessage.getWizResponseMacAddress(), receivedMessage.getMethod());

        String bulbIp = receivedMessage.getWizResponseIpAddress();
        String bulbMac = receivedMessage.getWizResponseMacAddress();

        if (!bulbMac.isEmpty()) {
            @Nullable
            WizHandler handler = this.getHandlerRegisteredByMac(bulbMac);

            if (handler != null) {
                // deliver message to handler.
                handler.newReceivedResponseMessage(receivedMessage);
            } else if (!bulbIp.isEmpty()) {
                logger.debug("There is no handler registered for mac address: {}",
                        receivedMessage.getWizResponseMacAddress());
                WizDiscoveryService discoveryServe = this.wizDiscoveryService;
                if (discoveryServe != null) {
                    discoveryServe.discoveredLight(bulbMac, bulbIp);
                    logger.trace("Sending a new thing to the discovery service.  MAC: {}  IP: {}", bulbMac, bulbIp);
                } else {
                    logger.trace("There is no discovery service registered to receive the new bulb!");
                }
            }
        } else {
            logger.warn("The sync response did not contain a valid mac address, it cannot be processed.");
        }
    }

    /**
     * Register one new {@link Thing} and the corresponding
     * {@link WizHandler}.
     *
     * @param thing the {@link Thing}.
     * @param handler the {@link WizHandler}.
     */
    @Override
    public void registerThingAndWizBulbHandler(final Thing thing, final WizHandler handler) {
        this.handlersRegisteredByThing.put(thing, handler);
    }

    /**
     * Unregister one {@link WizHandler} by the corresponding {@link Thing}.
     *
     * @param thing the {@link Thing}.
     */
    @Override
    public void unregisterWizBulbHandlerByThing(final Thing thing) {
        this.handlersRegisteredByThing.remove(thing);
    }

    /**
     * Utility method to get the registered thing handler in mediator by the mac
     * address.
     *
     * @param macAddress the mac address of the thing of the handler.
     * @return {@link WizHandler} if found.
     */
    private @Nullable WizHandler getHandlerRegisteredByMac(final String macAddress) {
        WizHandler searchedHandler = null;
        for (WizHandler handler : this.handlersRegisteredByThing.values()) {
            if (macAddress.equalsIgnoreCase(handler.getMacAddress())) {
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
        WizUpdateReceiverRunnable receiver = this.receiver;
        Thread receiverThread = this.receiverThread;
        // try with handler port if is null
        if ((receiver == null)
                || ((receiverThread != null) && (receiverThread.isInterrupted() || !receiverThread.isAlive()))) {
            try {
                logger.trace("Receiver thread is either null, interrupted, or dead.");
                WizUpdateReceiverRunnable newReceiver = new WizUpdateReceiverRunnable(this, DEFAULT_LISTENER_UDP_PORT);
                Thread newThread = new Thread(newReceiver,
                        "OH-binding-" + WizBindingConstants.BINDING_ID + "-ReceiverThread");
                newThread.setDaemon(true);
                newThread.start();
                this.receiver = newReceiver;
                this.receiverThread = newThread;
            } catch (SocketException e) {
                logger.debug("Cannot start the socket with default port {}...", e.getMessage());
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

    /**
     * Returns a {@link RegistrationRequestParam} based on the current openHAB
     * connection.
     * 
     * @throws IllegalStateException
     */
    public RegistrationRequestParam getRegistrationParams() throws IllegalStateException {
        String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        String macAddress = null;
        if (ipAddress != null) {
            macAddress = NetworkUtils.getMacAddress(ipAddress);
        }
        if (ipAddress == null || macAddress == null) {
            throw new IllegalStateException("Unable to determine openHAB's IP and/or MAC address");
        }
        return new RegistrationRequestParam(ipAddress, true, 0, macAddress);
    }

    @Override
    public void setDiscoveryService(final @Nullable WizDiscoveryService discoveryService) {
        this.wizDiscoveryService = discoveryService;
    }

    public @Nullable WizDiscoveryService getDiscoveryService() {
        return this.wizDiscoveryService;
    }

    @Override
    public NetworkAddressService getNetworkAddressService() {
        return this.networkAddressService;
    }
}
