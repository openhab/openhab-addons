/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import static org.openhab.binding.wink.WinkBindingConstants.CHANNEL_LIGHTLEVEL;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

/**
 * TODO: The {@link LightBulbHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastien Marchand - Initial contribution
 */
public class LightBulbHandler extends WinkHandler {

    public LightBulbHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (!this.deviceConfig.validateConfig()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid config.");
            return;
        }
        test();
        // TODO: Update status.
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected String getDeviceRequestPath() {
        return "light_bulbs/" + this.deviceConfig.getDeviceId();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            ReadDeviceState();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            if (command instanceof Number) {
                int level = ((Number) command).intValue();
                setLightLevel(level);
            } else if (command.equals(OnOffType.ON)) {
                // TODO: Add an ON level to the config?
                setLightLevel(100);
            } else if (command.equals(OnOffType.OFF)) {
                setLightLevel(0);
            } else if (command instanceof RefreshType) {
                logger.debug("Refreshing state");
                ReadDeviceState();
            }
        }
    }

    private void setLightLevel(int level) {
        DecimalFormat df = new DecimalFormat("0.00");
        String levelFormated = df.format(level / 100.0);
        if (level > 0) {
            sendCommand("{\"desired_state\":{\"powered\": true, \"brightness\": " + levelFormated + "}}");
        } else {
            sendCommand("{\"desired_state\": {\"powered\": false}}");
        }

    }

    private void updateState(JsonObject jsonDataBlob) {
        int brightness = Math
                .round(jsonDataBlob.get("last_reading").getAsJsonObject().get("brightness").getAsFloat() * 100);
        updateState(CHANNEL_LIGHTLEVEL, new PercentType(brightness));
    }

    @Override
    public void sendCommandCallback(JsonObject jsonResult) {
        // TODO: Is there something to do here? Maybe verify that the request succeed (e.g. that the device is online
        // etc...)
    }

    @Override
    public void updateDeviceStateCallback(JsonObject jsonDataBlob) {
        updateState(jsonDataBlob);
    }

    private void test() {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-f7bf7f7e-0542-11e3-a5e8-02ee2ddab7fe");

        PubNub pubnub = new PubNub(pnConfiguration);
        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                logger.info("message : " + message.getMessage());
                // Handle new message stored in message.message
                if (message.getChannel() != null) {
                    // Message has been received on channel group stored in
                    // message.getChannel()
                } else {
                    // Message has been received on channel stored in
                    // message.getSubscription()
                }

                /*
                 * log the following items with your favorite logger
                 * - message.getMessage()
                 * - message.getSubscription()
                 * - message.getTimetoken()
                 */
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                logger.info("PRESENCE");
            }

            @Override
            public void status(PubNub arg0, PNStatus arg1) {
                logger.info("STATUS");
            }
        });

        pubnub.subscribe()
                .channels(Arrays.asList("13ec77004f36b3a3af00264e42c9ac5ccd6008ef|light_bulb-2369974|user-608902"))
                .execute();
    }
}
