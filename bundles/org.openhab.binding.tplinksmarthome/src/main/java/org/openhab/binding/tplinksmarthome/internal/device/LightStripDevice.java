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
package org.openhab.binding.tplinksmarthome.internal.device;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;

/**
 * TP-Link Smart Home Light Strip.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class LightStripDevice extends BulbDevice {

    public LightStripDevice(final TPLinkSmartHomeThingType type) {
        super(type);
    }

    @Override
    protected @Nullable HasErrorResponse handleOnOffType(final String channelID, final OnOffType onOff,
            final int transitionPeriod) throws IOException {
        return commands.setLightStripStateResponse(
                connection.sendCommand(commands.setLightStripState(onOff, transitionPeriod)));
    }

    @Override
    protected @Nullable HasErrorResponse handleBrightness(final int brightness, final int transitionPeriod)
            throws IOException {
        return commands.setLightStripStateResponse(
                connection.sendCommand(commands.setLightStripBrightness(brightness, transitionPeriod)));
    }

    @Override
    protected @Nullable HasErrorResponse handleColorTemperature(final int colorTemperature, final int transitionPeriod)
            throws IOException {
        return commands.setLightStripStateResponse(
                connection.sendCommand(commands.setLightStripColorTemperature(colorTemperature, transitionPeriod)));
    }

    @Override
    protected @Nullable HasErrorResponse handleHSBType(final String channelID, final HSBType command,
            final int transitionPeriod) throws IOException {
        return commands.setLightStripStateResponse(
                connection.sendCommand(commands.setLightStripColor(command, transitionPeriod)));
    }
}
