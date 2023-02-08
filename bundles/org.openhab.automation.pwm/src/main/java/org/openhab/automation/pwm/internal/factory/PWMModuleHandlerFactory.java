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
package org.openhab.automation.pwm.internal.factory;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pwm.internal.handler.PWMTriggerHandler;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseModuleHandlerFactory;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.handler.ModuleHandlerFactory;
import org.openhab.core.items.ItemRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Factory for the PWM automation module.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
@Component(service = ModuleHandlerFactory.class, configurationPid = "automation.pwm")
public class PWMModuleHandlerFactory extends BaseModuleHandlerFactory {
    private static final Collection<String> TYPES = Set.of(PWMTriggerHandler.MODULE_TYPE_ID);
    private ItemRegistry itemRegistry;
    private BundleContext bundleContext;

    @Activate
    public PWMModuleHandlerFactory(@Reference ItemRegistry itemRegistry, BundleContext bundleContext) {
        this.itemRegistry = itemRegistry;
        this.bundleContext = bundleContext;
    }

    @Override
    public Collection<String> getTypes() {
        return TYPES;
    }

    @Override
    protected @Nullable ModuleHandler internalCreate(Module module, String ruleUID) {
        switch (module.getTypeUID()) {
            case PWMTriggerHandler.MODULE_TYPE_ID:
                return new PWMTriggerHandler((Trigger) module, itemRegistry, bundleContext, ruleUID);
        }

        return null;
    }
}
