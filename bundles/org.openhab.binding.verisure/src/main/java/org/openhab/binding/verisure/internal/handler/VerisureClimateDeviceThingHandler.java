/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.List;
import java.util.Set;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.verisure.internal.dto.VerisureBatteryStatusDTO;
import org.openhab.binding.verisure.internal.dto.VerisureClimatesDTO;
import org.openhab.binding.verisure.internal.dto.VerisureClimatesDTO.Climate;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Handler for all Climate Device thing types that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureClimateDeviceThingHandler extends VerisureThingHandler<VerisureClimatesDTO> {

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
    public Class<VerisureClimatesDTO> getVerisureThingClass() {
        return VerisureClimatesDTO.class;
    }

    @Override
    public synchronized void update(VerisureClimatesDTO thing) {
        updateClimateDeviceState(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateClimateDeviceState(VerisureClimatesDTO climateJSON) {
        getThing().getChannels().stream().map(Channel::getUID)
                .filter(channelUID -> isLinked(channelUID) && !"timestamp".equals(channelUID.getId()))
                .forEach(channelUID -> {
                    State state = getValue(channelUID.getId(), climateJSON);
                    updateState(channelUID, state);
                });
        List<Climate> climateList = climateJSON.getData().getInstallation().getClimates();
        if (climateList != null && !climateList.isEmpty()) {
            String timeStamp = climateList.get(0).getTemperatureTimestamp();
            if (timeStamp != null) {
                updateTimeStamp(timeStamp);
            }
        }

        updateInstallationChannels(climateJSON);
    }

    public State getValue(String channelId, VerisureClimatesDTO climateJSON) {
        List<Climate> climateList = climateJSON.getData().getInstallation().getClimates();
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                if (climateList != null && !climateList.isEmpty()) {
                    double temperature = climateList.get(0).getTemperatureValue();
                    return new QuantityType<Temperature>(temperature, SIUnits.CELSIUS);
                }
            case CHANNEL_HUMIDITY:
                if (climateList != null && !climateList.isEmpty() && climateList.get(0).isHumidityEnabled()) {
                    double humidity = climateList.get(0).getHumidityValue();
                    return new QuantityType<Dimensionless>(humidity, Units.PERCENT);
                }
            case CHANNEL_HUMIDITY_ENABLED:
                if (climateList != null && !climateList.isEmpty()) {
                    boolean humidityEnabled = climateList.get(0).isHumidityEnabled();
                    return OnOffType.from(humidityEnabled);
                }
            case CHANNEL_LOCATION:
                String location = climateJSON.getLocation();
                return location != null ? new StringType(location) : UnDefType.NULL;
            case CHANNEL_BATTERY_STATUS:
                VerisureBatteryStatusDTO batteryStatus = climateJSON.getBatteryStatus();
                if (batteryStatus != null) {
                    String status = batteryStatus.getStatus();
                    if ("CRITICAL".equals(status)) {
                        return OnOffType.from(true);
                    }
                }
                return OnOffType.from(false);
        }
        return UnDefType.UNDEF;
    }

    @Override
    public void updateTriggerChannel(String event) {
        logger.debug("ClimateThingHandler trigger event {}", event);
        triggerChannel(CHANNEL_SMOKE_DETECTION_TRIGGER_CHANNEL, event);
    }
}
