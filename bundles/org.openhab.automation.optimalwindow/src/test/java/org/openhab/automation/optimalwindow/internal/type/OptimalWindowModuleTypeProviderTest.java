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
package org.openhab.automation.optimalwindow.internal.type;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.automation.optimalwindow.internal.OptimalWindowConstants.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.automation.type.ConditionType;
import org.openhab.core.automation.type.ModuleType;
import org.openhab.core.automation.type.Output;
import org.openhab.core.automation.type.TriggerType;
import org.openhab.core.config.core.ConfigDescriptionParameter;

/**
 * Tests the module types of the Optimal Window automation.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class OptimalWindowModuleTypeProviderTest {
    private final OptimalWindowModuleTypeProvider provider = new OptimalWindowModuleTypeProvider();

    private static Set<String> names(List<ConfigDescriptionParameter> parameters) {
        return parameters.stream().map(ConfigDescriptionParameter::getName).collect(Collectors.toSet());
    }

    @Test
    void providesTriggerAndCondition() {
        assertEquals(2, provider.getAll().size());
        assertEquals(2, provider.getModuleTypes(null).size());
        assertInstanceOf(TriggerType.class, provider.getModuleType(TRIGGER_TYPE_ID, null));
        assertInstanceOf(ConditionType.class, provider.getModuleType(CONDITION_TYPE_ID, null));
        assertNull(provider.getModuleType("other", null));
    }

    @Test
    void triggerParameters() {
        ModuleType trigger = provider.getModuleType(TRIGGER_TYPE_ID, null);
        assertNotNull(trigger);
        assertEquals(Set.of(CONFIG_FORECAST_ITEM, CONFIG_PERSISTENCE_SERVICE, CONFIG_RANGE_START, CONFIG_RANGE_DURATION,
                CONFIG_LENGTH, CONFIG_CONSECUTIVE, CONFIG_GOAL, CONFIG_PREFER_START, CONFIG_ACTIVE_ITEM,
                CONFIG_START_ITEM, CONFIG_END_ITEM, CONFIG_COUNTDOWN_ITEM, CONFIG_REMAINING_ITEM,
                CONFIG_WINDOW_TEXT_ITEM), names(trigger.getConfigurationDescriptions()));
    }

    @Test
    void conditionHasNoStatusItems() {
        ModuleType condition = provider.getModuleType(CONDITION_TYPE_ID, null);
        assertNotNull(condition);
        assertEquals(names(WindowConfigDescriptions.window()), names(condition.getConfigurationDescriptions()));
    }

    @Test
    void commandOutputIsTaggedForItemCommandAction() {
        TriggerType trigger = provider.getModuleType(TRIGGER_TYPE_ID, null);
        assertNotNull(trigger);
        Output command = trigger.getOutputs().stream().filter(o -> OUTPUT_COMMAND.equals(o.getName())).findFirst()
                .orElseThrow();
        // the tag connects the output automatically to the command input of "send a command"
        assertEquals(Set.of("command"), command.getTags());
        assertEquals(Set.of(OUTPUT_EVENT, OUTPUT_COMMAND, OUTPUT_START, OUTPUT_END, OUTPUT_AVERAGE),
                trigger.getOutputs().stream().map(Output::getName).collect(Collectors.toSet()));
    }
}
