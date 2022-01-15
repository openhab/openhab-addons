/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.automation.pidcontroller.internal.template;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pidcontroller.internal.handler.PIDControllerTriggerHandler;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.template.RuleTemplate;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;

/**
 *
 * @author Hilbrand Bouwkamp - Initial Contribution
 */
@NonNullByDefault
public class PIDControllerRuleTemplate extends RuleTemplate {
    public static final String UID = "PIDControllerRuleTemplate";

    public static PIDControllerRuleTemplate initialize() {
        final String triggerId = UUID.randomUUID().toString();

        final List<Trigger> triggers = List.of(ModuleBuilder.createTrigger().withId(triggerId)
                .withTypeUID(PIDControllerTriggerHandler.MODULE_TYPE_ID).withLabel("PID Controller Trigger").build());

        return new PIDControllerRuleTemplate(Set.of("PID Controller"), triggers, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
    }

    public PIDControllerRuleTemplate(Set<String> tags, List<Trigger> triggers, List<Condition> conditions,
            List<Action> actions, List<ConfigDescriptionParameter> configDescriptions) {
        super(UID, "PID Controller", "Template for a PID controlled rule", tags, triggers, conditions, actions,
                configDescriptions, Visibility.VISIBLE);
    }
}
