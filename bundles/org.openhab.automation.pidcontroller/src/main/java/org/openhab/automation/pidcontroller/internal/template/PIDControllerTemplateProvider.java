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

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.template.RuleTemplate;
import org.openhab.core.automation.template.RuleTemplateProvider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Hilbrand Bouwkamp - Initial Contribution
 */
@Component
@NonNullByDefault
public class PIDControllerTemplateProvider implements RuleTemplateProvider {
    private static final RuleTemplate PROVIDED_RULE_TEMPLATE = PIDControllerRuleTemplate.initialize();

    @Override
    public @Nullable RuleTemplate getTemplate(String uid, @Nullable Locale locale) {
        return uid.equals(PIDControllerRuleTemplate.UID) ? PROVIDED_RULE_TEMPLATE : null;
    }

    @Override
    public Collection<RuleTemplate> getTemplates(@Nullable Locale locale) {
        return Set.of(PROVIDED_RULE_TEMPLATE);
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {
        // does nothing because this provider does not change
    }

    @Override
    public Collection<RuleTemplate> getAll() {
        return Set.of(PROVIDED_RULE_TEMPLATE);
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {
        // does nothing because this provider does not change
    }
}
