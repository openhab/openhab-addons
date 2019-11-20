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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants;

/**
 * @author Michael Geramb - Initial contribution
 */
public class Constants {

    public static Map<String, Function<String, HandlerBase>> HandlerFactory = new HashMap<String, Function<String, HandlerBase>>() {
        private static final long serialVersionUID = 1L;
        {
            put(HandlerPowerController.INTERFACE, (s) -> new HandlerPowerController());
            put(HandlerBrightnessController.INTERFACE, (s) -> new HandlerBrightnessController());
            put(HandlerColorController.INTERFACE, (s) -> new HandlerColorController());
            put(HandlerColorTemperatureController.INTERFACE, (s) -> new HandlerColorTemperatureController());
            put(HandlerSecurityPanelController.INTERFACE, (s) -> new HandlerSecurityPanelController());
            put(HandlerAcousticEventSensor.INTERFACE, (s) -> new HandlerAcousticEventSensor());
            put(HandlerTemperatureSensor.INTERFACE, (s) -> new HandlerTemperatureSensor());
            put(HandlerPercentageController.INTERFACE, (s) -> new HandlerPercentageController());
            put(HandlerPowerLevelController.INTERFACE, (s) -> new HandlerPowerLevelController());
        }
    };

    public static final Set<String> SUPPORTED_INTERFACES = HandlerFactory.keySet();

    // channel types

    public static final ChannelTypeUID CHANNEL_TYPE_TEMPERATURE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "temperature");

    // List of Item types
    public static final String ITEM_TYPE_SWITCH = "Switch";
    public static final String ITEM_TYPE_DIMMER = "Dimmer";
    public static final String ITEM_TYPE_STRING = "String";
    public static final String ITEM_TYPE_NUMBER = "Number";
    public static final String ITEM_TYPE_NUMBER_TEMPERATURE = "Number:Temperature";
    public static final String ITEM_TYPE_CONTACT = "Contact";
    public static final String ITEM_TYPE_COLOR = "Color";

}
