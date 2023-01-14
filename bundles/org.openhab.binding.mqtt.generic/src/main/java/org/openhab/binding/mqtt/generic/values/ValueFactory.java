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
package org.openhab.binding.mqtt.generic.values;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.ChannelConfig;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.core.types.util.UnitUtils;

/**
 * A factory t
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ValueFactory {

    /**
     * Creates a new channel state value.
     *
     * @param config The channel configuration
     * @param channelTypeID The channel type, for instance TEXT_CHANNEL.
     */
    public static Value createValueState(ChannelConfig config, String channelTypeID) throws IllegalArgumentException {
        Value value;
        switch (channelTypeID) {
            case MqttBindingConstants.STRING:
                value = config.allowedStates.isBlank() ? new TextValue()
                        : new TextValue(config.allowedStates.split(","));
                break;
            case MqttBindingConstants.DATETIME:
                value = new DateTimeValue();
                break;
            case MqttBindingConstants.IMAGE:
                value = new ImageValue();
                break;
            case MqttBindingConstants.LOCATION:
                value = new LocationValue();
                break;
            case MqttBindingConstants.NUMBER:
                value = new NumberValue(config.min, config.max, config.step, UnitUtils.parseUnit(config.unit));
                break;
            case MqttBindingConstants.DIMMER:
                value = new PercentageValue(config.min, config.max, config.step, config.on, config.off);
                break;
            case MqttBindingConstants.COLOR_HSB:
                value = new ColorValue(ColorMode.HSB, config.on, config.off, config.onBrightness);
                break;
            case MqttBindingConstants.COLOR_RGB:
                value = new ColorValue(ColorMode.RGB, config.on, config.off, config.onBrightness);
                break;
            case MqttBindingConstants.COLOR:
                ColorMode colorMode;
                try {
                    colorMode = ColorMode.valueOf(config.colorMode);
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Invalid color mode: " + config.colorMode, exception);
                }
                value = new ColorValue(colorMode, config.on, config.off, config.onBrightness);
                break;
            case MqttBindingConstants.SWITCH:
                value = new OnOffValue(config.on, config.off);
                break;
            case MqttBindingConstants.CONTACT:
                value = new OpenCloseValue(config.on, config.off);
                break;
            case MqttBindingConstants.ROLLERSHUTTER:
                value = new RollershutterValue(config.on, config.off, config.stop);
                break;
            case MqttBindingConstants.TRIGGER:
                config.trigger = true;
                value = new TextValue();
                break;
            default:
                throw new IllegalArgumentException("ChannelTypeUID not recognised: " + channelTypeID);
        }
        return value;
    }
}
