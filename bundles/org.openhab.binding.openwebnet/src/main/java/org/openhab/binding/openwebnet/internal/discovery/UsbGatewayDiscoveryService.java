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
package org.openhab.binding.openwebnet.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.ThingUID;
import org.openwebnet4j.GatewayListener;
import org.openwebnet4j.OpenDeviceType;
import org.openwebnet4j.USBGateway;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.Where;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UsbGatewayDiscoveryService} extends {@link AbstractDiscoveryService} to detect Zigbee USB gateways
 * connected via serial port. The service will iterate over the available serial ports and open each one to test if a
 * OpenWebNet Zigbee USB gateway is connected. On successful connection, a new DiscoveryResult is created.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.openwebnet")

public class UsbGatewayDiscoveryService extends AbstractDiscoveryService implements GatewayListener {

    private final Logger logger = LoggerFactory.getLogger(UsbGatewayDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 30;
    private static final int PORT_CHECK_TIMEOUT_MSEC = 1500;

    private CountDownLatch portCheckLatch = new CountDownLatch(1);
    private @Nullable ScheduledFuture<?> connectTimeout;

    private final SerialPortManager serialPortManager;
    private @Nullable USBGateway zbGateway;

    private String currentScannedPortName = "";

    /**
     * Keeps a boolean during time discovery process in progress.
     */
    private boolean scanning;

    /**
     * Constructs a new UsbGatewayDiscoveryService with the specified Zigbee USB Bridge ThingTypeUID
     */
    @Activate
    public UsbGatewayDiscoveryService(final @Reference SerialPortManager spm) {
        super(Set.of(OpenWebNetBindingConstants.THING_TYPE_ZB_GATEWAY), DISCOVERY_TIMEOUT_SECONDS, false);
        // Obtain the serial port manager service using an OSGi reference
        serialPortManager = spm;
    }

    /**
     * Starts a new discovery scan. All available Serial Ports are scanned.
     */
    @Override
    protected void startScan() {
        logger.debug("Started OpenWebNet Zigbee USB Gateway discovery scan");
        removeOlderResults(getTimestampOfLastScan());
        scanning = true;
        Stream<SerialPortIdentifier> portEnum = serialPortManager.getIdentifiers();
        // Check each available serial port
        try {
            for (SerialPortIdentifier portIdentifier : portEnum.toArray(SerialPortIdentifier[]::new)) {
                if (scanning) {
                    currentScannedPortName = portIdentifier.getName();
                    logger.debug("[{}] == checking serial port", currentScannedPortName);
                    if (portIdentifier.isCurrentlyOwned()) {
                        logger.debug("[{}] serial port is owned by: {}", currentScannedPortName,
                                portIdentifier.getCurrentOwner());
                    } else {
                        logger.debug("[{}] trying to connect to a Zigbee USB Gateway...", currentScannedPortName);
                        USBGateway gw = new USBGateway(currentScannedPortName);
                        zbGateway = gw;
                        gw.subscribe(this);
                        portCheckLatch = new CountDownLatch(1);
                        connectTimeout = scheduler.schedule(() -> {
                            logger.debug("[{}] timeout expired", currentScannedPortName);
                            endGwConnection();
                            portCheckLatch.countDown();
                        }, PORT_CHECK_TIMEOUT_MSEC, TimeUnit.MILLISECONDS);
                        try {
                            gw.connect();
                            portCheckLatch.await();
                        } catch (OWNException e) {
                            logger.debug("[{}] OWNException while trying to connect to a Zigbee USB Gateway: {}",
                                    currentScannedPortName, e.getMessage());
                            cancelConnectTimeout();
                            endGwConnection();
                        }
                    }
                    logger.debug("[{}] == finished checking port", currentScannedPortName);
                }
            }
            logger.debug("Finished checking all serial ports");
        } catch (InterruptedException ie) {
            logger.warn("[{}] interrupted: {}", currentScannedPortName, ie.getMessage());
            endGwConnection();
            logger.debug("Interrupted while checking serial ports");
        }
    }

    @Override
    protected synchronized void stopScan() {
        scanning = false;
        cancelConnectTimeout();
        endGwConnection();
        portCheckLatch.countDown();
        super.stopScan();
        logger.debug("Stopped OpenWebNet Zigbee USB Gateway discovery scan");
    }

    /**
     * Ends connection to the gateway
     */
    private void endGwConnection() {
        USBGateway gw = zbGateway;
        if (gw != null) {
            gw.closeConnection();
            zbGateway = null;
            logger.debug("[{}] connection to gateway closed", currentScannedPortName);
        }
    }

    private void cancelConnectTimeout() {
        ScheduledFuture<?> ct = connectTimeout;
        if (ct != null && !ct.isDone()) {
            ct.cancel(false);
            ct = null;
            logger.debug("[{}] timeout cancelled", currentScannedPortName);
        }
    }

    /**
     * Create and notify a new Zigbee USB Gateway thing has been discovered
     */
    private void bridgeDiscovered() {
        USBGateway gw = zbGateway;
        if (gw != null) {
            int gatewayZigBeeId = gw.getZigBeeIdAsDecimal();
            ThingUID gatewayUID = new ThingUID(OpenWebNetBindingConstants.THING_TYPE_ZB_GATEWAY,
                    Integer.toString(gatewayZigBeeId));
            Map<String, Object> gwProperties = new HashMap<>(3);
            gwProperties.put(OpenWebNetBindingConstants.CONFIG_PROPERTY_SERIAL_PORT, gw.getSerialPortName());
            gwProperties.put(OpenWebNetBindingConstants.PROPERTY_FIRMWARE_VERSION, gw.getFirmwareVersion());
            gwProperties.put(OpenWebNetBindingConstants.PROPERTY_ZIGBEEID, String.valueOf(gatewayZigBeeId));

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(gatewayUID).withProperties(gwProperties)
                    .withLabel(OpenWebNetBindingConstants.THING_LABEL_ZB_GATEWAY + " (" + gw.getSerialPortName() + ")")
                    .withRepresentationProperty(OpenWebNetBindingConstants.PROPERTY_ZIGBEEID).build();
            logger.debug("--- Zigbee USB Gateway thing discovered: {} fw: {}", discoveryResult.getLabel(),
                    gw.getFirmwareVersion());
            thingDiscovered(discoveryResult);
        }
    }

    @Override
    public void onConnected() {
        logger.debug("[{}] found Zigbee USB Gateway", currentScannedPortName);
        cancelConnectTimeout();
        bridgeDiscovered();
        endGwConnection();
        portCheckLatch.countDown();
    }

    @Override
    public void onConnectionError(@Nullable OWNException error) {
        OWNException e = error;
        String msg = (e != null ? e.getMessage() : "");
        logger.debug("[{}] onConnectionError(): {}", currentScannedPortName, msg);
    }

    @Override
    public void onConnectionClosed() {
        logger.debug("UsbGatewayDiscoveryService received onConnectionClosed()");
    }

    @Override
    public void onDisconnected(@Nullable OWNException error) {
        logger.debug("UsbGatewayDiscoveryService received onDisconnected()");
    }

    @Override
    public void onReconnected() {
        logger.debug("UsbGatewayDiscoveryService received onReconnected()");
    }

    @Override
    public void onEventMessage(@Nullable OpenMessage msg) {
        logger.debug("UsbGatewayDiscoveryService received onEventMessage(): {}", msg);
    }

    @Override
    public void onNewDevice(@Nullable Where where, @Nullable OpenDeviceType deviceType,
            @Nullable BaseOpenMessage message) {
        logger.debug("UsbGatewayDiscoveryService received onNewDevice()");
    }

    @Override
    public void onDiscoveryCompleted() {
        logger.debug("UsbGatewayDiscoveryService received onDiscoveryCompleted()");
    }
}
