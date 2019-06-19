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
package org.openhab.binding.mqtt.generic;

import static org.openhab.binding.mqtt.homie.generic.internal.MqttBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * Static test definitions, like thing, bridge and channel definitions
 *
 * @author David Graeff - Initial contribution
 */
public class ThingChannelConstants {
    // Common ThingUID and ChannelUIDs
    public final static ThingUID testHomieThing = new ThingUID(HOMIE300_MQTT_THING, "device123");
}
