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
package org.openhab.binding.yeelight.internal.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.yeelight.internal.YeelightBindingConstants;
import org.openhab.binding.yeelight.internal.lib.device.DeviceStatus;
import org.openhab.binding.yeelight.internal.lib.enums.ActiveMode;

/**
 * The {@link YeelightCeilingWithAmbientHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Viktor Koop - Initial contribution
 */
public class YeelightCeilingWithAmbientHandler extends YeelightCeilingHandler {

    public YeelightCeilingWithAmbientHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        handleCommandHelper(channelUID, command, "Handle ceiling ambient light command");
    }

    @Override
    protected void updateUI(DeviceStatus status) {
        super.updateUI(status);

        if (status.isBackgroundIsPowerOff()) {
            updateState(YeelightBindingConstants.CHANNEL_BACKGROUND_BRIGHTNESS, PercentType.ZERO);
        } else {
            updateState(YeelightBindingConstants.CHANNEL_BACKGROUND_BRIGHTNESS,
                    new PercentType(status.getBackgroundBrightness()));

            final HSBType hsbType = new HSBType(new DecimalType(status.getBackgroundHue()),
                    new PercentType(status.getBackgroundSat()), PercentType.HUNDRED);

            updateState(YeelightBindingConstants.CHANNEL_BACKGROUND_COLOR, hsbType);
        }

        updateState(YeelightBindingConstants.CHANNEL_NIGHTLIGHT,
                (status.getActiveMode() == ActiveMode.MOONLIGHT_MODE) ? OnOffType.ON : OnOffType.OFF);
    }
}
