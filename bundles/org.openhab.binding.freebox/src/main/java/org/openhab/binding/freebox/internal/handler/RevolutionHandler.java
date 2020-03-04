/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.handler;

import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.PERCENT;
import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.LcdConfig;
import org.openhab.binding.freebox.internal.api.model.LcdConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RevolutionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - updated to a bridge handler and delegate few things to another handler
 * @author Laurent Garnier - update discovery configuration
 * @author Laurent Garnier - use new internal API manager
 */
public class RevolutionHandler extends ServerHandler {

    private final Logger logger = LoggerFactory.getLogger(RevolutionHandler.class);

    public RevolutionHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected boolean internalHandleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        try {
            switch (channelUID.getIdWithoutGroup()) {
                case LCDBRIGHTNESS:
                    setBrightness(command);
                    return true;
                case LCDORIENTATION:
                    setOrientation(command);
                    return true;
                case LCDFORCED:
                    setForced(command);
                    return true;
                default:
                    return super.internalHandleCommand(channelUID, command);
            }
        } catch (FreeboxException e) {
            logCommandException(e, channelUID, command);
            internalPoll();
        }
        return false;
    }

    @Override
    protected void internalPoll() {
        super.internalPoll();
        logger.debug("Polling server state...");

        try {
            LcdConfig config = getLcdConfig();
            updateChannelQuantityType(DISPLAY, LCDBRIGHTNESS, new QuantityType<>(config.getBrightness(), PERCENT));
            updateChannelDecimalState(DISPLAY, LCDORIENTATION, config.getOrientation());
            updateChannelSwitchState(DISPLAY, LCDFORCED, config.isOrientationForced());
        } catch (FreeboxException e) {
            logger.debug("Phone state job failed: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void setBrightness(Command command) throws FreeboxException {
        if (command instanceof IncreaseDecreaseType) {
            updateChannelQuantityType(DISPLAY, LCDBRIGHTNESS,
                    new QuantityType<>(shiftLcdBrightness((IncreaseDecreaseType) command), PERCENT));
        } else if (command instanceof OnOffType) {
            updateChannelQuantityType(DISPLAY, LCDBRIGHTNESS,
                    new QuantityType<>(setLcdBrightness(command == OnOffType.ON ? 100 : 0), PERCENT));
        } else if (command instanceof DecimalType || command instanceof PercentType) {
            updateChannelQuantityType(DISPLAY, LCDBRIGHTNESS,
                    new QuantityType<>(setLcdBrightness(((DecimalType) command).intValue()), PERCENT));
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command, LCDBRIGHTNESS);
        }
    }

    private void setOrientation(Command command) throws FreeboxException {
        if (command instanceof DecimalType) {
            LcdConfig config = setLcdOrientation(((DecimalType) command).intValue());
            updateChannelDecimalState(DISPLAY, LCDORIENTATION, config.getOrientation());
            updateChannelSwitchState(DISPLAY, LCDFORCED, config.isOrientationForced());
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command, LCDORIENTATION);
        }
    }

    private void setForced(Command command) throws FreeboxException {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            updateChannelSwitchState(DISPLAY, LCDFORCED, setLcdOrientationForced(command.equals(OnOffType.ON)
                    || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)));

        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command, LCDFORCED);
        }
    }

    public LcdConfig setLcdOrientation(int orientation) throws FreeboxException {
        LcdConfig config = getLcdConfig();
        int newValue = Math.min(360, orientation);
        newValue = Math.max(newValue, 0);
        config.setOrientation(newValue);
        config.setOrientationForced(true);
        LcdConfig result = bridgeHandler.execute(config, null);
        // FreeboxLcdConfig result = bridgeHandler.executePut(FreeboxLcdConfigResponse.class, config);
        return result;
    }

    public LcdConfig getLcdConfig() throws FreeboxException {
        LcdConfig result = bridgeHandler.executeGet(LcdConfigResponse.class, null);
        return result;
    }

    public boolean setLcdOrientationForced(boolean forced) throws FreeboxException {
        LcdConfig config = getLcdConfig();
        config.setOrientationForced(forced);
        LcdConfig result = bridgeHandler.execute(config, null);
        return result.isOrientationForced();
        // return bridgeHandler.executePut(FreeboxLcdConfigResponse.class, config).isOrientationForced();
    }

    public int shiftLcdBrightness(IncreaseDecreaseType direction) throws FreeboxException {
        LcdConfig config = getLcdConfig();
        if (direction == IncreaseDecreaseType.INCREASE) {
            config.setBrightness(Math.min(100, config.getBrightness() + 1));
        } else {
            config.setBrightness(Math.max(0, config.getBrightness() - 1));
        }
        LcdConfig result = bridgeHandler.execute(config, null);
        return result.getBrightness();
        // return bridgeHandler.executePut(FreeboxLcdConfigResponse.class, config).getBrightness();
    }

    public int setLcdBrightness(int brightness) throws FreeboxException {
        LcdConfig config = getLcdConfig();
        int newValue = Math.min(100, brightness);
        newValue = Math.max(newValue, 0);
        config.setBrightness(newValue);
        LcdConfig result = bridgeHandler.execute(config, null);
        return result.getBrightness();
        // return bridgeHandler.executePut(FreeboxLcdConfigResponse.class, config).getBrightness();
    }

}
