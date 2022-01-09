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
package org.openhab.binding.rme.internal.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.rme.internal.RMEBindingConstants.DataField;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RMEThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class RMEThingHandler extends SerialThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RMEThingHandler.class);

    private static final StringType AUTOMATIC = new StringType("Automatic");
    private static final StringType CITY = new StringType("City");
    private static final StringType MANUAL = new StringType("Manual");
    private static final StringType RAIN = new StringType("Rain");

    public RMEThingHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing, serialPortManager);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RME handler.");

        if (getConfig().get(BAUD_RATE) == null) {
            baud = 2400;
        } else {
            baud = (int) getConfig().get(BAUD_RATE);
        }

        if (getConfig().get(BUFFER_SIZE) == null) {
            bufferSize = 1024;
        } else {
            bufferSize = (int) getConfig().get(BUFFER_SIZE);
        }

        port = (String) getConfig().get(PORT);

        sleep = 250;

        interval = 5000;

        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("The RME Rain Manager is a read-only device and can not handle commands");
    }

    @Override
    public void onDataReceived(String receivedLine) {
        String line = StringUtils.chomp(receivedLine);

        // little hack to overcome Locale limits of the RME Rain Manager
        // note to the attentive reader : should we add support for system
        // locale's in the Type classes? ;-)
        line = line.replace(",", ".");
        line = line.trim();

        Pattern responsePattern = Pattern.compile("(.*);(0|1);(0|1);(0|1);(0|1);(0|1);(0|1);(0|1);(0|1);(0|1)");

        try {
            logger.trace("Processing '{}'", line);

            Matcher matcher = responsePattern.matcher(line);
            if (matcher.matches()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    switch (DataField.get(i)) {
                        case LEVEL: {
                            DecimalType decimalType = new DecimalType(matcher.group(i));
                            updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()), decimalType);
                            break;
                        }
                        case MODE: {
                            StringType stringType = null;
                            if (matcher.group(i).equals("0")) {
                                stringType = MANUAL;
                            } else if (matcher.group(i).equals("1")) {
                                stringType = AUTOMATIC;
                            }
                            if (stringType != null) {
                                updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()),
                                        stringType);
                            }
                            break;
                        }
                        case SOURCE: {
                            StringType stringType = null;
                            if (matcher.group(i).equals("0")) {
                                stringType = RAIN;
                            } else if (matcher.group(i).equals("1")) {
                                stringType = CITY;
                            }
                            if (stringType != null) {
                                updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()),
                                        stringType);
                            }
                            break;
                        }
                        default:
                            if (matcher.group(i).equals("0")) {
                                updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()),
                                        OnOffType.OFF);
                            } else if (matcher.group(i).equals("1")) {
                                updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()),
                                        OnOffType.ON);
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while receiving data : '{}'", e.getMessage(), e);
        }
    }
}
