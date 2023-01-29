/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;

/**
 * A base class for common elements between JSON schema and template schema lights.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
abstract class AbstractRawSchemaLight extends Light {
    protected static final String RAW_CHANNEL_ID = "raw";

    protected ComponentChannel rawChannel;

    public AbstractRawSchemaLight(ComponentFactory.ComponentConfiguration builder) {
        super(builder);
        hiddenChannels.add(rawChannel = buildChannel(RAW_CHANNEL_ID, new TextValue(), "Raw state", this)
                .stateTopic(channelConfiguration.stateTopic).commandTopic(channelConfiguration.commandTopic,
                        channelConfiguration.isRetain(), channelConfiguration.getQos())
                .build(false));
    }

    protected boolean handleCommand(Command command) {
        HSBType newState;
        if (colorValue.getChannelState() instanceof HSBType) {
            newState = (HSBType) colorValue.getChannelState();
        } else {
            newState = HSBType.WHITE;
        }

        if (command.equals(PercentType.ZERO) || command.equals(OnOffType.OFF)) {
            newState = HSBType.BLACK;
        } else if (command.equals(OnOffType.ON)) {
            if (newState.getBrightness().equals(PercentType.ZERO)) {
                newState = new HSBType(newState.getHue(), newState.getSaturation(), PercentType.HUNDRED);
            }
        } else if (command instanceof HSBType) {
            newState = (HSBType) command;
        } else if (command instanceof PercentType) {
            newState = new HSBType(newState.getHue(), newState.getSaturation(), (PercentType) command);
        } else {
            return false;
        }

        publishState(newState);
        return false;
    }

    protected abstract void publishState(HSBType state);
}
