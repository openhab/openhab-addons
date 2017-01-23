/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.handler;

import static org.openhab.binding.tankerkoenig.TankerkoenigBindingConstants.*;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.tankerkoenig.internal.config.LittleStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TankerkoenigHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dennis Dollinger - Initial contribution
 */
public class TankerkoenigHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(TankerkoenigHandler.class);

    private String locationID;
    @SuppressWarnings("unused")
    private ScheduledFuture<?> pollingJob;

    public TankerkoenigHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            // updateData();
        } else {
            logger.warn("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Tankerkoenig handler '{}'", getThing().getUID());

        Configuration config = getThing().getConfiguration();
        this.setLocationID((String) config.get("locationid"));

        Bridge b = this.getBridge();

        if (b == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Could not find bridge (tankerkoenig config). Did you select one?");
            return;
        }

        BridgeHandler handler = (BridgeHandler) b.getHandler();
        boolean registeredSuccessfully = handler.RegisterTankstelleThing(getThing());

        if (registeredSuccessfully == false) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The limitation of tankstellen things for one tankstellen config (the bridge) is limited to 10");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

    }

    @Override
    public void handleRemoval() {

        Bridge b = this.getBridge();
        BridgeHandler handler = (BridgeHandler) b.getHandler();
        handler.UnregisterTankstelleThing(getThing());
        super.handleRemoval();
    }

    /***
     * Updates the channels of a tankstelle item
     *
     * @param station
     */
    public void updateData(LittleStation station) {
        logger.debug("Update Tankerkoenig data '{}'", getThing().getUID());

        DecimalType diesel = new DecimalType(station.getDiesel());
        DecimalType e10 = new DecimalType(station.getE10());
        DecimalType e5 = new DecimalType(station.getE5());

        updateState(CHANNEL_DIESEL, diesel);
        updateState(CHANNEL_E10, e10);
        updateState(CHANNEL_E5, e5);

    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }
}
