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
package org.openhab.automation.jsscripting.internal.threading;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRule;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRuleActionHandler;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.Configuration;

/**
 * A version of {@link SimpleRule} which controls multithreaded execution access to this specific rule. This is useful
 * for rules which wrap GraalJS Contexts, which are not multithreaded.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
class ThreadsafeSimpleRuleDelegate implements Rule, SimpleRuleActionHandler {

    private final Lock lock;
    private final SimpleRule delegate;

    /**
     * Constructor requires a lock object and delegate to forward invocations to.
     *
     * @param lock rule executions will synchronize on this object
     * @param delegate the delegate to forward invocations to
     */
    ThreadsafeSimpleRuleDelegate(Lock lock, SimpleRule delegate) {
        this.lock = lock;
        this.delegate = delegate;
    }

    @Override
    @NonNullByDefault({})
    public Object execute(Action module, Map<String, ?> inputs) {
        lock.lock();
        try {
            return delegate.execute(module, inputs);
        } finally { // Make sure that Lock is unlocked regardless of an exception is thrown or not to avoid deadlocks
            lock.unlock();
        }
    }

    @Override
    public String getUID() {
        return delegate.getUID();
    }

    @Override
    @Nullable
    public String getTemplateUID() {
        return delegate.getTemplateUID();
    }

    public void setTemplateUID(@Nullable String templateUID) {
        delegate.setTemplateUID(templateUID);
    }

    @Override
    @Nullable
    public String getName() {
        return delegate.getName();
    }

    public void setName(@Nullable String ruleName) {
        delegate.setName(ruleName);
    }

    @Override
    public Set<String> getTags() {
        return delegate.getTags();
    }

    public void setTags(@Nullable Set<String> ruleTags) {
        delegate.setTags(ruleTags);
    }

    @Override
    @Nullable
    public String getDescription() {
        return delegate.getDescription();
    }

    public void setDescription(@Nullable String ruleDescription) {
        delegate.setDescription(ruleDescription);
    }

    @Override
    public Visibility getVisibility() {
        return delegate.getVisibility();
    }

    public void setVisibility(@Nullable Visibility visibility) {
        delegate.setVisibility(visibility);
    }

    @Override
    public Configuration getConfiguration() {
        return delegate.getConfiguration();
    }

    public void setConfiguration(@Nullable Configuration ruleConfiguration) {
        delegate.setConfiguration(ruleConfiguration);
    }

    @Override
    public List<ConfigDescriptionParameter> getConfigurationDescriptions() {
        return delegate.getConfigurationDescriptions();
    }

    public void setConfigurationDescriptions(@Nullable List<ConfigDescriptionParameter> configDescriptions) {
        delegate.setConfigurationDescriptions(configDescriptions);
    }

    @Override
    public List<Condition> getConditions() {
        return delegate.getConditions();
    }

    public void setConditions(@Nullable List<Condition> conditions) {
        delegate.setConditions(conditions);
    }

    @Override
    public List<Action> getActions() {
        return delegate.getActions();
    }

    @Override
    public List<Trigger> getTriggers() {
        return delegate.getTriggers();
    }

    public void setActions(@Nullable List<Action> actions) {
        delegate.setActions(actions);
    }

    public void setTriggers(@Nullable List<Trigger> triggers) {
        delegate.setTriggers(triggers);
    }

    @Override
    public List<Module> getModules() {
        return delegate.getModules();
    }

    public <T extends Module> List<T> getModules(@Nullable Class<T> moduleClazz) {
        return delegate.getModules(moduleClazz);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return delegate.equals(obj);
    }

    @Override
    @Nullable
    public Module getModule(String moduleId) {
        return delegate.getModule(moduleId);
    }
}
