/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.model.VerisureClimates;

/**
 * Handler for all Climate Device thing types that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureClimateDeviceThingHandler extends VerisureThingHandler<VerisureClimates> {

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
    public Class<VerisureClimates> getVerisureThingClass() {
        return VerisureClimates.class;
    }

    @Override
    public synchronized void update(VerisureClimates thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        updateClimateDeviceState(thing);
    }

    private void updateClimateDeviceState(VerisureClimates climateJSON) {
        getThing().getChannels().stream().map(Channel::getUID)
                .filter(channelUID -> isLinked(channelUID) && !channelUID.getId().equals("timestamp"))
                .forEach(channelUID -> {
                    State state = getValue(channelUID.getId(), climateJSON);
                    updateState(channelUID, state);
                });
        updateTimeStamp(climateJSON.getData().getInstallation().getClimates().get(0).getTemperatureTimestamp());
        super.update(climateJSON);
    }

    public State getValue(String channelId, VerisureClimates climateJSON) {
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                double temperature = climateJSON.getData().getInstallation().getClimates().get(0).getTemperatureValue();
                return new QuantityType<Temperature>(temperature, SIUnits.CELSIUS);
            case CHANNEL_HUMIDITY:
                if (climateJSON.getData().getInstallation().getClimates().get(0).isHumidityEnabled()) {
                    double humidity = climateJSON.getData().getInstallation().getClimates().get(0).getHumidityValue();
                    return new DecimalType(humidity);
                }
            case CHANNEL_HUMIDITY_ENABLED:
                boolean humidityEnabled = climateJSON.getData().getInstallation().getClimates().get(0)
                        .isHumidityEnabled();
                return OnOffType.from(humidityEnabled);
            case CHANNEL_LOCATION:
                String location = climateJSON.getLocation();
                return location != null ? new StringType(location) : UnDefType.NULL;
        }
        return UnDefType.UNDEF;
    }
}
