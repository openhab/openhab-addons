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
package org.openhab.automation.pidcontroller.internal.type;

import static org.openhab.automation.pidcontroller.internal.PIDControllerConstants.*;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pidcontroller.internal.handler.PIDControllerActionHandler;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.Input;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;

/**
 *
 * @author Hilbrand Bouwkamp - Initial Contribution
 */
@NonNullByDefault
public class PIDControllerActionType extends ActionType {
    public static final String INPUT = "input";

    public static PIDControllerActionType initialize() {
        final ConfigDescriptionParameter outputItem = ConfigDescriptionParameterBuilder.create(OUTPUT, Type.TEXT)
                .withRequired(true).withMultiple(false).withContext("item").withLabel("Output Item")
                .withDescription("Item to send output").build();
        final ConfigDescriptionParameter pInspectorItem = ConfigDescriptionParameterBuilder
                .create(P_INSPECTOR, Type.TEXT).withRequired(false).withMultiple(false).withContext("item")
                .withLabel("P Inspector Item").withDescription("Item for debugging the P part").build();
        final ConfigDescriptionParameter iInspectorItem = ConfigDescriptionParameterBuilder
                .create(I_INSPECTOR, Type.TEXT).withRequired(false).withMultiple(false).withContext("item")
                .withLabel("I Inspector Item").withDescription("Item for debugging the I part").build();
        final ConfigDescriptionParameter dInspectorItem = ConfigDescriptionParameterBuilder
                .create(D_INSPECTOR, Type.TEXT).withRequired(false).withMultiple(false).withContext("item")
                .withLabel("D Inspector Item").withDescription("Item for debugging the D part").build();
        final ConfigDescriptionParameter eInspectorItem = ConfigDescriptionParameterBuilder
                .create(E_INSPECTOR, Type.TEXT).withRequired(false).withMultiple(false).withContext("item")
                .withLabel("Error Inspector Item").withDescription("Item for debugging the error value").build();

        List<ConfigDescriptionParameter> config = List.of(outputItem, pInspectorItem, iInspectorItem, dInspectorItem,
                eInspectorItem);

        List<Input> inputs = List.of(createInput(INPUT), createInput(P_INSPECTOR), createInput(I_INSPECTOR),
                createInput(D_INSPECTOR), createInput(E_INSPECTOR));

        return new PIDControllerActionType(config, inputs);
    }

    private static Input createInput(String name) {
        return new Input(name, BigDecimal.class.getName());
    }

    public PIDControllerActionType(List<ConfigDescriptionParameter> configDescriptions, List<Input> inputs) {
        super(PIDControllerActionHandler.MODULE_TYPE_ID, configDescriptions, "calculate PID output", null, null,
                Visibility.VISIBLE, inputs, null);
    }
}
