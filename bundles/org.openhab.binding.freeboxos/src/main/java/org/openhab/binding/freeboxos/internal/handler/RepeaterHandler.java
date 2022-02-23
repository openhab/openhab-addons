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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost;
import org.openhab.binding.freeboxos.internal.api.repeater.Repeater;
import org.openhab.binding.freeboxos.internal.api.repeater.RepeaterManager;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RepeaterHandler} is responsible for interface to a freebox
 * pop repeater.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RepeaterHandler extends HostHandler {
    private final Logger logger = LoggerFactory.getLogger(RepeaterHandler.class);

    public RepeaterHandler(Thing thing) {
        super(thing);
    }

    @Override
    void internalGetProperties(Map<String, String> properties) throws FreeboxException {
        super.internalGetProperties(properties);
        getManager(RepeaterManager.class).getDevices().stream().filter(rep -> rep.getMac().equals(getMac()))
                .forEach(repeater -> {
                    properties.put(Thing.PROPERTY_SERIAL_NUMBER, repeater.getSerial());
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, repeater.getFirmwareVersion());
                    properties.put(Thing.PROPERTY_MODEL_ID, repeater.getModel());
                });
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        logger.debug("Polling Repeater status");
        RepeaterManager repeaterManager = getManager(RepeaterManager.class);

        ClientConfiguration config = getConfigAs(ClientConfiguration.class);
        List<LanHost> hosts = repeaterManager.getRepeaterHosts(config.id);
        updateChannelDecimal(REPEATER_MISC, HOST_COUNT, hosts.size());

        Repeater repeater = repeaterManager.getDevice(config.id);
        updateChannelDateTimeState(REPEATER_MISC, RPT_TIMESTAMP, repeater.getBootTime());
        updateChannelOnOff(REPEATER_MISC, LED, repeater.getLedActivated());
        updateChannelString(REPEATER_MISC, CONNECTION_STATUS, repeater.getConnection());
    }
}
