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
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.protocol.TimeclockCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with the RA2 time clock.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class TimeclockHandler extends LutronHandler {
    private final Logger logger = LoggerFactory.getLogger(TimeclockHandler.class);

    private int integrationId;

    public TimeclockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        return integrationId;
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        logger.debug("Initializing timeclock handler");
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        integrationId = id.intValue();
        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Timeclock {}", getIntegrationId());
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
            queryTimeclock(TimeclockCommand.ACTION_CLOCKMODE);
            // handleUpdate() will set thing status to online when response arrives
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("Handling channel link request for timeclock {}", integrationId);
        if (channelUID.getId().equals(CHANNEL_CLOCKMODE)) {
            queryTimeclock(TimeclockCommand.ACTION_CLOCKMODE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getId();
        logger.debug("Handling timeclock command {} on channel {}", command, channelID);

        if (channelUID.getId().equals(CHANNEL_CLOCKMODE)) {
            if (command instanceof DecimalType decimalCommand) {
                Integer mode = decimalCommand.intValue();
                timeclock(TimeclockCommand.ACTION_CLOCKMODE, mode, null);
            } else if (command instanceof RefreshType) {
                queryTimeclock(TimeclockCommand.ACTION_CLOCKMODE);
            } else {
                logger.debug("Invalid command type for clockmode channnel");
            }
        } else if (channelUID.getId().equals(CHANNEL_EXECEVENT)) {
            if (command instanceof DecimalType decimalCommand) {
                Integer index = decimalCommand.intValue();
                timeclock(TimeclockCommand.ACTION_EXECEVENT, index, null);
            } else {
                logger.debug("Invalid command type for execevent channnel");
            }
        } else if (channelUID.getId().equals(CHANNEL_SUNRISE)) {
            if (command instanceof RefreshType) {
                queryTimeclock(TimeclockCommand.ACTION_SUNRISE);
            } else {
                logger.debug("Invalid command type for sunrise channnel");
            }
        } else if (channelUID.getId().equals(CHANNEL_SUNSET)) {
            if (command instanceof RefreshType) {
                queryTimeclock(TimeclockCommand.ACTION_SUNSET);
            } else {
                logger.debug("Invalid command type for sunset channnel");
            }
        } else if (channelUID.getId().equals(CHANNEL_ENABLEEVENT)) {
            if (command instanceof DecimalType decimalCommand) {
                Integer index = decimalCommand.intValue();
                timeclock(TimeclockCommand.ACTION_SETEVENT, index, true);
            } else {
                logger.debug("Invalid command type for enableevent channnel");
            }
        } else if (channelUID.getId().equals(CHANNEL_DISABLEEVENT)) {
            if (command instanceof DecimalType decimalCommand) {
                Integer index = decimalCommand.intValue();
                timeclock(TimeclockCommand.ACTION_SETEVENT, index, false);
            } else {
                logger.debug("Invalid command type for disableevent channnel");
            }
        } else {
            logger.debug("Command received on invalid channel");
        }
    }

    private @Nullable Calendar parseLutronTime(final String timeString) {
        Integer hour, minute;
        Calendar calendar = Calendar.getInstance();
        try {
            String hh = timeString.split(":", 2)[0];
            String mm = timeString.split(":", 2)[1];
            hour = Integer.parseInt(hh);
            minute = Integer.parseInt(mm);
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            logger.warn("Invaid time format received from timeclock {}", integrationId);
            return null;
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar;
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type != LutronCommandType.TIMECLOCK) {
            return;
        }
        logger.debug("Handling update received from timeclock {}", integrationId);

        try {
            if (parameters.length >= 2 && TimeclockCommand.ACTION_CLOCKMODE.toString().equals(parameters[0])) {
                Integer mode = Integer.valueOf(parameters[1]);
                if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                    updateStatus(ThingStatus.ONLINE);
                }
                updateState(CHANNEL_CLOCKMODE, new DecimalType(mode));

            } else if (parameters.length >= 2 && TimeclockCommand.ACTION_SUNRISE.toString().equals(parameters[0])) {
                Calendar calendar = parseLutronTime(parameters[1]);
                if (calendar != null) {
                    updateState(CHANNEL_SUNRISE,
                            new DateTimeType(ZonedDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault())));
                }

            } else if (parameters.length >= 2 && TimeclockCommand.ACTION_SUNSET.toString().equals(parameters[0])) {
                Calendar calendar = parseLutronTime(parameters[1]);
                if (calendar != null) {
                    updateState(CHANNEL_SUNSET,
                            new DateTimeType(ZonedDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault())));
                }

            } else if (parameters.length >= 2 && TimeclockCommand.ACTION_EXECEVENT.toString().equals(parameters[0])) {
                Integer index = Integer.valueOf(parameters[1]);
                updateState(CHANNEL_EXECEVENT, new DecimalType(index));

            } else if (parameters.length >= 3 && TimeclockCommand.ACTION_SETEVENT.toString().equals(parameters[0])) {
                Integer index = Integer.valueOf(parameters[1]);
                Integer state = Integer.valueOf(parameters[2]);
                if (state.equals(TimeclockCommand.EVENT_ENABLE)) {
                    updateState(CHANNEL_ENABLEEVENT, new DecimalType(index));
                } else if (state.equals(TimeclockCommand.EVENT_DISABLE)) {
                    updateState(CHANNEL_DISABLEEVENT, new DecimalType(index));
                }
            }
        } catch (NumberFormatException e) {
            logger.debug("Encountered number format exception while handling update for timeclock {}", integrationId);
            return;
        }
    }
}
