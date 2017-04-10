/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mailserver.handler;

import static org.openhab.binding.mailserver.MailServerBindingConstants.*;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MailServerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jereme Guenther - Initial contribution
 */
public class MailServerHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(MailServerHandler.class);
    private int TotalMessageCount = 0;

    Pattern pCommand = null;
    Pattern pValue = null;
    private int idxCommand = 1;
    private int idxValue = 1;
    public String ToAddress = "";

    public MailServerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // there are no commands to handle
    }

    @Override
    public void initialize() {
        /*
         * Get config values
         */
        try {
            Configuration config = this.getConfig();
            ToAddress = config.get(TO_ADDRESS).toString().toLowerCase();
            idxCommand = ((BigDecimal) config.get(PATTERN_COMMAND_INDEX)).intValue();
            idxValue = ((BigDecimal) config.get(PATTERN_VALUE_INDEX)).intValue();
            pCommand = Pattern.compile(config.get(PATTERN_COMMAND).toString());
            pValue = Pattern.compile(config.get(PATTERN_VALUE).toString());
        } catch (NullPointerException e) {
            // keep default
        }

        /*
         * Setup the inbox listener
         */
        Thing t = getThing();
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.", idxCommand);
            return;
        }
        MailServerBridgeHandler bridgehandler = (MailServerBridgeHandler) bridge.getHandler();
        MailServerHandler handler = (MailServerHandler) t.getHandler();
        if (bridgehandler != null && bridgehandler.registerMailListener(handler)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
        }
    }

    @Override
    public void dispose() {
        /*
         * Tear down the inbox listener
         */
        Thing t = getThing();
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }
        MailServerBridgeHandler bridgehandler = (MailServerBridgeHandler) bridge.getHandler();
        MailServerHandler handler = (MailServerHandler) t.getHandler();
        if (bridgehandler == null || bridgehandler.unregisterMailListener(handler)) {
            logger.debug("Failed to tear down listener for mail address {}.", ToAddress);
        }
    }

    /**
     * Method for parsing the received email. Called from SmtpReceivedMessageHandler.java
     *
     * @param data
     */
    public void parseRawMessageBodyData(String data) {

        if (isLinked(CHANNEL_ReceivedMessageCount)) {
            try {
                if (TotalMessageCount > 2000000000) {
                    // Reset the message count in case it ever gets this high so it doens't get close to overflow on a
                    // 32
                    // bit machine
                    TotalMessageCount = 0;
                }
                TotalMessageCount++;
                updateState(CHANNEL_ReceivedMessageCount, new DecimalType(TotalMessageCount));
            } catch (Exception e) {
            }
        }
        if (isLinked(CHANNEL_MessageBody)) {
            try {
                updateState(CHANNEL_MessageBody, new StringType(data));
            } catch (Exception e) {
            }
        }
        if (isLinked(CHANNEL_OpenHabCommand) && pCommand != null) {
            try {
                Matcher m = pCommand.matcher(data);
                // int startIndex = data.indexOf("<openhabcommand>");
                // int endIndex = data.indexOf("</openhabcommand>");
                if (m.find()) {
                    String commandData = m.group(idxCommand); // data.substring(startIndex + 16, endIndex);
                    updateState(CHANNEL_OpenHabCommand, new StringType(commandData));
                }
            } catch (Exception e) {
            }
        }
        if (isLinked(CHANNEL_OpenHabValue) && pValue != null) {
            try {
                Matcher m = pValue.matcher(data);
                // int startIndex = data.indexOf("<openhabvalue>");
                // int endIndex = data.indexOf("</openhabvalue>");
                if (m.find()) {
                    String valueData = m.group(idxValue); // data.substring(startIndex + 14, endIndex);
                    updateState(CHANNEL_OpenHabValue, new StringType(valueData));
                }
            } catch (Exception e) {
            }
        }
    }

}
