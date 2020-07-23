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
import org.openhab.binding.freebox.internal.api.APIRequests;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.LcdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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

    public RevolutionHandler(Bridge bridge, Gson gson) {
        super(bridge, gson);
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
            logger.debug("Thing {}: error while handling command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId(), e);
            try {
                internalPoll();
            } catch (FreeboxException e1) {
                logger.debug("Thing {}: error while handling command {} from channel {}", getThing().getUID(), command,
                        channelUID.getId(), e);
            }
        }
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        logger.debug("Polling server state...");

        LcdConfig config = apiManager.execute(new APIRequests.GetLcdConfig());
        updateChannelQuantity(DISPLAY, LCD_BRIGHTNESS, config.getBrightness(), PERCENT);
        updateChannelDecimal(DISPLAY, LCD_ORIENTATION, config.getOrientation());
        updateChannelOnOff(DISPLAY, LCD_FORCED, config.isOrientationForced());
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
            updateChannelDecimal(DISPLAY, LCD_ORIENTATION, config.getOrientation());
            updateChannelOnOff(DISPLAY, LCD_FORCED, config.isOrientationForced());
        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command, LCD_ORIENTATION);
        }
    }

    private void setForced(Command command) throws FreeboxException {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            updateChannelOnOff(DISPLAY, LCD_FORCED, setLcdOrientationForced(command.equals(OnOffType.ON))
                    || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN));

        } else {
            logger.debug("Thing {}: invalid command {} from channel {}", getThing().getUID(), command, LCD_FORCED);
        }
    }

    public LcdConfig setLcdOrientation(int orientation) throws FreeboxException {
        LcdConfig config = apiManager.execute(new APIRequests.GetLcdConfig());
        config.setOrientation(orientation);
        return apiManager.execute(new APIRequests.SetLcdConfig(config));
    }

    public boolean setLcdOrientationForced(boolean forced) throws FreeboxException {
        LcdConfig config = apiManager.execute(new APIRequests.GetLcdConfig());
        config.setOrientationForced(forced);
        config = apiManager.execute(new APIRequests.SetLcdConfig(config));
        return config.isOrientationForced();
    }

    public int shiftLcdBrightness(IncreaseDecreaseType direction) throws FreeboxException {
        LcdConfig config = apiManager.execute(new APIRequests.GetLcdConfig());
        config.setBrightness(config.getBrightness() + (direction == IncreaseDecreaseType.INCREASE ? 1 : -1));
        config = apiManager.execute(new APIRequests.SetLcdConfig(config));
        return config.getBrightness();
    }

    public int setLcdBrightness(int brightness) throws FreeboxException {
        LcdConfig config = apiManager.execute(new APIRequests.GetLcdConfig());
        config.setBrightness(brightness);
        config = apiManager.execute(new APIRequests.SetLcdConfig(config));
        return config.getBrightness();
    }

}
