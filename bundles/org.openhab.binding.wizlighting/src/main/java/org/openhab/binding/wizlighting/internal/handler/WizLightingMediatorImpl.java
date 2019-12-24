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

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.wizlighting.internal.WizLightingBindingConstants;
import org.openhab.binding.wizlighting.internal.entities.WizLightingSyncResponse;
import org.openhab.binding.wizlighting.internal.runnable.WizLightingUpdateReceiverRunnable;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.osgi.service.component.annotations.Component;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link WizLightingMediatorImpl} is responsible for receiving all the sync packets and route correctly to
 * each handler.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
// @Component(configurationPid = "WizLightingMediator", service = WizLightingMediator.class)
@NonNullByDefault
public class WizLightingMediatorImpl implements WizLightingMediator {

    private final Logger logger = LoggerFactory.getLogger(WizLightingMediatorImpl.class);

    private final Map<Thing, WizLightingHandler> handlersRegisteredByThing = new HashMap<>();

    private @Nullable WizLightingUpdateReceiverRunnable receiver;
    private @Nullable Thread receiverThread;

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
     * This method is called by the {@link WizLightingUpdateReceiverRunnable}, when one new message has been
     * received.
     *
     * @param receivedMessage the {@link WizLightingSyncResponse} message.
     */
    @Override
    public void processReceivedPacket(final WizLightingSyncResponse receivedMessage) {
        logger.debug("Received packet from: {} with method: [{}]", receivedMessage.getWizResponseIpAddress(),
                receivedMessage.getMethod());

        @Nullable String bulbMac =receivedMessage.getWizResponseIpAddress();

        if (bulbMac != null){
            @Nullable
            WizLightingHandler handler = this.getHandlerRegisteredByMac(bulbMac);

            if (handler != null) {
                // deliver message to handler.
                handler.newReceivedResponseMessage(receivedMessage);
                logger.debug("Received message delivered with success to handler of mac {}",
                        receivedMessage.getWizResponseIpAddress());
            } else {
                logger.debug("There is no handler registered for mac address:{}", receivedMessage.getWizResponseIpAddress());
            }
        } else {
            logger.warn("The sync response did not contain a mac address, it cannot be processed.");
        }
    }

    /**
     * Register one new {@link Thing} and the corresponding {@link WizLightingHandler}.
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
        // @Nullable
        // WizLightingHandler handler = this.handlersRegisteredByThing.get(thing);
        // if (handler != null) {
            this.handlersRegisteredByThing.remove(thing);
        // }
    }

    /**
     * Utilitary method to get the registered thing handler in mediator by the mac address.
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
     * Inits the mediator WizBulbUpdateReceiverRunnable thread. This thread is responsible to receive all
     * packets from Wiz Bulbs, and redirect the messages to mediator.
     */
    private void initMediatorWizBulbUpdateReceiverRunnable() {
        WizLightingUpdateReceiverRunnable receiver = this.receiver;
        Thread receiverThread = this.receiverThread;
        // try with handler port if is null
        if ((receiver == null) || ((receiverThread != null)
                && (receiverThread.isInterrupted() || !receiverThread.isAlive()))) {
            try {
                this.receiver = new WizLightingUpdateReceiverRunnable(this,
                        WizLightingBindingConstants.LISTENER_DEFAULT_UDP_PORT);
                this.receiverThread = new Thread(receiver);
                Thread receiverThread2 = this.receiverThread;
                if (receiverThread2 != null) {
                    receiverThread2.start();
                }
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
    @Override
    public Set<Thing> getAllThingsRegistered() {
        return this.handlersRegisteredByThing.keySet();
    }
}
