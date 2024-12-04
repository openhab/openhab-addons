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
package org.openhab.binding.wizlighting.internal.handler;

import static org.openhab.binding.wizlighting.internal.WizLightingBindingConstants.*;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wizlighting.internal.discovery.WizLightingDiscoveryService;
import org.openhab.binding.wizlighting.internal.entities.RegistrationRequestParam;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.runnable.WizLightingUpdateReceiverRunnable;
import org.openhab.binding.wizlighting.internal.utils.NetworkUtils;
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
 * The {@link WizLightingMediatorImpl} is responsible for receiving all the sync
 * packets and route correctly to each handler.
 *
 * @author Sriram Balakrishnan - Initial contribution
 * @author Joshua Freeman - pass through NetworkAddressService
 */
@Component(configurationPid = "WizLightingMediator", service = WizLightingMediator.class)
@NonNullByDefault
public class WizLightingMediatorImpl implements WizLightingMediator {

    private final Logger logger = LoggerFactory.getLogger(WizLightingMediatorImpl.class);

    private final Map<Thing, WizLightingHandler> handlersRegisteredByThing = new HashMap<>();

    private @Nullable WizLightingUpdateReceiverRunnable receiver;
    private @Nullable Thread wizLightingReceiverThread;

    private @Nullable WizLightingDiscoveryService wizlightingDiscoveryService;

    private final NetworkAddressService networkAddressService;

    /**
     * Constructor for the mediator implementation.
     *
     * @param IllegalArgumentException if the timeout < 0
     */
    @Activate
    public WizLightingMediatorImpl(
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
        logger.debug("Received packet from: {} - {} with method: [{}]", receivedMessage.getWizResponseIpAddress(),
                receivedMessage.getWizResponseMacAddress(), receivedMessage.getMethod());

        String bulbIp = receivedMessage.getWizResponseIpAddress();
        String bulbMac = receivedMessage.getWizResponseMacAddress();

        if (!bulbMac.equals(MISSING_INVALID_MAC_ADDRESS)) {
            @Nullable
            WizLightingHandler handler = this.getHandlerRegisteredByMac(bulbMac);

            if (handler != null) {
                // deliver message to handler.
                handler.newReceivedResponseMessage(receivedMessage);
            } else if (!bulbIp.equals(MISSING_INVALID_IP_ADDRESS)) {
                logger.debug("There is no handler registered for mac address: {}",
                        receivedMessage.getWizResponseMacAddress());
                WizLightingDiscoveryService discoveryServe = this.wizlightingDiscoveryService;
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
     * Utility method to get the registered thing handler in mediator by the mac
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
        Thread receiverThread = this.wizLightingReceiverThread;
        // try with handler port if is null
        if ((receiver == null)
                || ((receiverThread != null) && (receiverThread.isInterrupted() || !receiverThread.isAlive()))) {
            try {
                logger.trace("Receiver thread is either null, interrupted, or dead.");
                WizLightingUpdateReceiverRunnable newReceiver = new WizLightingUpdateReceiverRunnable(this,
                        DEFAULT_LISTENER_UDP_PORT);
                Thread newThread = new Thread(newReceiver);
                newThread.setDaemon(true);
                newThread.start();
                newThread.setName("wizLightingReceiverThread");
                this.receiver = newReceiver;
                this.wizLightingReceiverThread = newThread;
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

    private String getMyIpAddress() {
        String myIpAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (myIpAddress == null) {
            logger.warn("Network interface did not return an IP address!");
            return "OHIPAddress";
        }
        return myIpAddress;
    }

    private String getMyMacAddress() {
        String myMacAddress;
        try {
            myMacAddress = NetworkUtils.getMyMacAddress(getMyIpAddress());
            if (myMacAddress == null) {
                logger.warn("No network interface could be found.  MAC of OpenHab device is unknown.");
                return "OHMACAddress";
            }
        } catch (Exception e) {
            logger.warn("MAC Address of openHAB device is invalid.");
            return "OHMACAddress";
        }
        return myMacAddress;
    }

    /**
     * Returns a {@link RegistrationRequestParam} based on the current OpenHAB
     * connection.
     *
     */
    public RegistrationRequestParam getRegistrationParams() {
        return new RegistrationRequestParam(getMyIpAddress(), true, 0, getMyMacAddress());
    }

    @Override
    public void setDiscoveryService(final @Nullable WizLightingDiscoveryService discoveryService) {
        this.wizlightingDiscoveryService = discoveryService;
    }

    public @Nullable WizLightingDiscoveryService getDiscoveryService() {
        return this.wizlightingDiscoveryService;
    }

    @Override
    public NetworkAddressService getNetworkAddressService() {
        return this.networkAddressService;
    }
}
