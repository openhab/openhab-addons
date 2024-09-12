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
package org.openhab.binding.nikohomecontrol.internal.handler;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.THREAD_NAME_PREFIX;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc1.NikoHomeControlCommunication1;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    public NikoHomeControlBridgeHandler1(Bridge nikoHomeControlBridge, NetworkAddressService networkAddressService,
            TimeZoneProvider timeZoneProvider) {
        super(nikoHomeControlBridge, networkAddressService, timeZoneProvider);
    }

    @Override
    public void initialize() {
        logger.debug("initializing bridge handler");

        scheduler.submit(() -> getControllerId());

        InetAddress addr = getAddr();
        int port = getPort();

        logger.debug("bridge handler host {}, port {}", addr, port);

        if (addr != null) {
            String eventThreadName = THREAD_NAME_PREFIX + thing.getUID().getAsString();
            nhcComm = new NikoHomeControlCommunication1(this, scheduler, eventThreadName);
            startCommunication();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.ip");
        }
    }

    @Override
    protected void updateProperties() {
        Map<String, String> properties = new HashMap<>(thing.getProperties());

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
