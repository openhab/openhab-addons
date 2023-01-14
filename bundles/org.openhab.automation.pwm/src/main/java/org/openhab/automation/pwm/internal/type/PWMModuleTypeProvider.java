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
package org.openhab.automation.pwm.internal.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pwm.internal.handler.PWMTriggerHandler;
import org.openhab.core.automation.type.ModuleType;
import org.openhab.core.automation.type.ModuleTypeProvider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.osgi.service.component.annotations.Component;

/**
 * Provides the module types for the rules engine.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@Component
@NonNullByDefault
public class PWMModuleTypeProvider implements ModuleTypeProvider {
    private static final Map<String, ModuleType> PROVIDED_MODULE_TYPES = Map.of(PWMTriggerHandler.MODULE_TYPE_ID,
            PWMTriggerType.initialize());

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> T getModuleType(@Nullable String UID, @Nullable Locale locale) {
        return (T) PROVIDED_MODULE_TYPES.get(UID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> Collection<T> getModuleTypes(@Nullable Locale locale) {
        return (Collection<T>) PROVIDED_MODULE_TYPES.values();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

    @Override
    public Collection<ModuleType> getAll() {
        return Collections.unmodifiableCollection(PROVIDED_MODULE_TYPES.values());
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }
}
