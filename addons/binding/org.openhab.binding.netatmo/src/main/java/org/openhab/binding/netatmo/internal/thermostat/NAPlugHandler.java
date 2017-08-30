/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.thermostat;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.binding.netatmo.internal.NADeviceAdapter;
import org.openhab.binding.netatmo.internal.NAPlugAdapter;
import org.openhab.binding.netatmo.internal.config.NetatmoDeviceConfiguration;

import io.swagger.client.model.NAThermostatDataBody;

/**
 * {@link NAPlugHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAPlugHandler extends NetatmoDeviceHandler<NetatmoDeviceConfiguration> {
    public NAPlugHandler(Thing thing) {
        super(thing, NetatmoDeviceConfiguration.class);
    }

    @Override
    protected NADeviceAdapter<?> updateReadings(String equipmentId) {
        NAThermostatDataBody thermostatDataBody = getBridgeHandler().getThermostatsDataBody(equipmentId);
        if (thermostatDataBody != null) {
            return new NAPlugAdapter(thermostatDataBody);
        } else {
            return null;
        }
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        NAPlugAdapter plugAdapter = (NAPlugAdapter) device;
        switch (channelId) {
            case CHANNEL_CONNECTED_BOILER:
                return plugAdapter.getConnectedBoiler() ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_LAST_PLUG_SEEN:
                return ChannelTypeUtils.toDateTimeType(plugAdapter.getLastPlugSeen());
            case CHANNEL_LAST_BILAN:
                return ChannelTypeUtils.toDateTimeType(plugAdapter.getLastBilan());
            default:
                return super.getNAThingProperty(channelId);
        }
    }

}
