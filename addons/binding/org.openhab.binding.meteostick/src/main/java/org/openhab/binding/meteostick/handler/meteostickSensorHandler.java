/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meteostick.handler;

import static org.openhab.binding.meteostick.meteostickBindingConstants.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link meteostickSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Chris Jackson - Initial contribution
 */
public class meteostickSensorHandler extends BaseThingHandler implements meteostickEventListener {
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DAVIS);

    private Logger logger = LoggerFactory.getLogger(meteostickSensorHandler.class);

    private int channel = 0;
    private meteostickBridgeHandler bridgeHandler;

    public meteostickSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MeteoStick handler.");
        super.initialize();

        updateStatus(ThingStatus.OFFLINE);

        channel =  ((BigDecimal)getConfig().get(PARAMETER_CHANNEL)).intValue();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    protected void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        bridgeHandler = (meteostickBridgeHandler) thingHandler;

        if (channel != 0) {
            if (bridgeHandler != null) {
                bridgeHandler.subscribeEvents(channel, this);
                // getThing().setStatus(getBridge().getStatus());
            }
        }

        // Until we get an update put the Thing offline
        updateStatus(ThingStatus.OFFLINE);
    }
    
    private void processSignalStrength(String dbmString) {
        double dbm = Double.parseDouble(dbmString);
        int strength;

        if (dbm > -60) {
            strength = 4;
        } else if (dbm > -70) {
            strength = 3;
        } else if (dbm > -80) {
            strength = 2;
        } else if (dbm > -90) {
            strength = 1;
        } else {
            strength = 0;
        }

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SIGNAL_STRENGTH), new DecimalType(strength));
    }

    private void processBattery(boolean batteryLow) {
        OnOffType state = batteryLow ? OnOffType.ON : OnOffType.OFF;

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_LOW_BATTERY), state);
    }

    @Override
    public void onStateChange() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDataReceived(String[] data) {
        updateStatus(ThingStatus.ONLINE);

        switch (data[0]) {
            case "R": // Rain
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_RAIN),
                        new DecimalType(Integer.parseInt(data[2])));
                processSignalStrength(data[3]);
                processBattery(data.length == 5);
                break;
            case "W": // Wind
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_SPEED), new DecimalType(
                        Double.parseDouble(data[2])));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_DIRECTION),
                        new DecimalType(Integer.parseInt(data[3])));
                processSignalStrength(data[4]);
                processBattery(data.length == 6);
                break;
            case "T": // Temperature
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(
                        Double.parseDouble(data[2])));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY), new DecimalType(
                        Double.parseDouble(data[3])));
                processSignalStrength(data[4]);
                processBattery(data.length == 6);
                break;
            case "P": // Solar panel power
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SOLAR_POWER), new DecimalType(
                        Double.parseDouble(data[2])));
                processSignalStrength(data[3]);
                processBattery(data.length == 5);
                break;
        }
    }
}
