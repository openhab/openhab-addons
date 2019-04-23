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
package org.openhab.binding.nikohomecontrol.internal.handler;

import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NikoHomeControlCommunication2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NikoHomeControlBridgeHandler2} is the handler for a Niko Home Control II Connected Controller and connects it
 * to the framework.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlBridgeHandler2 extends NikoHomeControlBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlBridgeHandler2.class);

    NetworkAddressService networkAddressService;

    public NikoHomeControlBridgeHandler2(Bridge nikoHomeControlBridge, NetworkAddressService networkAddressService) {
        super(nikoHomeControlBridge);
        this.networkAddressService = networkAddressService;
    }

    @Override
    public void initialize() {
        logger.debug("Niko Home Control: initializing NHC II bridge handler");

        setConfig();

        String profile = getProfile();
        logger.debug("Niko Home Control: touch profile {}", profile);
        if (profile.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: profile name not set.");
            return;
        }

        String password = getPassword();
        if (password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: password for profile cannot be empty.");
            return;
        }

        String addr = networkAddressService.getPrimaryIpv4HostAddress();
        addr = (addr == null) ? "unknown" : addr.replace(".", "_");
        String clientId = addr + "-" + thing.getUID().toString().replace(":", "_");
        String userDataFolder = ConfigConstants.getUserDataFolder();
        try {
            nhcComm = new NikoHomeControlCommunication2(this, clientId, userDataFolder);
            startCommunication();
        } catch (CertificateException e) {
            // this should not happen unless there is a programming error
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Niko Home Control: not able to set SSL context");
            return;
        }
    }

    @Override
    protected void updateProperties() {
        Map<String, String> properties = new HashMap<>();

        NikoHomeControlCommunication2 comm = (NikoHomeControlCommunication2) nhcComm;
        if (comm != null) {
            properties.put("nhcVersion", comm.getSystemInfo().getNhcVersion());
            properties.put("cocoImage", comm.getSystemInfo().getCocoImage());
            properties.put("language", comm.getSystemInfo().getLanguage());
            properties.put("currency", comm.getSystemInfo().getCurrency());
            properties.put("units", comm.getSystemInfo().getUnits());
            properties.put("lastConfig", comm.getSystemInfo().getLastConfig());
            properties.put("electricityTariff", comm.getSystemInfo().getElectricityTariff());
            properties.put("gasTariff", comm.getSystemInfo().getGasTariff());
            properties.put("waterTariff", comm.getSystemInfo().getWaterTariff());
            properties.put("timeZone", comm.getTimeInfo().getTimeZone());
            properties.put("isDST", comm.getTimeInfo().getIsDst());
            properties.put("services", comm.getServices());

            thing.setProperties(properties);
        }
    }

    @Override
    public String getProfile() {
        String profile = ((NikoHomeControlBridgeConfig2) config).profile;
        if (profile == null) {
            return "";
        }
        return profile;
    }

    @Override
    public String getPassword() {
        String password = ((NikoHomeControlBridgeConfig2) config).password;
        if ((password == null) || password.isEmpty()) {
            logger.debug("Niko Home Control: no password set.");
            return "";
        }
        return password;
    }

    @Override
    protected synchronized void setConfig() {
        config = getConfig().as(NikoHomeControlBridgeConfig2.class);
    }
}
