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

package org.openhab.automation.jsscripting.internal.threading;

import java.util.concurrent.locks.Lock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.module.script.rulesupport.shared.ScriptedAutomationManager;
import org.openhab.core.automation.module.script.rulesupport.shared.ScriptedHandler;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleActionHandler;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleConditionHandler;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRule;
import org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleTriggerHandler;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.ConditionType;
import org.openhab.core.automation.type.TriggerType;

/**
 * A replacement for {@link ScriptedAutomationManager} which wraps all rule registrations in a
 * {@link ThreadsafeSimpleRuleDelegate}. This means that all rules registered via this class with be run in serial per
 * instance of this class that they are registered with.
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Florian Hotze - Pass in lock object for multi-thread synchronization; Switch to {@link Lock} for multi-thread
 *         synchronization
 */
@NonNullByDefault
public class ThreadsafeWrappingScriptedAutomationManagerDelegate {

    private ScriptedAutomationManager delegate;
    private final Lock lock;

    public ThreadsafeWrappingScriptedAutomationManagerDelegate(ScriptedAutomationManager delegate, Lock lock) {
        this.delegate = delegate;
        this.lock = lock;
    }

    public void removeModuleType(String UID) {
        delegate.removeModuleType(UID);
    }

    public void removeHandler(String typeUID) {
        delegate.removeHandler(typeUID);
    }

    public void removePrivateHandler(String privId) {
        delegate.removePrivateHandler(privId);
    }

    public void removeAll() {
        delegate.removeAll();
    }

    public Rule addRule(Rule element) {
        // wrap in a threadsafe version, safe per context
        if (element instanceof SimpleRule) {
            element = new ThreadsafeSimpleRuleDelegate(lock, (SimpleRule) element);
        }

        return delegate.addRule(element);
    }

    public void addConditionType(ConditionType condititonType) {
        delegate.addConditionType(condititonType);
    }

    public void addConditionHandler(String uid, ScriptedHandler conditionHandler) {
        delegate.addConditionHandler(uid, conditionHandler);
    }

    public String addPrivateConditionHandler(SimpleConditionHandler conditionHandler) {
        return delegate.addPrivateConditionHandler(conditionHandler);
    }

    public void addActionType(ActionType actionType) {
        delegate.addActionType(actionType);
    }

    public void addActionHandler(String uid, ScriptedHandler actionHandler) {
        delegate.addActionHandler(uid, actionHandler);
    }

    public String addPrivateActionHandler(SimpleActionHandler actionHandler) {
        return delegate.addPrivateActionHandler(actionHandler);
    }

    public void addTriggerType(TriggerType triggerType) {
        delegate.addTriggerType(triggerType);
    }

    public void addTriggerHandler(String uid, ScriptedHandler triggerHandler) {
        delegate.addTriggerHandler(uid, triggerHandler);
    }

    public String addPrivateTriggerHandler(SimpleTriggerHandler triggerHandler) {
        return delegate.addPrivateTriggerHandler(triggerHandler);
    }
}
