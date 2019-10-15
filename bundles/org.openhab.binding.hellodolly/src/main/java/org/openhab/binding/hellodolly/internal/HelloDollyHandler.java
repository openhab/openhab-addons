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
package org.openhab.binding.hellodolly.internal;

import static org.openhab.binding.hellodolly.internal.HelloDollyBindingConstants.*;

import java.util.ArrayList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HelloDollyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jochen Bauer - Initial contribution
 */
@NonNullByDefault
public class HelloDollyHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HelloDollyHandler.class);

    // Hello Dolly Song by Louis Armstrong
    ArrayList<String> hdl;

    private @Nullable HelloDollyConfiguration config;

    public HelloDollyHandler(Thing thing) {
        super(thing);
        this.hdl = new ArrayList<String>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        //logger.debug("Hello Dolly | handleCommand()!");
        if (CHANNEL_0.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }
        }
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
                if (this.hdl == null){
                    fillHelloDollyArray();
                }
            }
        }
        // change value after button is triggered
        if (CHANNEL_2.equals(channelUID.getId())) {
            if (this.hdl == null){
                fillHelloDollyArray();
            }

            if (command instanceof RefreshType) {
                // TODO: handle data refresh
                if (this.hdl == null){
                    fillHelloDollyArray();
                }
            }

            // TODO: handle command

            // Random number between 0 and 4
            int randomNumber = getRandomIntegerBetweenRange(0, 4);
            String randomLine = this.hdl.get(randomNumber);

            // set state of text field
            try {
                State newState = new StringType(randomLine);
                updateState(CHANNEL_1, newState);
            } catch (Exception ex) {
                logger.error("Can't update state for channel {} : {}", channelUID, ex.getMessage(), ex);
            }

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(HelloDollyConfiguration.class);
        
        if (hdl == null){
            fillHelloDollyArray();
        }

        int randomNumber = getRandomIntegerBetweenRange(0, 4);
        String randomLine = this.hdl.get(randomNumber);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
                State newState = new StringType(randomLine);
                updateState(CHANNEL_1, newState);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private void fillHelloDollyArray(){
        // Hello Dolly Song by Louis Armstrong
        this.hdl = new ArrayList<String>();
        hdl.add("It's so nice to have you back where you belong");
        hdl.add("You're lookin' swell, Dolly");
        hdl.add("You're still glowin', you're still crowin'");
        hdl.add("You're still goin' strong");
        hdl.add("Dolly'll never go away again Hello, Dolly,");
    }

    private int getRandomIntegerBetweenRange(int min, int max){
        double randomVal = Math.random();
        double multA = max - min + 1;
        double sumA = randomVal * multA;
        double sumB = sumA + min;
        //logger.info("Hello Dolly Binding - getRandomIntegerBetweenRange(" + min + ", " + max + "): " + randomVal + "; " + sumA + "; " + sumB + ";");
        int y = (int) sumB;
        return y;
    }
}
