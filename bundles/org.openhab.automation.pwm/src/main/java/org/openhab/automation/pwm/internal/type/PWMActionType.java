/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.automation.pwm.internal.type;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pwm.internal.PWMConstants;
import org.openhab.automation.pwm.internal.handler.PWMActionHandler;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.Input;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.library.types.OnOffType;

/**
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class PWMActionType extends ActionType {
    public static PWMActionType initialize() {
        final ConfigDescriptionParameter itemName = ConfigDescriptionParameterBuilder
                .create(PWMConstants.CONFIG_OUTPUT_ITEM, Type.TEXT) //
                .withRequired(true) //
                .withMultiple(false) //
                .withContext("item") //
                .withLabel("Item") //
                .withDescription("Item to send output").build();

        List<ConfigDescriptionParameter> config = List.of(itemName);
        List<Input> inputs = List.of(new Input(PWMConstants.INPUT, OnOffType.class.getName()));

        return new PWMActionType(config, inputs);
    }

    public PWMActionType(List<ConfigDescriptionParameter> configDescriptions, List<Input> inputs) {
        super(PWMActionHandler.MODULE_TYPE_ID, configDescriptions, "calculate PWM output", null, null,
                Visibility.VISIBLE, inputs, null);
    }
}
