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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants;

/**
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class Constants {
    public static final Map<String, @Nullable Supplier<HandlerBase>> HANDLER_FACTORY = new HashMap<>();

    static {
        HANDLER_FACTORY.put(HandlerPowerController.INTERFACE, HandlerPowerController::new);
        HANDLER_FACTORY.put(HandlerBrightnessController.INTERFACE, HandlerBrightnessController::new);
        HANDLER_FACTORY.put(HandlerColorController.INTERFACE, HandlerColorController::new);
        HANDLER_FACTORY.put(HandlerColorTemperatureController.INTERFACE, HandlerColorTemperatureController::new);
        HANDLER_FACTORY.put(HandlerSecurityPanelController.INTERFACE, HandlerSecurityPanelController::new);
        HANDLER_FACTORY.put(HandlerAcousticEventSensor.INTERFACE, HandlerAcousticEventSensor::new);
        HANDLER_FACTORY.put(HandlerTemperatureSensor.INTERFACE, HandlerTemperatureSensor::new);
        HANDLER_FACTORY.put(HandlerPercentageController.INTERFACE, HandlerPercentageController::new);
        HANDLER_FACTORY.put(HandlerPowerLevelController.INTERFACE, HandlerPowerLevelController::new);
    }

    public static final Set<String> SUPPORTED_INTERFACES = HANDLER_FACTORY.keySet();

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
