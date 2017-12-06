/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.rule.lgwebos.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.openhab.binding.lgwebos.LGWebOS;
import org.openhab.binding.lgwebos.LGWebOSBindingConstants;
import org.openhab.rule.lgwebos.handler.AppActionHandler;
import org.openhab.rule.lgwebos.handler.BrowserActionHandler;
import org.openhab.rule.lgwebos.handler.ButtonActionHandler;
import org.openhab.rule.lgwebos.handler.TextActionHandler;
import org.openhab.rule.lgwebos.handler.ToastActionHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class dynamically provides the LGWebOS action type.
 * This is necessary since there is no other way to provide dynamic config param options for module types.
 *
 * @author Sebastian Prehn - initial contribution
 *
 */
@Component(immediate = true)
public class LGWebOSActionTypeProvider implements ModuleTypeProvider {

    private ThingRegistry thingRegistry;
    private Map<String, Function<Locale, ModuleType>> moduleTypes = new HashMap<>();
    {
        moduleTypes.put(ToastActionHandler.TYPE_ID, locale -> getToastActionType(locale));
        moduleTypes.put(ButtonActionHandler.TYPE_ID, locale -> getButtonActionType(locale));
        moduleTypes.put(TextActionHandler.TYPE_ID, locale -> getTextActionType(locale));
        moduleTypes.put(BrowserActionHandler.TYPE_ID, locale -> getBrowserActionType(locale));
        moduleTypes.put(AppActionHandler.TYPE_ID, locale -> getApplicationActionType(locale));
    }

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    @Override
    public Collection<ModuleType> getAll() {
        return getModuleTypes(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ModuleType getModuleType(String UID, Locale locale) {
        return moduleTypes.getOrDefault(UID, l -> null).apply(locale);
    }

    @Override
    public Collection<ModuleType> getModuleTypes(Locale locale) {
        return moduleTypes.values().stream().map(function -> function.apply(locale)).collect(Collectors.toList());
    }

    private ModuleType getToastActionType(Locale locale) {
        ConfigDescriptionParameter param1 = ConfigDescriptionParameterBuilder
                .create(ToastActionHandler.PARAM_THING_ID, Type.TEXT).withRequired(true).withLabel("Thing")
                .withDescription("the WebOS device").withOptions(getDeviceOptions()).withLimitToOptions(true).build();
        ConfigDescriptionParameter param2 = ConfigDescriptionParameterBuilder
                .create(ToastActionHandler.PARAM_MESSAGE, Type.TEXT).withRequired(true).withLabel("Message")
                .withDescription("message to display").build();

        List<ConfigDescriptionParameter> params = Stream.of(param1, param2).collect(Collectors.toList());

        return new ActionType(ToastActionHandler.TYPE_ID, params, "send a toast message",
                "Displays a message on screen.", null, Visibility.VISIBLE, new ArrayList<>(), new ArrayList<>());
    }

    private ModuleType getButtonActionType(Locale locale) {
        ConfigDescriptionParameter param1 = ConfigDescriptionParameterBuilder
                .create(ButtonActionHandler.PARAM_THING_ID, Type.TEXT).withRequired(true).withLabel("Thing")
                .withDescription("the WebOS device").withOptions(getDeviceOptions()).withLimitToOptions(true).build();
        ConfigDescriptionParameter param2 = ConfigDescriptionParameterBuilder
                .create(ButtonActionHandler.PARAM_BUTTON, Type.TEXT).withRequired(true).withLabel("Button")
                .withDescription("button to send").withOptions(getButtonOptions()).withLimitToOptions(true).build();
        List<ConfigDescriptionParameter> params = Stream.of(param1, param2).collect(Collectors.toList());

        return new ActionType(ButtonActionHandler.TYPE_ID, params, "send a button press",
                "Sends a button press to the TV.", null, Visibility.VISIBLE, new ArrayList<>(), new ArrayList<>());
    }

    private ModuleType getTextActionType(Locale locale) {
        ConfigDescriptionParameter param1 = ConfigDescriptionParameterBuilder
                .create(TextActionHandler.PARAM_THING_ID, Type.TEXT).withRequired(true).withLabel("Thing")
                .withDescription("the WebOS device").withOptions(getDeviceOptions()).withLimitToOptions(true).build();
        ConfigDescriptionParameter param2 = ConfigDescriptionParameterBuilder
                .create(TextActionHandler.PARAM_TEXT, Type.TEXT).withRequired(true).withLabel("Text")
                .withDescription("text to send").build();
        List<ConfigDescriptionParameter> params = Stream.of(param1, param2).collect(Collectors.toList());

        return new ActionType(TextActionHandler.TYPE_ID, params, "send text input", "Sends text input to the TV.", null,
                Visibility.VISIBLE, new ArrayList<>(), new ArrayList<>());
    }

    private ModuleType getBrowserActionType(Locale locale) {
        ConfigDescriptionParameter param1 = ConfigDescriptionParameterBuilder
                .create(BrowserActionHandler.PARAM_THING_ID, Type.TEXT).withRequired(true).withLabel("Thing")
                .withDescription("the WebOS device").withOptions(getDeviceOptions()).withLimitToOptions(true).build();
        ConfigDescriptionParameter param2 = ConfigDescriptionParameterBuilder
                .create(BrowserActionHandler.PARAM_URL, Type.TEXT).withRequired(true).withLabel("URL")
                .withDescription("url to open").build();
        List<ConfigDescriptionParameter> params = Stream.of(param1, param2).collect(Collectors.toList());

        return new ActionType(BrowserActionHandler.TYPE_ID, params, "open url", "Opens a URL on the TV.", null,
                Visibility.VISIBLE, new ArrayList<>(), new ArrayList<>());
    }

    private ModuleType getApplicationActionType(Locale locale) {
        ConfigDescriptionParameter param1 = ConfigDescriptionParameterBuilder
                .create(AppActionHandler.PARAM_THING_ID, Type.TEXT).withRequired(true).withLabel("Thing")
                .withDescription("the WebOS device").withOptions(getDeviceOptions()).withLimitToOptions(true).build();
        ConfigDescriptionParameter param2 = ConfigDescriptionParameterBuilder
                .create(AppActionHandler.PARAM_APP_ID, Type.TEXT).withRequired(true).withLabel("Application ID")
                .withDescription("application to open").build();
        List<ConfigDescriptionParameter> params = Stream.of(param1, param2).collect(Collectors.toList());

        return new ActionType(ButtonActionHandler.TYPE_ID, params, "launch application",
                "Launches an application on the TV.", null, Visibility.VISIBLE, new ArrayList<>(), new ArrayList<>());
    }

    private List<ParameterOption> getButtonOptions() {
        return Stream.of(LGWebOS.Button.values()).map(button -> new ParameterOption(button.name(), button.name()))
                .collect(Collectors.toList());
    }

    private List<ParameterOption> getDeviceOptions() {
        return thingRegistry.stream()
                .filter(thing -> LGWebOSBindingConstants.THING_TYPE_WEBOSTV.equals(thing.getThingTypeUID()))
                .map(thing -> new ParameterOption(thing.getUID().getId(), thing.getLabel()))
                .collect(Collectors.toList());
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

}
