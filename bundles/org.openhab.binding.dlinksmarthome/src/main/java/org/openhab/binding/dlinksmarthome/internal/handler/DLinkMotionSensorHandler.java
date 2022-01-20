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
package org.openhab.binding.dlinksmarthome.internal.handler;

import static org.openhab.binding.dlinksmarthome.internal.DLinkSmartHomeBindingConstants.MOTION;

import org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorCommunication;
import org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorCommunication.DeviceStatus;
import org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorConfig;
import org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorListener;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

/**
 * The {@link DLinkMotionSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mike Major - Initial contribution
 */
public class DLinkMotionSensorHandler extends BaseThingHandler implements DLinkMotionSensorListener {

    private DLinkMotionSensorCommunication motionSensor;

    private final ChannelUID motionChannel;

    public DLinkMotionSensorHandler(final Thing thing) {
        super(thing);
        motionChannel = new ChannelUID(getThing().getUID(), MOTION);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // Does not support commands
    }

    @Override
    public void initialize() {
        final DLinkMotionSensorConfig config = getConfigAs(DLinkMotionSensorConfig.class);
        motionSensor = new DLinkMotionSensorCommunication(config, this, scheduler);
    }

    @Override
    public void motionDetected() {
        triggerChannel(motionChannel);
    }

    @Override
    public void sensorStatus(final DeviceStatus status) {
        switch (status) {
            case ONLINE:
                updateStatus(ThingStatus.ONLINE);
                break;
            case COMMUNICATION_ERROR:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                break;
            case INVALID_PIN:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid pin code");
                break;
            case INTERNAL_ERROR:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "System error");
                break;
            case UNSUPPORTED_FIRMWARE:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unsupported firmware");
                break;
            default:
                break;
        }
    }

    @Override
    public void dispose() {
        if (motionSensor != null) {
            motionSensor.dispose();
        }
        super.dispose();
    }
}
