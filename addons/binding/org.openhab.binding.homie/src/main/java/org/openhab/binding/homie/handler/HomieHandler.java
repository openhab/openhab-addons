/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.handler;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.homie.internal.MqttConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomieHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieHandler extends BaseThingHandler implements IMqttMessageListener {

    private Logger logger = LoggerFactory.getLogger(HomieHandler.class);

    public HomieHandler(Thing thing) {
        super(thing);

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if(channelUID.getId().equals(CHANNEL_1)) {
        // // TODO: handle command
        //
        // // Note: if communication with thing fails for some reason,
        // // indicate that by setting the status with detail information
        // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        MqttConnection.getInstance().subscribe(thing, this);
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (topic.endsWith("$stats/uptime")) {
            ChannelUID channel = new ChannelUID("uptime");
            triggerChannel(channel, message.toString());
        } else if (topic.endsWith("$online")) {
            ChannelUID channel = new ChannelUID("online");
            triggerChannel(channel, message.toString());
        } else if (topic.endsWith("$name")) {
            getThing().setLabel(message.toString());
            updateThing(thing);
        }

        /*
         * $homie Device → Controller Version of the Homie convention the device conforms to Yes Yes
         * $online Device → Controller true when the device is online, false when the device is offline (through LWT).
         * When sending the device is online, this message must be sent last, to indicate every other required messages
         * are sent and the device is ready Yes Yes
         * $name Device → Controller Friendly name of the device Yes Yes
         * $localip Device → Controller IP of the device on the local network Yes Yes
         * $mac Device → Controller Mac address of the device network interface. The format MUST be of the type
         * A1:B2:C3:D4:E5:F6 Yes Yes
         * $stats/uptime Device → Controller Time elapsed in seconds since the boot of the device Yes Yes
         * $stats/signal Device → Controller Integer representing the Wi-Fi signal quality in percentage if applicable
         * Yes No, this is not applicable to an Ethernet connected device for example
         * $stats/interval Device → Controller Interval in seconds at which the $stats/uptime and $stats/signal are
         * refreshed Yes Yes
         * $fw/name Device → Controller Name of the firmware running on the device. Allowed characters are the same as
         * the device ID Yes Yes
         * $fw/version Device → Controller Version of the firmware running on the device Yes Yes
         * $fw/checksum Device → Controller MD5 checksum of the firmware running on the device Yes No, depending of your
         * implementation
         * $implementation Device → Controller An identifier for the Homie implementation (example esp8266) Yes Yes
         * $implementation/
         */
    }
}
