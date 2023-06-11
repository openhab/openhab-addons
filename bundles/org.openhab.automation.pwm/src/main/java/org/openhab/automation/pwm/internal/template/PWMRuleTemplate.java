/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.automation.pwm.internal.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.pwm.internal.PWMConstants;
import org.openhab.automation.pwm.internal.type.PWMTriggerType;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.template.RuleTemplate;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;

/**
 * Rule template for the PWM automation module.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class PWMRuleTemplate extends RuleTemplate {
    public static final String UID = "PWMRuleTemplate";

    public static PWMRuleTemplate initialize() {
        final String triggerId = UUID.randomUUID().toString();

        final List<Trigger> triggers = Collections.singletonList(ModuleBuilder.createTrigger().withId(triggerId)
                .withTypeUID(PWMTriggerType.UID).withLabel("PWM Trigger").build());

        final Map<String, String> actionInputs = new HashMap<String, String>();
        actionInputs.put(PWMConstants.INPUT, triggerId + "." + PWMConstants.OUTPUT);

        Set<String> tags = new HashSet<String>();
        tags.add("PWM");

        return new PWMRuleTemplate(tags, triggers, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList());
    }

    public PWMRuleTemplate(Set<String> tags, List<Trigger> triggers, List<Condition> conditions, List<Action> actions,
            List<ConfigDescriptionParameter> configDescriptions) {
        super(UID, "PWM", "Template for a PWM rule", tags, triggers, conditions, actions, configDescriptions,
                Visibility.VISIBLE);
    }
}
