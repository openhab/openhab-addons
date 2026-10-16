/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Set;

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
 * @author Fabian Wolter - Add inspector Items for debugging
 */
@NonNullByDefault
public class PIDControllerTriggerType extends TriggerType {
    private static final String DEFAULT_LOOPTIME_MS = "1000";
    private static final String ITEM = "item";

    public static PIDControllerTriggerType initialize() {
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<>();
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_INPUT_ITEM, Type.TEXT) //
                .withRequired(true) //
                .withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("Input Item") //
                .withDescription("Item to monitor") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_SETPOINT_ITEM, Type.TEXT) //
                .withRequired(true) //
                .withMultiple(false) //
                .withContext(ITEM) //
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
                .withDescription("Slows the rate of change of the D-part (T1) in seconds.") //
                .withUnit("s") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_LOOP_TIME, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withDefault(DEFAULT_LOOPTIME_MS) //
                .withLabel("Loop Time") //
                .withDescription("The interval the output value is updated in ms") //
                .withUnit("ms") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_I_MIN, Type.DECIMAL) //
                .withRequired(false) //
                .withMultiple(false) //
                .withLabel("I-part Lower Limit") //
                .withDescription("The I-part will be min this value. Can be left empty for no limit.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_I_MAX, Type.DECIMAL) //
                .withRequired(false) //
                .withMultiple(false) //
                .withLabel("I-part Upper Limit") //
                .withDescription("The I-part will be max this value. Can be left empty for no limit.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_I_DECAY_TIME, Type.DECIMAL) //
                .withRequired(false) //
                .withMultiple(false) //
                .withMinimum(BigDecimal.ZERO) //
                .withDefault("0") //
                .withLabel("I-part Decay Time") //
                .withDescription("Time constant in seconds for fading out the I-part while the deviation from the "
                        + "setpoint is no longer growing. After one decay time the I-part has fallen to about 37% of "
                        + "its value, after three decay times to about 5%. Use this if the I-part stays at its limit "
                        + "long after the demand has gone, which happens when the process settles slightly off the "
                        + "setpoint and the error never changes sign, so the I-part is never unwound. While the "
                        + "deviation is still growing the I-part accumulates normally and is not faded out. "
                        + "0 (the default) disables the fade-out.") //
                .withUnit("s") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_I_HOLD_ITEM, Type.TEXT) //
                .withRequired(false) //
                .withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("I-part Hold Item") //
                .withDescription("Switch or Contact Item that suspends the I-part while the actuator cannot act on "
                        + "the process, for example a mixing damper whose supply is on the wrong side of the room "
                        + "temperature. While the Item is ON (or CLOSED for a Contact) the I-part keeps its value but "
                        + "stops growing, so it does not wind up during a period the controller has no influence "
                        + "over. Leave empty to always integrate.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_I_HOLD_DIRECTIONAL, Type.BOOLEAN) //
                .withRequired(false) //
                .withDefault("false") //
                .withLabel("Directional I-part Hold") //
                .withDescription("Suspend only the accumulation that takes the I-part further from zero while the "
                        + "hold Item is active, and let a step that brings it back through. Use this when the hold "
                        + "reports a lasting plant condition rather than a brief one: a symmetric hold also blocks "
                        + "the recovery step, so the I-part stays at the value it reached even once the process "
                        + "starts moving the right way again.") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(P_INSPECTOR, Type.TEXT) //
                .withRequired(false) //
                .withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("P Inspector Item") //
                .withDescription("Item for debugging the P part") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(I_INSPECTOR, Type.TEXT) //
                .withRequired(false) //
                .withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("I Inspector Item") //
                .withDescription("Item for debugging the I part") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(D_INSPECTOR, Type.TEXT) //
                .withRequired(false).withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("D Inspector Item") //
                .withDescription("Item for debugging the D part") //
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(E_INSPECTOR, Type.TEXT) //
                .withRequired(false).withMultiple(false) //
                .withContext(ITEM) //
                .withLabel("Error Inspector Item") //
                .withDescription("Item for debugging the error value") //
                .build());

        Output output = new Output(COMMAND, BigDecimal.class.getName(), "Output", "Output value of the PID Controller",
                Set.of("command"), null, null);

        return new PIDControllerTriggerType(configDescriptions, List.of(output));
    }

    public PIDControllerTriggerType(List<ConfigDescriptionParameter> configDescriptions, List<Output> outputs) {
        super(PIDControllerTriggerHandler.MODULE_TYPE_ID, configDescriptions, "PID controller triggers", null, null,
                Visibility.VISIBLE, outputs);
    }
}
