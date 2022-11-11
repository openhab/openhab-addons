/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.discovery;

import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.CONFIGURATION_ADDITIONAL_KEY;
import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.CONFIGURATION_DECRYPTION_KEY;
import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.CONFIGURATION_DECRYPTION_KEY_EMPTY;
import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.CONFIGURATION_SERIAL_PORT;
import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.DSMR_PORT_NAME;
import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.THING_TYPE_DSMR_BRIDGE;
import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.THING_TYPE_SMARTY_BRIDGE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceRunnable;
import org.openhab.binding.dsmr.internal.device.DSMREventListener;
import org.openhab.binding.dsmr.internal.device.DSMRSerialAutoDevice;
import org.openhab.binding.dsmr.internal.device.DSMRTelegramListener;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorErrorEvent;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram.TelegramState;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements the discovery service for detecting new DSMR Meters.
 *
 * The service will iterate over the available serial ports and open the given serial port and wait for telegrams. If
 * the port is already owned because it's already detected this service will ignore it. But it will report a warning in
 * case the port was locked due to a crash.
 * After {@link #BAUDRATE_SWITCH_TIMEOUT_SECONDS} seconds it will switch the baud rate and wait again for telegrams.
 * When that doesn't produce any results the service will give up (assuming no DSMR Bridge is present).
 *
 * If a telegram is received with at least 1 Cosem Object a bridge is assumed available and a Thing is added (regardless
 * if there were problems receiving the telegram) and the discovery is stopped.
 *
 * If there are communication problems the service will give an warning and give up
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored code to detect meters during actual discovery phase.
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.dsmr")
public class DSMRBridgeDiscoveryService extends DSMRDiscoveryService implements DSMREventListener {

    /**
     * The timeout used to switch baudrate if no valid data is received within that time frame.
     */
    private static final int BAUDRATE_SWITCH_TIMEOUT_SECONDS = 25;

    private final Logger logger = LoggerFactory.getLogger(DSMRBridgeDiscoveryService.class);

    /**
     * Serial Port Manager.
     */
    private final SerialPortManager serialPortManager;

    /**
     * DSMR Device that is scanned when discovery process in progress.
     */
    private @Nullable DSMRDeviceRunnable currentScannedDevice;

    /**
     * Name of the serial port that is scanned when discovery process in progress.
     */
    private String currentScannedPortName = "";

    /**
     * Keeps a boolean during time discovery process in progress.
     */
    private boolean scanning;

    @Activate
    public DSMRBridgeDiscoveryService(final @Reference TranslationProvider i18nProvider,
            final @Reference LocaleProvider localeProvider, final @Reference SerialPortManager serialPortManager) {
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.serialPortManager = serialPortManager;
    }

    /**
     * Starts a new discovery scan.
     *
     * All available Serial Ports are scanned for P1 telegrams.
     */
    @Override
    protected void startScan() {
        logger.debug("Started DSMR discovery scan");
        scanning = true;
        final Stream<SerialPortIdentifier> portEnum = serialPortManager.getIdentifiers();

        // Traverse each available serial port
        portEnum.forEach(portIdentifier -> {
            if (scanning) {
                currentScannedPortName = portIdentifier.getName();
                if (portIdentifier.isCurrentlyOwned()) {
                    logger.trace("Possible port to check:{}, owned:{} by:{}", currentScannedPortName,
                            portIdentifier.isCurrentlyOwned(), portIdentifier.getCurrentOwner());
                    if (DSMR_PORT_NAME.equals(portIdentifier.getCurrentOwner())) {
                        logger.debug("The port {} is owned by this binding. If no DSMR meters will be found it "
                                + "might indicate the port is locked by an older instance of this binding. "
                                + "Restart the system to unlock the port.", currentScannedPortName);
                    }
                } else {
                    logger.debug("Start discovery on serial port: {}", currentScannedPortName);
                    //
                    final DSMRTelegramListener telegramListener = new DSMRTelegramListener("",
                            CONFIGURATION_ADDITIONAL_KEY);
                    final DSMRSerialAutoDevice device = new DSMRSerialAutoDevice(serialPortManager,
                            portIdentifier.getName(), this, telegramListener, scheduler,
                            BAUDRATE_SWITCH_TIMEOUT_SECONDS);
                    device.setLenientMode(true);
                    currentScannedDevice = new DSMRDeviceRunnable(device, this);
                    currentScannedDevice.run();
                }
            }
        });
    }

    @Override
    protected synchronized void stopScan() {
        scanning = false;
        stopSerialPortScan();
        super.stopScan();
        logger.debug("Stop DSMR discovery scan");
    }

    /**
     * Stops the serial port device.
     */
    private void stopSerialPortScan() {
        logger.debug("Stop discovery scan on port [{}].", currentScannedPortName);
        if (currentScannedDevice != null) {
            currentScannedDevice.stop();
        }
        currentScannedDevice = null;
        currentScannedPortName = "";
    }

    /**
     * Handle if telegrams are received.
     *
     * If there are cosem objects received a new bridge will we discovered.
     *
     * @param telegram the received telegram
     */
    @Override
    public void handleTelegramReceived(final P1Telegram telegram) {
        final List<CosemObject> cosemObjects = telegram.getCosemObjects();

        if (logger.isDebugEnabled()) {
            logger.debug("[{}] Received {} cosemObjects", currentScannedPortName, cosemObjects.size());
        }
        if (telegram.getTelegramState() == TelegramState.INVALID_ENCRYPTION_KEY) {
            bridgeDiscovered(THING_TYPE_SMARTY_BRIDGE);
            stopSerialPortScan();
        } else if (!cosemObjects.isEmpty()) {
            final ThingUID bridgeThingUID = bridgeDiscovered(THING_TYPE_DSMR_BRIDGE);
            meterDetector.detectMeters(telegram).getKey().forEach(m -> meterDiscovered(m, bridgeThingUID));
            stopSerialPortScan();
        }
    }

    /**
     * Creates a bridge.
     *
     * @return The {@link ThingUID} of the newly created bridge
     */
    private ThingUID bridgeDiscovered(final ThingTypeUID bridgeThingTypeUID) {
        final ThingUID thingUID = new ThingUID(bridgeThingTypeUID,
                Integer.toHexString(currentScannedPortName.hashCode()));
        final boolean smarty = THING_TYPE_SMARTY_BRIDGE.equals(bridgeThingTypeUID);
        final String label = String.format("@text/thing-type.dsmr.%s.label", smarty ? "smartyBridge" : "dsmrBridge");

        // Construct the configuration for this meter
        final Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIGURATION_SERIAL_PORT, currentScannedPortName);
        if (smarty) {
            properties.put(CONFIGURATION_DECRYPTION_KEY, CONFIGURATION_DECRYPTION_KEY_EMPTY);
            properties.put(CONFIGURATION_ADDITIONAL_KEY, CONFIGURATION_ADDITIONAL_KEY);
        }
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(bridgeThingTypeUID).withProperties(properties).withLabel(label).build();

        logger.debug("[{}] discovery result:{}", currentScannedPortName, discoveryResult);

        thingDiscovered(discoveryResult);
        return thingUID;
    }

    @Override
    public void handleErrorEvent(final DSMRConnectorErrorEvent portEvent) {
        logger.debug("[{}] Error on port during discovery: {}", currentScannedPortName, portEvent);
        stopSerialPortScan();
    }
}
