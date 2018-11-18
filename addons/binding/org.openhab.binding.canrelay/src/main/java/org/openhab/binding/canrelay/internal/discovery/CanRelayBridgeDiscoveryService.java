/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.discovery;

import static org.openhab.binding.canrelay.internal.CanRelayBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CanRelayBridgeDiscoveryService} auto discovers new bridges to access CanRelay on CANBUS.
 *
 * @author Lubos Housa - Initial Contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.canrelay")
public class CanRelayBridgeDiscoveryService extends AbstractDiscoveryService {

    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_HW_BRIDGE);
    private static final Logger logger = LoggerFactory.getLogger(CanRelayBridgeDiscoveryService.class);

    private SerialPortManager serialPortManager;

    public CanRelayBridgeDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    protected void startScan() {
        logger.debug("Started CanRelay CANBUS bridge discovery scan");

        // pretty much copy paste from DSMR binding, perhaps can be shared somehow?
        Stream<SerialPortIdentifier> ports = serialPortManager.getIdentifiers();
        ports.forEach(portIdentifier -> {
            String portName = portIdentifier.getName();
            if (portIdentifier.isCurrentlyOwned()) {
                logger.trace("Possible port to check:{}, owned:{} by:{}", portName, portIdentifier.isCurrentlyOwned(),
                        portIdentifier.getCurrentOwner());
                if (CANRELAY_PORT_NAME.equals(portIdentifier.getCurrentOwner())) {
                    logger.debug(
                            "The port {} is owned by this binding. If no CANBUS CanRelay CANBUS bridges will be found it "
                                    + "might indicate the port is locked by an older instance of this binding. "
                                    + "Restart the system to unlock the port.",
                            portName);
                }
            } else {
                logger.debug("Attempting to create a CanRelay bridge on serial port: {}", portName);
                Map<String, Object> properties = new HashMap<>();
                properties.put(HW_BIDGE_CONFIG_SERIALPORT, portName);
                ThingUID bridgeUID = new ThingUID(THING_TYPE_HW_BRIDGE, Integer.toHexString(portName.hashCode()));
                thingDiscovered(DiscoveryResultBuilder.create(bridgeUID)
                        .withLabel("@text/bridge-type.canrelay.hwBridge.label").withProperties(properties).build());
            }
        });

        logger.debug("Finished CanRelay CANBUS bridge discovery scan");
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }
}
