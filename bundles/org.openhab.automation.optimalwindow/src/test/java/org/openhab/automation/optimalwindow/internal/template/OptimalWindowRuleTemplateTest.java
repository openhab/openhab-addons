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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.automation.optimalwindow.internal.OptimalWindowConstants;
import org.openhab.automation.optimalwindow.internal.type.WindowConfigDescriptions;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.template.RuleTemplate;
import org.openhab.core.config.core.ConfigDescriptionParameter;

/**
 * Tests the {@link OptimalWindowRuleTemplate} and the {@link OptimalWindowTemplateProvider}.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class OptimalWindowRuleTemplateTest {
    private static final Pattern REFERENCE = Pattern.compile("\\{\\{(\\w+)}}");

    private final RuleTemplate template = OptimalWindowRuleTemplate.initialize();

    @Test
    void triggerAndAction() {
        assertEquals(1, template.getTriggers().size());
        Trigger trigger = template.getTriggers().get(0);
        assertEquals(OptimalWindowConstants.TRIGGER_TYPE_ID, trigger.getTypeUID());

        assertEquals(1, template.getActions().size());
        Action action = template.getActions().get(0);
        assertEquals("core.ItemCommandAction", action.getTypeUID());
        assertEquals("{{targetItem}}", action.getConfiguration().get("itemName"));
    }

    @Test
    void allWindowParametersArePassedToTheTrigger() {
        Trigger trigger = template.getTriggers().get(0);
        for (ConfigDescriptionParameter parameter : WindowConfigDescriptions.window()) {
            assertEquals("{{" + parameter.getName() + "}}", trigger.getConfiguration().get(parameter.getName()),
                    parameter.getName());
        }
    }

    @Test
    void everyReferenceHasATemplateParameter() {
        Set<String> parameters = template.getConfigurationDescriptions().stream()
                .map(ConfigDescriptionParameter::getName).collect(Collectors.toSet());

        Stream.concat(template.getTriggers().stream(), template.getActions().stream()).map(Module::getConfiguration)
                .flatMap(config -> config.getProperties().values().stream()).map(Object::toString).forEach(value -> {
                    Matcher matcher = REFERENCE.matcher(value);
                    while (matcher.find()) {
                        assertTrue(parameters.contains(matcher.group(1)), "missing parameter " + matcher.group(1));
                    }
                });
    }

    @Test
    void provider() {
        OptimalWindowTemplateProvider provider = new OptimalWindowTemplateProvider();
        assertNotNull(provider.getTemplate(OptimalWindowRuleTemplate.UID, null));
        assertNull(provider.getTemplate("other", null));
        assertEquals(1, provider.getTemplates(null).size());
        assertEquals(1, provider.getAll().size());
    }
}
