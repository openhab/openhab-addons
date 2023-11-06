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
package org.openhab.io.hueemulation.internal.automation;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.handler.ActionHandler;
import org.openhab.core.automation.handler.BaseModuleHandler;
import org.openhab.core.config.core.Configuration;

/**
 * This action module type allows to remove a rule from the rule registry.
 * <p>
 * This is very useful for rules that should execute only once etc.
 * 
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class RemoveRuleActionHandler extends BaseModuleHandler<Action> implements ActionHandler {
    public static final String MODULE_TYPE_ID = "rules.RemoveRuleAction";
    public static final String CALLBACK_CONTEXT_NAME = "CALLBACK";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    public static final String CFG_REMOVE_UID = "removeuid";
    private final String ruleUID;

    private RuleRegistry ruleRegistry;

    @SuppressWarnings({ "null", "unused" })
    public RemoveRuleActionHandler(final Action module, RuleRegistry ruleRegistry) {
        super(module);
        this.ruleRegistry = ruleRegistry;
        final Configuration config = module.getConfiguration();
        if (config.getProperties().isEmpty()) {
            throw new IllegalArgumentException("'Configuration' can not be empty.");
        }

        ruleUID = (String) config.get(CFG_REMOVE_UID);
        if (ruleUID == null) {
            throw new IllegalArgumentException("'ruleUIDs' property must not be null.");
        }
    }

    @Override
    public @Nullable Map<String, Object> execute(Map<String, Object> context) {
        ruleRegistry.remove(ruleUID);
        return null;
    }
}
