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
package org.openhab.io.hueemulation.internal.automation;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.handler.BaseModuleHandlerFactory;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.handler.ModuleHandlerFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory is responsible for rule and http related module types.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@Component(service = ModuleHandlerFactory.class)
public class RulesHandlerFactory extends BaseModuleHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(RulesHandlerFactory.class);

    private static final Collection<String> TYPES = Arrays
            .asList(new String[] { RemoveRuleActionHandler.MODULE_TYPE_ID, HttpActionHandler.MODULE_TYPE_ID });

    @Reference
    protected @NonNullByDefault({}) RuleRegistry ruleRegistry;

    @Reference
    protected @NonNullByDefault({}) HttpClientFactory httpFactory;

    @Override
    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public Collection<String> getTypes() {
        return TYPES;
    }

    @Override
    protected @Nullable ModuleHandler internalCreate(Module module, String ruleUID) {
        logger.trace("create {} -> {}", module.getId(), module.getTypeUID());
        String moduleTypeUID = module.getTypeUID();
        if (RemoveRuleActionHandler.MODULE_TYPE_ID.equals(moduleTypeUID) && module instanceof Action) {
            return new RemoveRuleActionHandler((Action) module, ruleRegistry);
        } else if (HttpActionHandler.MODULE_TYPE_ID.equals(moduleTypeUID) && module instanceof Action) {
            return new HttpActionHandler((Action) module, httpFactory);
        } else {
            logger.error("The module handler type '{}' is not supported.", moduleTypeUID);
        }
        return null;
    }
}
