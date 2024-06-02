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
package org.openhab.binding.govee.internal;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.govee.internal.model.DiscoveryData;
import org.openhab.binding.govee.internal.model.DiscoveryResponse;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers Govee devices
 *
 * Scan approach:
 * 1. Determines all local network interfaces
 * 2. Send a multicast message on each interface to the Govee multicast address 239.255.255.250 at port 4001
 * 3. Retrieve the list of devices
 *
 * Based on the description at https://app-h5.govee.com/user-manual/wlan-guide
 *
 * A typical scan response looks as follows
 *
 * <pre>{@code
 * {
 *   "msg":{
 *     "cmd":"scan",
 *     "data":{
 *       "ip":"192.168.1.23",
 *       "device":"1F:80:C5:32:32:36:72:4E",
 *       "sku":"Hxxxx",
 *       "bleVersionHard":"3.01.01",
 *       "bleVersionSoft":"1.03.01",
 *       "wifiVersionHard":"1.00.10",
 *       "wifiVersionSoft":"1.02.03"
 *     }
 *   }
 * }
 * }
 * </pre>
 *
 * Note that it uses the same port for receiving data like when receiving devices status updates.
 *
 * @see GoveeHandler
 *
 * @author Stefan Höhn - Initial Contribution
 * @author Danny Baumann - Thread-Safe design refactoring
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.govee")
public class GoveeDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(GoveeDiscoveryService.class);

    private CommunicationManager communicationManager;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(GoveeBindingConstants.THING_TYPE_LIGHT);

    @Activate
    public GoveeDiscoveryService(@Reference TranslationProvider i18nProvider, @Reference LocaleProvider localeProvider,
            @Reference CommunicationManager communicationManager) {
        super(SUPPORTED_THING_TYPES_UIDS, 0, false);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.communicationManager = communicationManager;
    }

    // for test purposes only
    public GoveeDiscoveryService(CommunicationManager communicationManager) {
        super(SUPPORTED_THING_TYPES_UIDS, 0, false);
        this.communicationManager = communicationManager;
    }

    @Override
    protected void startScan() {
        logger.debug("starting Scan");

        getLocalNetworkInterfaces().forEach(localNetworkInterface -> {
            logger.debug("Discovering Govee devices on {} ...", localNetworkInterface);
            try {
                communicationManager.runDiscoveryForInterface(localNetworkInterface, response -> {
                    DiscoveryResult result = responseToResult(response);
                    if (result != null) {
                        thingDiscovered(result);
                    }
                });
                logger.trace("After runDiscoveryForInterface");
            } catch (IOException e) {
                logger.debug("Discovery with IO exception: {}", e.getMessage());
            }
            logger.trace("After try");
        });
    }

    public @Nullable DiscoveryResult responseToResult(DiscoveryResponse response) {
        final DiscoveryData data = response.msg().data();
        final String macAddress = data.device();
        if (macAddress.isEmpty()) {
            logger.warn("Empty Mac address received during discovery - ignoring {}", response);
            return null;
        }

        final String ipAddress = data.ip();
        if (ipAddress.isEmpty()) {
            logger.warn("Empty IP address received during discovery - ignoring {}", response);
            return null;
        }

        final String sku = data.sku();
        if (sku.isEmpty()) {
            logger.warn("Empty SKU (product name) received during discovery - ignoring {}", response);
            return null;
        }

        final String productName;
        if (i18nProvider != null) {
            Bundle bundle = FrameworkUtil.getBundle(GoveeDiscoveryService.class);
            productName = i18nProvider.getText(bundle, "discovery.govee-light." + sku, null,
                    localeProvider.getLocale());
        } else {
            productName = sku;
        }
        String nameForLabel = productName != null ? productName + " " + sku : sku;

        ThingUID thingUid = new ThingUID(GoveeBindingConstants.THING_TYPE_LIGHT, macAddress.replace(":", "_"));
        DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUid)
                .withRepresentationProperty(GoveeBindingConstants.MAC_ADDRESS)
                .withProperty(GoveeBindingConstants.MAC_ADDRESS, macAddress)
                .withProperty(GoveeBindingConstants.IP_ADDRESS, ipAddress)
                .withProperty(GoveeBindingConstants.DEVICE_TYPE, sku)
                .withLabel(String.format("Govee %s (%s)", nameForLabel, ipAddress));

        if (productName != null) {
            builder.withProperty(GoveeBindingConstants.PRODUCT_NAME, productName);
        }

        String hwVersion = data.wifiVersionHard();
        if (hwVersion != null) {
            builder.withProperty(GoveeBindingConstants.HW_VERSION, hwVersion);
        }
        String swVersion = data.wifiVersionSoft();
        if (swVersion != null) {
            builder.withProperty(GoveeBindingConstants.SW_VERSION, swVersion);
        }

        return builder.build();
    }

    private List<NetworkInterface> getLocalNetworkInterfaces() {
        List<NetworkInterface> result = new LinkedList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                try {
                    if (networkInterface.isUp() && !networkInterface.isLoopback()
                            && !networkInterface.isPointToPoint()) {
                        result.add(networkInterface);
                    }
                } catch (SocketException exception) {
                    // ignore
                }
            }
        } catch (SocketException exception) {
            return List.of();
        }
        return result;
    }
}
