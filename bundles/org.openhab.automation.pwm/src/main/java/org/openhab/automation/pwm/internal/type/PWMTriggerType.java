/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.automation.pwm.internal.PWMConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pwm.internal.handler.PWMTriggerHandler;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.Output;
import org.openhab.core.automation.type.TriggerType;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.library.types.OnOffType;

/**
 * Creates the configuration for the Trigger module in the rules engine.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class PWMTriggerType extends TriggerType {
    public static final String UID = PWMTriggerHandler.MODULE_TYPE_ID;

    public static PWMTriggerType initialize() {
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<>();
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_DUTY_CYCLE_ITEM, Type.TEXT) //
                .withRequired(true) //
                .withMultiple(false) //
                .withContext("item") //
                .withLabel("Dutycycle Item").withDescription("Item to read the current dutycycle from (PercentType)")
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_PERIOD, Type.DECIMAL) //
                .withRequired(true) //
                .withMultiple(false) //
                .withDefault("600") //
                .withLabel("PWM Interval") //
                .withUnit("s") //
                .withDescription("Duration of the PWM interval in sec.").build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_MIN_DUTYCYCLE, Type.DECIMAL) //
                .withRequired(false) //
                .withMultiple(false) //
                .withMinimum(BigDecimal.ZERO) //
                .withMaximum(BigDecimal.valueOf(100)) //
                .withDefault("0") //
                .withLabel("Min Dutycycle") //
                .withUnit("%") //
                .withDescription("The dutycycle below this value will be increased to this value").build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_EQUATE_MIN_TO_ZERO, Type.BOOLEAN) //
                .withRequired(false) //
                .withMultiple(false) //
                .withDefault("false") //
                .withLabel("Equate Min Dutycycle to 0") //
                .withDescription("True if the dutycycle below Min Dutycycle should be set to 0 (defaults to false)")
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_MAX_DUTYCYCLE, Type.DECIMAL) //
                .withRequired(false) //
                .withMultiple(false) //
                .withMinimum(BigDecimal.ZERO) //
                .withMaximum(BigDecimal.valueOf(100)) //
                .withDefault("100") //
                .withUnit("%") //
                .withLabel("Max Dutycycle") //
                .withDescription("The dutycycle above this value will be increased to 100").build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_EQUATE_MAX_TO_HUNDRED, Type.BOOLEAN) //
                .withRequired(false) //
                .withMultiple(false) //
                .withDefault("true") //
                .withLabel("Equate Max Dutycycle to 100") //
                .withDescription("True if the dutycycle above Max Dutycycle should be set to 100 (defaults to true)")
                .build());
        configDescriptions.add(ConfigDescriptionParameterBuilder.create(CONFIG_DEAD_MAN_SWITCH, Type.DECIMAL) //
                .withRequired(false) //
                .withMultiple(false) //
                .withMinimum(BigDecimal.ZERO) //
                .withDefault("") //
                .withLabel("Dead Man Switch") //
                .withUnit("ms") //
                .withDescription(
                        "If the duty cycle Item is not updated within this time (in ms), the output is switched off")
                .build());

        List<Output> outputs = List.of(new Output(OUTPUT, OnOffType.class.getName(), "Output",
                "Output value of the PWM module", Set.of("command"), null, null));

        return new PWMTriggerType(configDescriptions, outputs);
    }

    public PWMTriggerType(List<ConfigDescriptionParameter> configDescriptions, List<Output> outputs) {
        super(UID, configDescriptions, "PWM triggers", null, null, Visibility.VISIBLE, outputs);
    }
}
