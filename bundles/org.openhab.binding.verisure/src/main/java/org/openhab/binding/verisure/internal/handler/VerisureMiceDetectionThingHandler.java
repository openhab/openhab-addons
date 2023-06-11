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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.verisure.internal.dto.VerisureMiceDetectionDTO;
import org.openhab.binding.verisure.internal.dto.VerisureMiceDetectionDTO.Detection;
import org.openhab.binding.verisure.internal.dto.VerisureMiceDetectionDTO.Mouse;
import org.openhab.core.library.types.DecimalType;
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
 * Handler for the Mice Detection thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureMiceDetectionThingHandler extends VerisureThingHandler<VerisureMiceDetectionDTO> {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_MICE_DETECTION);

    public VerisureMiceDetectionThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Class<VerisureMiceDetectionDTO> getVerisureThingClass() {
        return VerisureMiceDetectionDTO.class;
    }

    @Override
    public synchronized void update(VerisureMiceDetectionDTO thing) {
        updateMiceDetectionState(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateMiceDetectionState(VerisureMiceDetectionDTO miceDetectionJSON) {
        List<Mouse> miceList = miceDetectionJSON.getData().getInstallation().getMice();
        if (!miceList.isEmpty()) {
            Mouse mouse = miceList.get(0);
            getThing().getChannels().stream().map(Channel::getUID).filter(channelUID -> isLinked(channelUID)
                    && !channelUID.getId().equals("timestamp") && !channelUID.getId().equals("temperatureTimestamp"))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), miceDetectionJSON, mouse);
                        updateState(channelUID, state);
                    });
            if (mouse.getDetections().size() != 0) {
                updateTimeStamp(mouse.getDetections().get(0).getNodeTime());
            }
            updateTimeStamp(miceDetectionJSON.getTemperatureTime(), CHANNEL_TEMPERATURE_TIMESTAMP);
            updateInstallationChannels(miceDetectionJSON);
        } else {
            logger.debug("MiceList is empty!");
        }
    }

    public State getValue(String channelId, VerisureMiceDetectionDTO miceDetectionJSON, Mouse mouse) {
        switch (channelId) {
            case CHANNEL_COUNT_LATEST_DETECTION:
                if (mouse.getDetections().size() == 0) {
                    return new DecimalType(0);
                } else {
                    return new DecimalType(mouse.getDetections().get(0).getCount());
                }
            case CHANNEL_COUNT_LAST_24_HOURS:
                if (mouse.getDetections().size() == 0) {
                    return new DecimalType(0);
                } else {
                    return new DecimalType(mouse.getDetections().stream().mapToInt(Detection::getCount).sum());
                }
            case CHANNEL_DURATION_LATEST_DETECTION:
                if (mouse.getDetections().size() == 0) {
                    return new QuantityType<Time>(0, Units.SECOND);
                } else {
                    return new QuantityType<Time>(mouse.getDetections().get(0).getDuration(), Units.SECOND);
                }
            case CHANNEL_DURATION_LAST_24_HOURS:
                if (mouse.getDetections().size() == 0) {
                    return new QuantityType<Time>(0, Units.SECOND);
                } else {
                    return new QuantityType<Time>(mouse.getDetections().stream().mapToInt(Detection::getDuration).sum(),
                            Units.SECOND);
                }
            case CHANNEL_LOCATION:
                String location = mouse.getDevice().getArea();
                return location != null ? new StringType(location) : UnDefType.NULL;
            case CHANNEL_TEMPERATURE:
                double temperature = miceDetectionJSON.getTemperatureValue();
                return temperature != VerisureMiceDetectionDTO.UNDEFINED
                        ? new QuantityType<Temperature>(temperature, SIUnits.CELSIUS)
                        : UnDefType.UNDEF;
        }
        return UnDefType.UNDEF;
    }

    @Override
    public void updateTriggerChannel(String event) {
        logger.debug("MiceThingHandler trigger event {}", event);
        triggerChannel(CHANNEL_MICE_DETECTION_TRIGGER_CHANNEL, event);
    }
}
