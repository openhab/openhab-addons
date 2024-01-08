/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.protocol;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.handler.LeapBridgeHandler;
import org.openhab.binding.lutron.internal.protocol.leap.CommandType;
import org.openhab.binding.lutron.internal.protocol.leap.LeapCommand;
import org.openhab.binding.lutron.internal.protocol.leap.Request;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.LutronOperation;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lutron OUTPUT command object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class OutputCommand extends LutronCommandNew {
    // shade, blind, dimmer defs
    public static final Integer ACTION_ZONELEVEL = 1;
    public static final Integer ACTION_STARTRAISING = 2;
    public static final Integer ACTION_STARTLOWERING = 3;
    public static final Integer ACTION_STOP = 4;
    public static final Integer ACTION_POSITION_UPDATE = 32; // For shades/blinds. Undocumented in protocol guide.

    // blind defs
    public static final Integer ACTION_LIFTLEVEL = 1;
    public static final Integer ACTION_TILTLEVEL = 9;
    public static final Integer ACTION_LIFTTILTLEVEL = 10;
    public static final Integer ACTION_STARTRAISINGTILT = 11;
    public static final Integer ACTION_STARTLOWERINGTILT = 12;
    public static final Integer ACTION_STOPTILT = 13;
    public static final Integer ACTION_STARTRAISINGLIFT = 14;
    public static final Integer ACTION_STARTLOWERINGLIFT = 15;
    public static final Integer ACTION_STOPLIFT = 16;

    // cco defs
    public static final Integer ACTION_STATE = 1;
    public static final Integer ACTION_PULSE = 6;

    private final Logger logger = LoggerFactory.getLogger(OutputCommand.class);

    private final Integer action;
    private final @Nullable Number parameter;
    private final @Nullable LutronDuration fadeTime;
    private final @Nullable LutronDuration delayTime;
    private final FanSpeedType fanSpeed;

    /**
     * OutputCommand constructor
     *
     * @param targetType
     * @param operation
     * @param integrationId
     * @param action
     * @param parameter
     * @param fadeTime
     * @param delayTime
     */
    public OutputCommand(TargetType targetType, LutronOperation operation, Integer integrationId, Integer action,
            @Nullable Number parameter, @Nullable LutronDuration fadeTime, @Nullable LutronDuration delayTime) {
        super(targetType, operation, LutronCommandType.OUTPUT, integrationId);
        this.action = action;
        this.parameter = parameter;
        if (parameter != null) {
            this.fanSpeed = FanSpeedType.toFanSpeedType(parameter.intValue());
        } else {
            this.fanSpeed = FanSpeedType.OFF;
        }
        this.fadeTime = fadeTime;
        this.delayTime = delayTime;
    }

    /**
     * OutputCommand constructor for fan commands
     *
     * @param targetType
     * @param operation
     * @param integrationId
     * @param action
     * @param fanSpeed
     * @param fadeTime
     * @param delayTime
     */
    public OutputCommand(TargetType targetType, LutronOperation operation, Integer integrationId, Integer action,
            FanSpeedType fanSpeed, @Nullable LutronDuration fadeTime, @Nullable LutronDuration delayTime) {
        super(targetType, operation, LutronCommandType.OUTPUT, integrationId);
        this.action = action;
        this.fanSpeed = fanSpeed;
        this.parameter = fanSpeed.speed();
        this.fadeTime = fadeTime;
        this.delayTime = delayTime;
    }

    @Override
    public String lipCommand() {
        StringBuilder builder = new StringBuilder().append(operation).append(commandType);
        builder.append(',').append(integrationId);
        builder.append(',').append(action);

        if (parameter != null && targetType == TargetType.CCO && action.equals(OutputCommand.ACTION_PULSE)) {
            builder.append(',').append(String.format(Locale.ROOT, "%.2f", parameter));
        } else if (parameter != null) {
            builder.append(',').append(parameter);
        }

        if (fadeTime != null) {
            builder.append(',').append(fadeTime);
        } else if (fadeTime == null && delayTime != null) {
            // must add 0 placeholder here in order to set delay time
            builder.append(',').append("0");
        }
        if (delayTime != null) {
            builder.append(',').append(delayTime);
        }

        return builder.toString();
    }

    @Override
    public @Nullable LeapCommand leapCommand(LeapBridgeHandler bridgeHandler, @Nullable Integer leapZone) {
        int zone;
        Number parameter = this.parameter;

        if (leapZone == null) {
            return null;
        } else {
            zone = leapZone;
        }

        if (operation == LutronOperation.QUERY) {
            if (action.equals(OutputCommand.ACTION_ZONELEVEL)) {
                return new LeapCommand(Request.getZoneStatus(zone));
            } else {
                logger.debug("Ignoring unsupported query action");
                return null;
            }
        } else if (operation == LutronOperation.EXECUTE) {
            if (targetType == TargetType.SWITCH) {
                if (action.equals(OutputCommand.ACTION_ZONELEVEL) && parameter != null) {
                    return new LeapCommand(Request.goToLevel(zone, parameter.intValue()));
                } else {
                    logger.debug("Ignoring unsupported switch action");
                    return null;
                }
            } else if (targetType == TargetType.DIMMER) {
                if (action.equals(OutputCommand.ACTION_ZONELEVEL) && parameter != null) {
                    if (fadeTime == null && delayTime == null) {
                        return new LeapCommand(Request.goToLevel(zone, parameter.intValue()));
                    } else {
                        LutronDuration fade = (fadeTime == null) ? new LutronDuration(0) : fadeTime;
                        LutronDuration delay = (delayTime == null) ? new LutronDuration(0) : delayTime;
                        return new LeapCommand(Request.goToDimmedLevel(zone, parameter.intValue(), fade.asLeapString(),
                                delay.asLeapString()));
                    }
                } else {
                    logger.debug("Ignoring unsupported dimmer action");
                    return null;
                }
            } else if (targetType == TargetType.FAN) {
                if (action.equals(OutputCommand.ACTION_ZONELEVEL)) {
                    return new LeapCommand(Request.goToFanSpeed(zone, fanSpeed));
                } else {
                    logger.debug("Ignoring unsupported fan action");
                    return null;
                }
            } else if (targetType == TargetType.SHADE) {
                if (action.equals(OutputCommand.ACTION_ZONELEVEL) && parameter != null) {
                    return new LeapCommand(Request.goToLevel(zone, parameter.intValue()));
                } else if (action.equals(OutputCommand.ACTION_STARTRAISING)) {
                    return new LeapCommand(Request.zoneCommand(zone, CommandType.RAISE));
                } else if (action.equals(OutputCommand.ACTION_STARTLOWERING)) {
                    return new LeapCommand(Request.zoneCommand(zone, CommandType.LOWER));
                } else if (action.equals(OutputCommand.ACTION_STOP)) {
                    return new LeapCommand(Request.zoneCommand(zone, CommandType.STOP));
                } else {
                    logger.debug("Ignoring unsupported shade action");
                    return null;
                }
            } else {
                logger.debug("Ignoring unsupported target type: {}", targetType);
                return null;
            }
        } else {
            logger.debug("Ignoring unsupported operation: {}", operation);
            return null;
        }
    }

    @Override
    public String toString() {
        return lipCommand();
    }
}
