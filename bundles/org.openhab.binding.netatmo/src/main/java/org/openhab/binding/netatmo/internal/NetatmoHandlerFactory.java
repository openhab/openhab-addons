/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.DeviceWithEventHandler;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.netatmo")
public class NetatmoHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(NetatmoHandlerFactory.class);

    private final NetatmoDescriptionProvider stateDescriptionProvider;
    private final ApiBridge apiBridge;
    private final NetatmoServlet webhookServlet;

    @Activate
    public NetatmoHandlerFactory(@Reference ApiBridge apiBridge, @Reference NetatmoServlet webhookServlet,
            @Reference NetatmoDescriptionProvider stateDescriptionProvider) {
        this.apiBridge = apiBridge;
        this.webhookServlet = webhookServlet;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ModuleType.asSet.stream().anyMatch(mt -> mt.thingTypeUID.equals(thingTypeUID));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        Bridge bridge = (Bridge) thing;
        BaseThingHandler handler = ModuleType.asSet.stream().filter(mt -> mt.thingTypeUID.equals(thingTypeUID))
                .findFirst().map(mt -> buildThing(bridge, mt)).orElse(null);
        if (handler instanceof DeviceWithEventHandler) {
            ((DeviceWithEventHandler) handler).setWebHookServlet(webhookServlet);
        }
        return handler;
    }

    private @Nullable BaseThingHandler buildThing(Bridge bridge, ModuleType moduleType) {
        List<AbstractChannelHelper> helpers = new ArrayList<>();
        try {
            Constructor<?> handlerConstructor = moduleType.getHandlerConstructor();
            for (Class<?> helperClass : moduleType.channelHelpers) {
                Constructor<?> helperConstructor = helperClass.getConstructor();
                Object helper = helperConstructor.newInstance();
                if (helper instanceof AbstractChannelHelper) {
                    helpers.add((AbstractChannelHelper) helper);
                }
            }
            return (BaseThingHandler) handlerConstructor.newInstance(bridge, helpers, apiBridge,
                    stateDescriptionProvider);
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            logger.warn("Error creating or initializing constructor : {}", e.getMessage());
        }
        return null;
    }
}
