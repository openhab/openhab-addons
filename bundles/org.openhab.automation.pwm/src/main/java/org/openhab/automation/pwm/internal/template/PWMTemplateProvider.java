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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.template.RuleTemplate;
import org.openhab.core.automation.template.RuleTemplateProvider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.osgi.service.component.annotations.Component;

/**
 * Rule template provider for the PWM automation module.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@Component
@NonNullByDefault
public class PWMTemplateProvider implements RuleTemplateProvider {
    private final Map<String, RuleTemplate> providedRuleTemplates = new HashMap<String, RuleTemplate>();

    public PWMTemplateProvider() {
        providedRuleTemplates.put(PWMRuleTemplate.UID, PWMRuleTemplate.initialize());
    }

    @Override
    @Nullable
    public RuleTemplate getTemplate(String UID, @Nullable Locale locale) {
        return providedRuleTemplates.get(UID);
    }

    @Override
    public Collection<RuleTemplate> getTemplates(@Nullable Locale locale) {
        return providedRuleTemplates.values();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {
        // does nothing because this provider does not change
    }

    @Override
    public Collection<RuleTemplate> getAll() {
        return Collections.unmodifiableCollection(providedRuleTemplates.values());
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {
        // does nothing because this provider does not change
    }
}
