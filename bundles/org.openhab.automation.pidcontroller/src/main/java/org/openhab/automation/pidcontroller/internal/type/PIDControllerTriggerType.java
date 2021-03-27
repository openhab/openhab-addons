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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pidcontroller.internal.handler.PIDControllerTriggerHandler;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.Output;
import org.openhab.core.automation.type.TriggerType;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;

/**
 *
 * @author Hilbrand Bouwkamp - Initial Contribution
 */
@NonNullByDefault
public class PIDControllerTriggerType extends TriggerType {
    private static final String DEFAULT_LOOPTIME_MS = "1000";

    public static PIDControllerTriggerType initialize() {
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<>();
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_INPUT_ITEM, Type.TEXT) //
                .withRequired(true) //
                .withMultiple(false) //
                .withContext("item") //
                .withLabel("Input Item") //
                .withDescription("Item to monitor") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_SETPOINT_ITEM, Type.TEXT) //
                .withRequired(true) //
                .withMultiple(false) //
                .withContext("item") //
                .withLabel("Setpoint") //
                .withDescription("Targeted setpoint") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_KP_GAIN, Type.DECIMAL).withRequired(true) //
                .withMultiple(false) //
                .withDefault("1.0") //
                .withMinimum(BigDecimal.ZERO) //
                .withLabel("Proportional Gain (Kp)") //
                .withDescription("Change to output propertional to current error value.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_KI_GAIN, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withDefault("1.0") //
                .withMinimum(BigDecimal.ZERO) //
                .withLabel("Integral Gain (Ki)") //
                .withDescription("Accelerate movement towards the setpoint.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_KD_GAIN, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withDefault("1.0") //
                .withMinimum(BigDecimal.ZERO) //
                .withLabel("Derivative Gain (Kd)") //
                .withDescription("Slows the rate of change of the output.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_KD_TIMECONSTANT, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withMinimum(BigDecimal.ZERO) //
                .withDefault("1.0") //
                .withLabel("Derivative Time Constant") //
                .withDescription("Slows the rate of change of the D part (T1) in seconds.") //
                .withUnit("s") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_COMMAND_ITEM, Type.TEXT) //
                .withRequired(false) //
                .withMultiple(false) //
                .withContext("item") //
                .withLabel("Command Item") //
                .withDescription("You can send String commands to this Item like \"RESET\".") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_LOOP_TIME, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withDefault(DEFAULT_LOOPTIME_MS) //
                .withLabel("Loop Time") //
                .withDescription("The interval the output value is updated in ms") //
                .withUnit("ms") //
                .build());
        Output output = new Output(OUTPUT, BigDecimal.class.getName(), "Output", "Output value of the PID Controller",
                null, null, null);
        Output pInspector = new Output(P_INSPECTOR, BigDecimal.class.getName(), "P Inspector",
                "Current P value of the pid controller", null, null, null);
        Output iInspector = new Output(I_INSPECTOR, BigDecimal.class.getName(), "I Inspector",
                "Current I value of the pid controller", null, null, null);
        Output dInspector = new Output(D_INSPECTOR, BigDecimal.class.getName(), "D Inspector",
                "Current D value of the pid controller", null, null, null);
        Output eInspector = new Output(E_INSPECTOR, BigDecimal.class.getName(), "Error Value Inspector",
                "Current error value of the pid controller", null, null, null);

        List<Output> outputs = List.of(output, pInspector, iInspector, dInspector, eInspector);

        return new PIDControllerTriggerType(configDescriptions, outputs);
    }

    public PIDControllerTriggerType(List<ConfigDescriptionParameter> configDescriptions, List<Output> outputs) {
        super(PIDControllerTriggerHandler.MODULE_TYPE_ID, configDescriptions, "PID controller triggers", null, null,
                Visibility.VISIBLE, outputs);
    }
}
