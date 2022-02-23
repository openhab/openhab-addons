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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lcd.LcdConfig;
import org.openhab.binding.freeboxos.internal.api.lcd.LcdManager;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
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

    public RevolutionHandler(Thing thing, AudioHTTPServer audioHTTPServer, @Nullable String ipAddress,
            BundleContext bundleContext) {
        super(thing, audioHTTPServer, ipAddress, bundleContext);
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        LcdManager manager = getManager(LcdManager.class);
        LcdConfig config = manager.getConfig();
        switch (channelUID.getIdWithoutGroup()) {
            case LCD_BRIGHTNESS:
                setBrightness(manager, config, command);
                internalPoll();
                return true;
            case LCD_ORIENTATION:
                setOrientation(manager, config, command);
                internalPoll();
                return true;
            case LCD_FORCED:
                setForced(manager, config, command);
                internalPoll();
                return true;
        }
        return super.internalHandleCommand(channelUID, command);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        LcdConfig config = getManager(LcdManager.class).getConfig();
        updateChannelQuantity(DISPLAY, LCD_BRIGHTNESS, config.getBrightness(), PERCENT);
        updateChannelDecimal(DISPLAY, LCD_ORIENTATION, config.getOrientation());
        updateChannelOnOff(DISPLAY, LCD_FORCED, config.isOrientationForced());
    }

    private void setOrientation(LcdManager manager, LcdConfig config, Command command) throws FreeboxException {
        if (command instanceof DecimalType) {
            config.setOrientation(((DecimalType) command).intValue());
            manager.setConfig(config);
        } else {
            logger.warn("Invalid command {} from channel {}", command, LCD_ORIENTATION);
        }
    }

    private void setForced(LcdManager manager, LcdConfig config, Command command) throws FreeboxException {
        if (ON_OFF_CLASSES.contains(command.getClass())) {
            config.setOrientationForced(TRUE_COMMANDS.contains(command));
            manager.setConfig(config);
        } else {
            logger.warn("Invalid command {} from channel {}", command, LCD_FORCED);
        }
    }

    private void setBrightness(LcdManager manager, LcdConfig config, Command command) throws FreeboxException {
        if (command instanceof IncreaseDecreaseType) {
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
        LcdManager manager = getManager(LcdManager.class);
        LcdConfig config = manager.getConfig();
        try {
            int newValue = function.call();
            config.setBrightness(newValue);
            manager.setConfig(config);
        } catch (Exception e) {
            throw new FreeboxException(e, "Error setting brightness");
        }
    }
}
