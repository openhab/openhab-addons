/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.ChannelConfig;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;

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
                value = StringUtils.isBlank(config.allowedStates) ? new TextValue()
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
                value = new NumberValue(config.min, config.max, config.step);
                break;
            case MqttBindingConstants.DIMMER:
                value = new PercentageValue(config.min, config.max, config.step, config.on, config.off);
                break;
            case MqttBindingConstants.COLOR_RGB:
                value = new ColorValue(true, config.on, config.off, config.onBrightness);
                break;
            case MqttBindingConstants.COLOR_HSB:
                value = new ColorValue(false, config.on, config.off, config.onBrightness);
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
            default:
                throw new IllegalArgumentException("ChannelTypeUID not recognised: " + channelTypeID);
        }
        return value;
    }

}
