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
package org.openhab.automation.optimalwindow.internal.template;

import static org.openhab.automation.optimalwindow.internal.OptimalWindowConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.optimalwindow.internal.type.WindowConfigDescriptions;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.template.RuleTemplate;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.Configuration;

/**
 * Rule template that switches an item ON when the optimal window starts and OFF when it ends.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 * @author Thomas Leber - Adapted for the Optimal Window automation
 */
@NonNullByDefault
public class OptimalWindowRuleTemplate extends RuleTemplate {
    public static final String UID = "OptimalWindowRuleTemplate";
    private static final String CONFIG_TARGET_ITEM = "targetItem";

    public static OptimalWindowRuleTemplate initialize() {
        List<ConfigDescriptionParameter> parameters = new ArrayList<>();
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_TARGET_ITEM, Type.TEXT) //
                .withRequired(true) //
                .withContext("item") //
                .withLabel("Target Item") //
                .withDescription("Switch item that is turned ON when the window starts and OFF when it ends.") //
                .build());
        parameters.addAll(WindowConfigDescriptions.window());

        // pass all window parameters of the template to the trigger
        Map<String, Object> triggerConfig = new HashMap<>();
        for (ConfigDescriptionParameter parameter : WindowConfigDescriptions.window()) {
            triggerConfig.put(parameter.getName(), "{{" + parameter.getName() + "}}");
        }

        List<Trigger> triggers = List.of(ModuleBuilder.createTrigger().withId("1").withTypeUID(TRIGGER_TYPE_ID)
                .withLabel("Optimal window starts or ends").withConfiguration(new Configuration(triggerConfig))
                .build());
        List<Action> actions = List.of(ModuleBuilder.createAction().withId("2").withTypeUID("core.ItemCommandAction")
                .withLabel("Switch the target item")
                .withConfiguration(new Configuration(Map.of("itemName", "{{" + CONFIG_TARGET_ITEM + "}}"))).build());

        return new OptimalWindowRuleTemplate(Set.of("Optimal Window"), triggers, List.of(), actions, parameters);
    }

    public OptimalWindowRuleTemplate(Set<String> tags, List<Trigger> triggers, List<Condition> conditions,
            List<Action> actions, List<ConfigDescriptionParameter> configDescriptions) {
        super(UID, "Run during optimal window",
                "Switches an item ON during the window with the lowest or highest forecast values, e.g. the cheapest "
                        + "electricity prices.",
                tags, triggers, conditions, actions, configDescriptions, Visibility.VISIBLE);
    }
}
