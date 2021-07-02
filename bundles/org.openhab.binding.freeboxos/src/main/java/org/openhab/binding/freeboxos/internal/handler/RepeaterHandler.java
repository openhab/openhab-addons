/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.time.ZoneId;
import java.util.List;

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
 * The {@link RepeaterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RepeaterHandler extends HostHandler {
    private final Logger logger = LoggerFactory.getLogger(RepeaterHandler.class);

    public RepeaterHandler(Thing thing, ZoneId zoneId) {
        super(thing, zoneId);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        logger.debug("Polling Repeater status");
        RepeaterManager repeaterManager = getApi().getRepeaterManager();

        ClientConfiguration config = getConfigAs(ClientConfiguration.class);
        List<LanHost> hosts = repeaterManager.getRepeaterHosts(config.id);
        updateChannelDecimal(REPEATER_MISC, HOST_COUNT, hosts.size());

        Repeater repeater = repeaterManager.getRepeater(config.id);
        updateChannelDateTimeState(REPEATER_MISC, RPT_TIMESTAMP, repeater.getBootTime());
        updateChannelOnOff(REPEATER_MISC, LED, repeater.getLedActivated());
        updateChannelString(REPEATER_MISC, CONNECTION_STATUS, repeater.getConnection());
    }
}
