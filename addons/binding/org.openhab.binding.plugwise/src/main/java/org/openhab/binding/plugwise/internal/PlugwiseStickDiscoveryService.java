/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import static org.openhab.binding.plugwise.PlugwiseBindingConstants.THING_TYPE_STICK;
import static org.openhab.binding.plugwise.internal.protocol.field.DeviceType.STICK;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.plugwise.PlugwiseBindingConstants;
import org.openhab.binding.plugwise.internal.config.PlugwiseStickConfig;
import org.openhab.binding.plugwise.internal.listener.PlugwiseMessageListener;
import org.openhab.binding.plugwise.internal.protocol.InformationRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.InformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.NetworkStatusRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.NetworkStatusResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import gnu.io.CommPortIdentifier;

/**
 * Discovers Stick devices by periodically sending a {@link NetworkStatusRequestMessage} to unused serial ports.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.plugwise")
public class PlugwiseStickDiscoveryService extends AbstractDiscoveryService
        implements ExtendedDiscoveryService, PlugwiseMessageListener {

    private static final Set<ThingTypeUID> DISCOVERED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_STICK);

    private static final int DISCOVERY_INTERVAL = 180;
    private static final int SCAN_TIMEOUT = 5;

    private final Logger logger = LoggerFactory.getLogger(PlugwiseStickDiscoveryService.class);

    private DiscoveryServiceCallback discoveryServiceCallback;

    private ScheduledFuture<?> discoveryJob;

    private PlugwiseCommunicationHandler communicationHandler = new PlugwiseCommunicationHandler();

    private boolean discovering;
    private ReentrantLock discoveryLock = new ReentrantLock();
    private Condition continueDiscovery = discoveryLock.newCondition();

    public PlugwiseStickDiscoveryService() throws IllegalArgumentException {
        super(DISCOVERED_THING_TYPES_UIDS, 1, true);
    }

    @Override
    public synchronized void abortScan() {
        logger.debug("Aborting Stick discovery");
        super.abortScan();
        discovering = false;
    }

    @Activate
    public void activate() {
        super.activate(new HashMap<>());
        communicationHandler.addMessageListener(this);
    }

    private DiscoveryResult createDiscoveryResult(MACAddress macAddress, Map<String, String> properties) {
        String mac = macAddress.toString();
        ThingUID thingUID = new ThingUID(THING_TYPE_STICK, mac);

        return DiscoveryResultBuilder.create(thingUID).withLabel("Plugwise " + STICK.toString())
                .withProperty(PlugwiseBindingConstants.CONFIG_PROPERTY_MAC_ADDRESS, mac)
                .withProperty(PlugwiseBindingConstants.CONFIG_PROPERTY_SERIAL_PORT,
                        communicationHandler.getConfiguration().getSerialPort())
                .withProperties(new HashMap<>(properties))
                .withRepresentationProperty(PlugwiseBindingConstants.PROPERTY_MAC_ADDRESS).build();
    }

    @Deactivate
    @Override
    protected void deactivate() {
        super.deactivate();
        communicationHandler.removeMessageListener(this);
    }

    private void discoverNewStickDetails(MACAddress macAddress) {
        if (!isAlreadyDiscovered(macAddress)) {
            logger.debug("Discovered new Stick ({})", macAddress);
            sendMessage(new InformationRequestMessage(macAddress));
        } else {
            try {
                discoveryLock.lock();
                logger.debug("Already discovered Stick ({})", macAddress);
                continueDiscovery.signalAll();
            } finally {
                discoveryLock.unlock();
            }
        }
    }

    private void discoverStick(String serialPort) {
        logger.debug("Discovering Stick on serial port: {}", serialPort);

        PlugwiseStickConfig stickConfig = new PlugwiseStickConfig();
        stickConfig.setSerialPort(serialPort);
        communicationHandler.setConfiguration(stickConfig);

        try {
            discoveryLock.lock();
            try {
                communicationHandler.start();
                sendMessage(new NetworkStatusRequestMessage());
                if (!continueDiscovery.await(SCAN_TIMEOUT, TimeUnit.SECONDS)) {
                    logger.debug("Scan timeout elapsed while discovering Stick on serial port '{}'", serialPort);
                }
            } catch (InterruptedException e) {
                logger.debug("Interrupted while discovering Stick on serial port '{}'", serialPort);
            } catch (PlugwiseInitializationException e) {
                logger.debug("Failed to initialize serial port '{}' for Stick discovery: {}", serialPort,
                        e.getMessage());
            } finally {
                communicationHandler.stop();
            }
        } finally {
            discoveryLock.unlock();
        }
    }

    protected void discoverSticks() {
        if (discovering) {
            logger.debug("Stick discovery not possible (already discovering)");
        } else {
            discovering = true;

            @SuppressWarnings("unchecked")
            Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();

            while (discovering && portIdentifiers.hasMoreElements()) {
                CommPortIdentifier portIdentifier = portIdentifiers.nextElement();
                if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL
                        && !portIdentifier.isCurrentlyOwned()) {
                    discoverStick(portIdentifier.getName());
                }
            }

            discovering = false;
            logger.debug("Finished discovering Sticks on serial ports");
        }
    }

    private void handleInformationResponse(InformationResponseMessage message) {
        MACAddress mac = message.getMACAddress();
        Map<String, String> properties = new HashMap<>();
        PlugwiseUtils.updateProperties(properties, message);
        thingDiscovered(createDiscoveryResult(mac, properties));

        try {
            discoveryLock.lock();
            logger.debug("Finished discovery of {} ({})", STICK, mac);
            continueDiscovery.signalAll();
        } finally {
            discoveryLock.unlock();
        }
    }

    private void handleNetworkStatusResponseMessage(NetworkStatusResponseMessage message) {
        discoverNewStickDetails(message.getMACAddress());
    }

    @Override
    public void handleReponseMessage(Message message) {
        switch (message.getType()) {
            case DEVICE_INFORMATION_RESPONSE:
                handleInformationResponse((InformationResponseMessage) message);
                break;
            case NETWORK_STATUS_RESPONSE:
                handleNetworkStatusResponseMessage((NetworkStatusResponseMessage) message);
                break;
            default:
                logger.trace("Received unhandled {} message from {}", message.getType(), message.getMACAddress());
                break;
        }
    }

    private boolean isAlreadyDiscovered(MACAddress macAddress) {
        ThingUID thingUID = new ThingUID(THING_TYPE_STICK, macAddress.toString());
        if (discoveryServiceCallback == null) {
            logger.debug("Assuming Stick ({}) has not yet been discovered (callback null)", macAddress);
            return false;
        } else if (discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
            logger.debug("Stick ({}) has existing discovery result: {}", macAddress, thingUID);
            return true;
        } else if (discoveryServiceCallback.getExistingThing(thingUID) != null) {
            logger.debug("Stick ({}) has existing thing: {}", macAddress, thingUID);
            return true;
        }
        logger.debug("Stick ({}) has not yet been discovered", macAddress);
        return false;
    }

    @Modified
    @Override
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    private void sendMessage(Message message) {
        try {
            communicationHandler.sendMessage(message, PlugwiseMessagePriority.UPDATE_AND_DISCOVERY);
        } catch (IOException e) {
            try {
                discoveryLock.lock();
                logger.debug("Exception while sending Stick discovery message");
                continueDiscovery.signalAll();
            } finally {
                discoveryLock.unlock();
            }
        }
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Plugwise Stick background discovery");

        Runnable discoveryRunnable = new Runnable() {
            @Override
            public void run() {
                logger.debug("Discover Sticks (background discovery)");
                discoverSticks();
            }
        };

        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(discoveryRunnable, 15, DISCOVERY_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Discover Sticks (manual discovery)");
        discoverSticks();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Plugwise Stick background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

}
