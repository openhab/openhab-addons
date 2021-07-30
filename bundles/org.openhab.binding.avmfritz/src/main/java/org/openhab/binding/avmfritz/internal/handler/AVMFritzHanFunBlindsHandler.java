/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.CHANNEL_ROLLERSHUTTER;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.avmfritz.internal.config.AVMFritzHanFunBlindsConfiguration;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.DeviceModel;
import org.openhab.binding.avmfritz.internal.dto.LevelcontrolModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetBlindTargetCallback.BlindCommand;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a HAN-FUN blind. Handles commands, which are sent to one of the channels.
 *
 * @author Ulrich Mertin - Initial contribution
 */
@NonNullByDefault
public class AVMFritzHanFunBlindsHandler extends DeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzHanFunBlindsHandler.class);

    private @Nullable Boolean invertLevel;

    public AVMFritzHanFunBlindsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        final AVMFritzHanFunBlindsConfiguration config = getConfigAs(AVMFritzHanFunBlindsConfiguration.class);
        this.invertLevel = config.invertLevel;
        super.initialize();
    }

    @Override
    public void onDeviceUpdated(ThingUID thingUID, AVMFritzBaseModel device) {
        if (thing.getUID().equals(thingUID)) {
            DeviceModel deviceModel = (DeviceModel) device;
            super.onDeviceUpdated(thingUID, deviceModel);
            logger.debug("Update blind '{}' with device model: {}", thingUID, device);
            LevelcontrolModel levelcontrol = deviceModel.getLevelcontrol();
            if (invertLevel != null && invertLevel.booleanValue()) {
                BigDecimal oldLevelPercentage = levelcontrol.getLevelPercentage();
                BigDecimal newLevelPercentage = BigDecimal.valueOf(100L).subtract(oldLevelPercentage);
                logger.debug("Inverting level of blind {}: {} -> {}", thingUID, oldLevelPercentage, newLevelPercentage);
                levelcontrol.setLevelPercentage(newLevelPercentage);
            }
            updateLevelcontrol(levelcontrol);
        }
    }

    protected void updateLevelcontrol(@Nullable LevelcontrolModel levelcontrolModel) {
        if (levelcontrolModel != null) {
            updateThingChannelState(CHANNEL_ROLLERSHUTTER, new PercentType(levelcontrolModel.getLevelPercentage()));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for blind channel {}", command, channelId);
        if (command == RefreshType.REFRESH) {
            handleRefreshCommand();
            return;
        }
        if (command != RefreshType.REFRESH) {
            FritzAhaWebInterface fritzBox = getWebInterface();
            if (fritzBox == null) {
                logger.debug("Cannot handle command '{}' because connection is missing", command);
                return;
            }
            String ain = getIdentifier();
            if (ain == null) {
                logger.debug("Cannot handle command '{}' because AIN is missing", command);
                return;
            }
            if (channelId.equals(CHANNEL_ROLLERSHUTTER)) {
                if (command instanceof StopMoveType) {
                    StopMoveType rollershutterCommand = (StopMoveType) command;
                    if (StopMoveType.STOP.equals(rollershutterCommand)) {
                        fritzBox.setBlind(ain, BlindCommand.STOP);
                    } else {
                        logger.debug("Received unknown rollershutter StopMove command MOVE");
                    }
                } else if (command instanceof UpDownType) {
                    UpDownType rollershutterCommand = (UpDownType) command;
                    if (UpDownType.UP.equals(rollershutterCommand)) {
                        fritzBox.setBlind(ain, BlindCommand.OPEN);
                    } else {
                        fritzBox.setBlind(ain, BlindCommand.CLOSE);
                    }
                } else if (command instanceof PercentType) {
                    PercentType rollershutterCommand = (PercentType) command;
                    BigDecimal levelpercentage = rollershutterCommand.toBigDecimal();
                    if (invertLevel != null && invertLevel.booleanValue()) {
                        BigDecimal newLevelPercentage = BigDecimal.valueOf(100L).subtract(levelpercentage);
                        logger.debug("Inverting level of blind channel {}: {} -> {}", channelUID, levelpercentage,
                                newLevelPercentage);
                        fritzBox.setLevelpercentage(ain, newLevelPercentage);
                    } else {
                        fritzBox.setLevelpercentage(ain, levelpercentage);
                    }
                } else {
                    logger.debug("Received unknown rollershutter command type '{}'", command.toString());
                }
            } else {
                logger.debug("Received unknown channel {}", channelId);
            }
        }
    }
}
