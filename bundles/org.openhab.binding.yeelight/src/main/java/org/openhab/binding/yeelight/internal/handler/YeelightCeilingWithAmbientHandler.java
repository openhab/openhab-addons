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
package org.openhab.binding.yeelight.internal.handler;

import org.openhab.binding.yeelight.internal.YeelightBindingConstants;
import org.openhab.binding.yeelight.internal.lib.device.DeviceStatus;
import org.openhab.binding.yeelight.internal.lib.enums.ActiveMode;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

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
            updateState(YeelightBindingConstants.CHANNEL_BACKGROUND_COLOR, PercentType.ZERO);
        } else {
            final HSBType hsbType = new HSBType(new DecimalType(status.getBackgroundHue()),
                    new PercentType(status.getBackgroundSat()), new PercentType(status.getBackgroundBrightness()));

            updateState(YeelightBindingConstants.CHANNEL_BACKGROUND_COLOR, hsbType);
        }

        updateState(YeelightBindingConstants.CHANNEL_NIGHTLIGHT,
                OnOffType.from((status.getActiveMode() == ActiveMode.MOONLIGHT_MODE)));
    }
}
