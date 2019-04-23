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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc1.NikoHomeControlCommunication1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NikoHomeControlBridgeHandler1} is the handler for a Niko Home Control I IP-interface and connects it to
 * the framework.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlBridgeHandler1 extends NikoHomeControlBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlBridgeHandler1.class);

    public NikoHomeControlBridgeHandler1(Bridge nikoHomeControlBridge) {
        super(nikoHomeControlBridge);
    }

    @Override
    public void initialize() {
        logger.debug("Niko Home Control: initializing bridge handler");

        setConfig();
        InetAddress addr = getAddr();
        int port = getPort();

        logger.debug("Niko Home Control: bridge handler host {}, port {}", addr, port);

        if (addr != null) {
            nhcComm = new NikoHomeControlCommunication1(this, scheduler);
            startCommunication();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Niko Home Control: cannot resolve bridge IP with hostname " + config.addr);
        }
    }

    @Override
    protected void updateProperties() {
        Map<String, String> properties = new HashMap<>();

        NikoHomeControlCommunication1 comm = (NikoHomeControlCommunication1) nhcComm;
        if (comm != null) {
            properties.put("softwareVersion", comm.getSystemInfo().getSwVersion());
            properties.put("apiVersion", comm.getSystemInfo().getApi());
            properties.put("language", comm.getSystemInfo().getLanguage());
            properties.put("currency", comm.getSystemInfo().getCurrency());
            properties.put("units", comm.getSystemInfo().getUnits());
            properties.put("tzOffset", comm.getSystemInfo().getTz());
            properties.put("dstOffset", comm.getSystemInfo().getDst());
            properties.put("configDate", comm.getSystemInfo().getLastConfig());
            properties.put("energyEraseDate", comm.getSystemInfo().getLastEnergyErase());
            properties.put("connectionStartDate", comm.getSystemInfo().getTime());

            thing.setProperties(properties);
        }
    }
}
