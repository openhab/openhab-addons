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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lcd.LcdConfig;
import org.openhab.binding.freeboxos.internal.api.lcd.LcdManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
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
 */
@NonNullByDefault
public class RevolutionHandler extends ServerHandler {
    private final Logger logger = LoggerFactory.getLogger(RevolutionHandler.class);
    private @Nullable LcdManager lcdManager;

    public RevolutionHandler(Thing thing) {
        super(thing);
    }

    private LcdManager getLcdManager() throws FreeboxException {
        LcdManager manager = lcdManager;
        if (manager == null) {
            manager = getManager(LcdManager.class);
            lcdManager = manager;
        }
        return manager;
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        switch (channelUID.getIdWithoutGroup()) {
            case LCD_BRIGHTNESS:
                setBrightness(command);
                internalPoll();
                return true;
            case LCD_ORIENTATION:
                setOrientation(command);
                internalPoll();
                return true;
            case LCD_FORCED:
                setForced(command);
                internalPoll();
                return true;
        }
        return super.internalHandleCommand(channelUID, command);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        LcdConfig config = getLcdManager().getConfig();
        updateChannelQuantity(DISPLAY, LCD_BRIGHTNESS, config.getBrightness(), PERCENT);
        updateChannelDecimal(DISPLAY, LCD_ORIENTATION, config.getOrientation());
        updateChannelOnOff(DISPLAY, LCD_FORCED, config.isOrientationForced());
    }

    private void setOrientation(Command command) throws FreeboxException {
        if (command instanceof DecimalType) {
            LcdConfig config = getLcdManager().getConfig();
            config.setOrientation(((DecimalType) command).intValue());
            getLcdManager().setConfig(config);
        } else {
            logger.warn("Invalid command {} from channel {}", command, LCD_ORIENTATION);
        }
    }

    private void setForced(Command command) throws FreeboxException {
        if (ON_OFF_CLASSES.contains(command.getClass())) {
            LcdConfig config = getLcdManager().getConfig();
            config.setOrientationForced(TRUE_COMMANDS.contains(command));
            getLcdManager().setConfig(config);
        } else {
            logger.warn("Invalid command {} from channel {}", command, LCD_FORCED);
        }
    }

    private void setBrightness(Command command) throws FreeboxException {
        if (command instanceof IncreaseDecreaseType) {
            LcdConfig config = getLcdManager().getConfig();
            changeLcdBrightness(() -> config.getBrightness() + (command == IncreaseDecreaseType.INCREASE ? 1 : -1));
        } else if (command instanceof OnOffType) {
            changeLcdBrightness(() -> command == OnOffType.ON ? 100 : 0);
        } else if (command instanceof QuantityType) {
            changeLcdBrightness(() -> ((QuantityType<?>) command).intValue());
        } else if (command instanceof DecimalType || command instanceof PercentType) {
            changeLcdBrightness(() -> ((DecimalType) command).intValue());
        } else {
            logger.warn("Invalid command {} from channel {}", command, LCD_BRIGHTNESS);
        }
    }

    private void changeLcdBrightness(Callable<Integer> function) throws FreeboxException {
        LcdConfig config = getLcdManager().getConfig();
        try {
            int newValue = function.call();
            config.setBrightness(newValue);
            getLcdManager().setConfig(config);
        } catch (Exception e) {
            throw new FreeboxException("Error setting brightness", e);
        }
    }
}
