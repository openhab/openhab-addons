/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.orvibo.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AllOneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Janis Steder - Initial contribution
 */
public class AllOneHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(AllOneHandler.class);

    // private OrviboMain orvibo;

    public AllOneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        /*
         * // if uninitialized -> subscribe
         * // send command
         * switch (channelUID.getId()) {
         * case OrviboBindingConstants.EMIT:
         * try {
         * orvibo.emit(command.toString());
         * } catch (IOException e) {
         * logger.error("Orvibo: Could not emit signal: " + e.getMessage());
         * e.printStackTrace();
         * }
         * break;
         * case OrviboBindingConstants.LEARN:
         * try {
         * orvibo.learnMode();
         * } catch (IOException e) {
         * logger.error("Orvibo: Could not enter learn mode: " + e.getMessage());
         * e.printStackTrace();
         * }
         * case OrviboBindingConstants.LEARN_NAME:
         * orvibo.setLearnName(command.toString());
         * default:
         * logger.debug("Command received for an unknown channel: {}", channelUID.getId());
         * break;
         * }
         */
    }

    @Override
    public void initialize() {
        configure();
        /*
         * logger.debug("Initializing Network handler.");
         * Configuration conf = this.getConfig();
         *
         * super.initialize();
         * this.orvibo = new OrviboMain();
         * try {
         * this.orvibo.setIP(String.valueOf(conf.get(OrviboBindingConstants.PARAMETER_HOSTNAME)));
         * this.orvibo.setRoot(String.valueOf(conf.get(OrviboBindingConstants.PARAMETER_ROOT)));
         * this.orvibo.setMac(String.valueOf(conf.get(OrviboBindingConstants.PARAMETER_MAC_ADDRESS)));
         * this.orvibo.connect();
         * } catch (IOException e) {
         * logger.error("Orvibo init failed: " + e.getMessage());
         * e.printStackTrace();
         * this.updateStatus(ThingStatus.OFFLINE);
         * } catch (NumberFormatException e) {
         * logger.error("Orvibo init failed: Invalid Mac " + e.getMessage());
         * e.printStackTrace();
         * this.updateStatus(ThingStatus.OFFLINE);
         * } catch (OrviboException e) {
         * logger.error("Orvibo init failed: Invalid Mac " + e.getMessage());
         * e.printStackTrace();
         * this.updateStatus(ThingStatus.OFFLINE);
         * }
         */
    }

    private void configure() {
        // TODO Auto-generated method stub
        updateStatus(ThingStatus.ONLINE);
    }
}
