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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;
import static org.openhab.core.library.unit.SmartHomeUnits.PERCENT;

import java.time.ZoneId;
import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lcd.LcdConfig;
import org.openhab.binding.freeboxos.internal.api.lcd.LcdManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RevolutionHandler} is responsible for handling take care of
 * revolution server specifics
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - updated to a bridge handler and delegate few things to another handler
 */
@NonNullByDefault
public class RevolutionHandler extends ServerHandler {
    private final Logger logger = LoggerFactory.getLogger(RevolutionHandler.class);
    private @NonNullByDefault({}) LcdManager lcdManager;

    public RevolutionHandler(Thing thing, ZoneId zoneId) {
        super(thing, zoneId);
    }

    @Override
    public void initialize() {
        super.initialize();
        lcdManager = bridgeHandler.getLcdManager();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getIdWithoutGroup()) {
                case LCD_BRIGHTNESS:
                    setBrightness(command);
                    break;
                case LCD_ORIENTATION:
                    setOrientation(command);
                    break;
                case LCD_FORCED:
                    setForced(command);
                    break;
                default:
                    super.handleCommand(channelUID, command);
            }
            internalPoll();
        } catch (FreeboxException e) {
            logger.debug("Error while handling command {} from channel {}", command, channelUID.getId(), e);
        }
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        LcdConfig config = lcdManager.getConfig();
        updateChannelQuantity(DISPLAY, LCD_BRIGHTNESS, config.getBrightness(), PERCENT);
        updateChannelDecimal(DISPLAY, LCD_ORIENTATION, config.getOrientation());
        updateChannelOnOff(DISPLAY, LCD_FORCED, config.isOrientationForced());
    }

    private void setOrientation(Command command) throws FreeboxException {
        if (command instanceof DecimalType) {
            LcdConfig config = lcdManager.getConfig();
            config.setOrientation(((DecimalType) command).intValue());
            lcdManager.setConfig(config);
        } else {
            logger.debug("Invalid command {} from channel {}", command, LCD_ORIENTATION);
        }
    }

    private void setForced(Command command) throws FreeboxException {
        if (ON_OFF_CLASSES.contains(command.getClass())) {
            LcdConfig config = lcdManager.getConfig();
            config.setOrientationForced(TRUE_COMMANDS.contains(command));
            lcdManager.setConfig(config);
        } else {
            logger.debug("Invalid command {} from channel {}", command, LCD_FORCED);
        }
    }

    private void setBrightness(Command command) throws FreeboxException {
        if (command instanceof IncreaseDecreaseType) {
            LcdConfig config = lcdManager.getConfig();
            changeLcdBrightness(() -> config.getBrightness() + (command == IncreaseDecreaseType.INCREASE ? 1 : -1));
        } else if (command instanceof OnOffType) {
            changeLcdBrightness(() -> command == OnOffType.ON ? 100 : 0);
        } else if (command instanceof DecimalType || command instanceof PercentType) {
            changeLcdBrightness(() -> ((DecimalType) command).intValue());
        } else {
            logger.debug("Invalid command {} from channel {}", command, LCD_BRIGHTNESS);
        }
    }

    private void changeLcdBrightness(Callable<Integer> function) throws FreeboxException {
        LcdConfig config = lcdManager.getConfig();
        try {
            int newValue = function.call();
            config.setBrightness(newValue);
            lcdManager.setConfig(config);
        } catch (Exception e) {
            throw new FreeboxException("Error setting brightness", e);
        }
    }
}
