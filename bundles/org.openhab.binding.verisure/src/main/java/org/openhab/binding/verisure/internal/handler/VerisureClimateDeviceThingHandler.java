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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.verisure.internal.model.VerisureClimatesJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

/**
 * Handler for all Climate Device thing types that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureClimateDeviceThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES.add(THING_TYPE_SMOKEDETECTOR);
        SUPPORTED_THING_TYPES.add(THING_TYPE_WATERDETECTOR);
        SUPPORTED_THING_TYPES.add(THING_TYPE_SIREN);
        SUPPORTED_THING_TYPES.add(THING_TYPE_NIGHT_CONTROL);
    }

    public VerisureClimateDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_SMOKEDETECTOR)
                || getThing().getThingTypeUID().equals(THING_TYPE_WATERDETECTOR)
                || getThing().getThingTypeUID().equals(THING_TYPE_NIGHT_CONTROL)
                || getThing().getThingTypeUID().equals(THING_TYPE_SIREN)) {
            VerisureClimatesJSON obj = (VerisureClimatesJSON) thing;
            if (obj != null) {
                updateClimateDeviceState(obj);
            }
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateClimateDeviceState(VerisureClimatesJSON climateJSON) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE);
        Double temperature = climateJSON.getData().getInstallation().getClimates().get(0).getTemperatureValue();
        if (temperature != null) {
            updateState(cuid, new QuantityType<Temperature>(temperature, SIUnits.CELSIUS));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY);
        Boolean humidityEnabled = climateJSON.getData().getInstallation().getClimates().get(0).isHumidityEnabled();
        if (humidityEnabled) {
            cuid = new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY);
            Double humidity = climateJSON.getData().getInstallation().getClimates().get(0).getHumidityValue();
            updateState(cuid, new DecimalType(humidity));
        }
        updateTimeStamp(climateJSON.getData().getInstallation().getClimates().get(0).getTemperatureTimestamp());
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
        updateState(cuid, new StringType(climateJSON.getLocation()));
        super.update(climateJSON);
    }
}
