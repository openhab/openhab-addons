/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hp1000.internal;

import static org.openhab.binding.hp1000.HP1000BindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.openhab.binding.hp1000.handler.HP1000Handler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The {@link HP1000HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniel Bauer - Initial contribution
 */
@Component(service = { ThingHandlerFactory.class,
        HP1000HandlerFactory.class }, immediate = true, configurationPid = "binding.hp1000")
public class HP1000HandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(THING_TYPE_WEATHER_STATION);
    private ChannelTypeRegistry channelTypeRegistry;
    private List<HP1000Handler> handlerList = new ArrayList<>();

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_WEATHER_STATION)) {
            HP1000Handler handler = new HP1000Handler(channelTypeRegistry, thing);
            handlerList.add(handler);
            return handler;
        }

        return null;

    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        handlerList.removeAll(handlerList.stream()
                .filter(handler -> handler.getThing().getUID().equals(thingHandler.getThing().getUID()))
                .collect(Collectors.toList()));
        super.removeHandler(thingHandler);
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    protected void unsetChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = null;
    }

    public void webHookEvent(String host, Map<String, String[]> paramterMap) {
        handlerList.stream().filter(handler -> {
            Object hostConfig = handler.getThing().getConfiguration().get(CONFIG_PARAMETER_HOST_NAME);
            if (hostConfig == null) {
                return false;
            }
            return hostConfig.toString().equalsIgnoreCase(host);
        }).forEach(handler -> handler.webHookEvent(paramterMap));
    }

}
