/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.action.lgwebos.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.openhab.binding.lgwebos.LGWebOS;
import org.openhab.action.lgwebos.handler.AppActionHandler;
import org.openhab.action.lgwebos.handler.BrowserActionHandler;
import org.openhab.action.lgwebos.handler.ButtonActionHandler;
import org.openhab.action.lgwebos.handler.TextActionHandler;
import org.openhab.action.lgwebos.handler.ToastActionHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Sebastian Prehn - Initial contribution
 */
@Component(service = ModuleHandlerFactory.class)
public class LGWebOSModuleHandlerFactory extends BaseModuleHandlerFactory {
    private LGWebOS api;
    private Map<String, BiFunction<Action, LGWebOS, ModuleHandler>> moduleHandlers = new HashMap<>();
    {
        moduleHandlers.put(ToastActionHandler.TYPE_ID, (action, api) -> new ToastActionHandler(action, api));
        moduleHandlers.put(ButtonActionHandler.TYPE_ID, (action, api) -> new ButtonActionHandler(action, api));
        moduleHandlers.put(TextActionHandler.TYPE_ID, (action, api) -> new TextActionHandler(action, api));
        moduleHandlers.put(BrowserActionHandler.TYPE_ID, (action, api) -> new BrowserActionHandler(action, api));
        moduleHandlers.put(AppActionHandler.TYPE_ID, (action, api) -> new AppActionHandler(action, api));
    }

    @Override
    public Collection<String> getTypes() {
        return moduleHandlers.keySet().stream().collect(Collectors.toList());
    }

    @Override
    protected ModuleHandler internalCreate(Module module, String ruleUID) {
        if (module instanceof Action) {
            return moduleHandlers.getOrDefault(module.getTypeUID(), (m, a) -> null).apply((Action) module, api);
        }
        return null;
    }

    @Reference
    protected void setLGWebOS(LGWebOS api) {
        this.api = api;
    }

    protected void unsetLGWebOS(LGWebOS api) {
        this.api = null;
    }
}
