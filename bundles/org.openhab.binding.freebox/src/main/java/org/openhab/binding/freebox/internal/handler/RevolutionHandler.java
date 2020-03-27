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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
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
 */
@NonNullByDefault
public class RevolutionHandler extends ServerHandler {

    private final Logger logger = LoggerFactory.getLogger(RevolutionHandler.class);

    public RevolutionHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getIdWithoutGroup()) {
                case LCD_BRIGHTNESS:
                    setBrightness(command);
                case LCD_ORIENTATION:
                    setOrientation(command);
                case LCD_FORCED:
                    setForced(command);
                default:
                    super.handleCommand(channelUID, command);
            }
        } catch (FreeboxException e) {
            logCommandException(e, channelUID, command);
            try {
                internalPoll();
            } catch (FreeboxException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    public void logCommandException(FreeboxException e, ChannelUID channelUID, Command command) {
        if (e.isMissingRights()) {
            logger.debug("Thing {}: missing right {} while handling command {} from channel {}", getThing().getUID(),
                    e.getResponse().getMissingRight(), command, channelUID.getId());
        } else {
            logger.debug("Thing {}: error while handling command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId(), e);
        }
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        logger.debug("Polling server state...");

        LcdConfig config = getLcdConfig();
        updateChannelQuantity(DISPLAY, LCD_BRIGHTNESS, config.getBrightness(), PERCENT);
        updateState(new ChannelUID(getThing().getUID(), DISPLAY, LCD_ORIENTATION),
                new DecimalType(config.getOrientation()));
        updateState(new ChannelUID(getThing().getUID(), DISPLAY, LCD_FORCED),
                OnOffType.from(config.isOrientationForced()));

    }

    private void setBrightness(Command command) throws FreeboxException {
        if (command instanceof IncreaseDecreaseType) {
            updateChannelQuantity(DISPLAY, LCD_BRIGHTNESS, shiftLcdBrightness((IncreaseDecreaseType) command), PERCENT);
        } else if (command instanceof OnOffType) {
            updateChannelQuantity(DISPLAY, LCD_BRIGHTNESS, setLcdBrightness(command == OnOffType.ON ? 100 : 0),
                    PERCENT);
        } else if (command instanceof DecimalType || command instanceof PercentType) {
            updateChannelQuantity(DISPLAY, LCD_BRIGHTNESS, setLcdBrightness(((DecimalType) command).intValue()),
                    PERCENT);
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command, LCD_BRIGHTNESS);
        }
    }

    private void setOrientation(Command command) throws FreeboxException {
        if (command instanceof DecimalType) {
            LcdConfig config = setLcdOrientation(((DecimalType) command).intValue());
            updateState(new ChannelUID(getThing().getUID(), DISPLAY, LCD_ORIENTATION),
                    new DecimalType(config.getOrientation()));
            updateState(new ChannelUID(getThing().getUID(), DISPLAY, LCD_FORCED),
                    OnOffType.from(config.isOrientationForced()));
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command, LCD_ORIENTATION);
        }
    }

    private void setForced(Command command) throws FreeboxException {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            updateState(new ChannelUID(getThing().getUID(), DISPLAY, LCD_FORCED),
                    OnOffType.from(setLcdOrientationForced(command.equals(OnOffType.ON))
                            || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)));

        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command, LCD_FORCED);
        }
    }

    public LcdConfig setLcdOrientation(int orientation) throws FreeboxException {
        LcdConfig config = getLcdConfig();
        int newValue = Math.min(360, orientation);
        newValue = Math.max(newValue, 0);
        config.setOrientation(newValue);
        config.setOrientationForced(true);
        LcdConfig result = apiManager.execute(config, null);
        // FreeboxLcdConfig result = bridgeHandler.executePut(FreeboxLcdConfigResponse.class, config);
        return result;
    }

    public LcdConfig getLcdConfig() throws FreeboxException {
        LcdConfig result = apiManager.executeGet(LcdConfigResponse.class, null);
        return result;
    }

    public boolean setLcdOrientationForced(boolean forced) throws FreeboxException {
        LcdConfig config = getLcdConfig();
        config.setOrientationForced(forced);
        LcdConfig result = apiManager.execute(config, null);
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
        LcdConfig result = apiManager.execute(config, null);
        return result.getBrightness();
        // return bridgeHandler.executePut(FreeboxLcdConfigResponse.class, config).getBrightness();
    }

    public int setLcdBrightness(int brightness) throws FreeboxException {
        LcdConfig config = getLcdConfig();
        int newValue = Math.min(100, brightness);
        newValue = Math.max(newValue, 0);
        config.setBrightness(newValue);
        LcdConfig result = apiManager.execute(config, null);
        return result.getBrightness();
        // return bridgeHandler.executePut(FreeboxLcdConfigResponse.class, config).getBrightness();
    }

}
