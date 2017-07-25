/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miflora.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiFloraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class MiFloraHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MiFloraHandler.class);

    public MiFloraHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // In case of communication error or somesuch
            // updateStatus(ThingStatus.OFFLINE);
            // else
            // updateStatus(ThingStatus.ONLINE);
            // and update data

            logger.debug("Handling command {} with data {}", command, channelUID);
        } else {
            logger.debug("Unsupported command {}! Supported commands: REFRESH", command);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");

        // thingConfiguration = getConfigAs(SenseBoxConfiguration.class);
        String thingUid = getThing().getUID().toString();
        // thingConfiguration.setThingUid(thingUid);

        // logger.debug("Thing Configuration {} initialized {}", thingConfiguration.getThingUid(),
        // thingConfiguration.getSenseBoxId());
        //
        String offlineReason = "";
        boolean validConfig = true;
        //
        // if (StringUtils.trimToNull(thingConfiguration.getSenseBoxId()) == null) {
        //     offlineReason = "senseBox ID is mandatory and must be configured";
        //     logger.error("{}, disabling thing '{}'", offlineReason, thingUid);
        //     validConfig = false;
        // }
        //
        // if (thingConfiguration.getRefreshInterval() < MINIMUM_UPDATE_INTERVAL) {
        //     logger.info("Refresh interval is much too small, setting to default of {} seconds",
        //             MINIMUM_UPDATE_INTERVAL);
        //     thingConfiguration.setRefreshInterval(MINIMUM_UPDATE_INTERVAL);
        // }
        //
        if (validConfig) {
            // logger.debug("Thing Configuration: {}", thingConfiguration.toString());
            updateStatus(ThingStatus.ONLINE);
            //     startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, offlineReason);
        }

        // updateStatus(ThingStatus.UNKNOWN);
        updateStatus(ThingStatus.ONLINE);

        logger.debug("Thing {} initialized {}", getThing().getUID(), getThing().getStatus());
    }
}
