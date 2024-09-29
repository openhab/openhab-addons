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
import org.openhab.core.automation.Module;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseModuleHandlerFactory;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.handler.ModuleHandlerFactory;
import org.openhab.core.scheduler.Scheduler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory is responsible for timer related module types.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@Component(service = ModuleHandlerFactory.class)
public class TimerModuleExHandlerFactory extends BaseModuleHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(TimerModuleExHandlerFactory.class);

    private static final Collection<String> TYPES = Arrays
            .asList(new String[] { AbsoluteDateTimeTriggerHandler.MODULE_TYPE_ID, TimerTriggerHandler.MODULE_TYPE_ID });

    @Reference
    private @NonNullByDefault({}) Scheduler scheduler;

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
        if (AbsoluteDateTimeTriggerHandler.MODULE_TYPE_ID.equals(moduleTypeUID) && module instanceof Trigger) {
            return new AbsoluteDateTimeTriggerHandler((Trigger) module, scheduler);
        } else if (TimerTriggerHandler.MODULE_TYPE_ID.equals(moduleTypeUID) && module instanceof Trigger) {
            return new TimerTriggerHandler((Trigger) module, scheduler);
        } else {
            logger.error("The module handler type '{}' is not supported.", moduleTypeUID);
        }
        return null;
    }
}
