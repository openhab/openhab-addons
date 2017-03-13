/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinksmarthome.handler;

import static org.openhab.binding.dlinksmarthome.DLinkSmartHomeBindingConstants.MOTION;
import static org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorConfig.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorCommunication;
import org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorCommunication.DeviceStatus;
import org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorConfig;
import org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorListener;

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
        if (getConfig().get(REBOOT_INTERVAL) == null) {
            final Configuration configuration = editConfiguration();
            if ("A1".equals(getThing().getProperties().get(HARDWARE_VERSION))) {
                configuration.put(REBOOT_INTERVAL, 4);
            } else {
                configuration.put(REBOOT_INTERVAL, 24);
            }
            updateConfiguration(configuration);
        }

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
            case REBOOTING:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "Device rebooting");
                break;
            case COMMUNICATION_ERROR:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                break;
            case INVALID_PIN:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid pin code");
                break;
            case INVALID_TIME:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid reboot time or interval");
                break;
            case INTERNAL_ERROR:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "System error");
                break;
            case UNSUPPORTED_FIRMWARE:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Unsupported firmware");
                break;
            default:
                break;
        }
    }

    @Override
    public void dispose() {
        motionSensor.dispose();
        super.dispose();
    }
}
