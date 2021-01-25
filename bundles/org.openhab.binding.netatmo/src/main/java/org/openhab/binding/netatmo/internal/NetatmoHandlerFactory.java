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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.SignalHelper;
import org.openhab.binding.netatmo.internal.handler.HomeSecurityHandler;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.netatmo", property = Constants.SERVICE_PID
        + "=" + SERVICE_PID)
public class NetatmoHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(NetatmoHandlerFactory.class);

    private final TimeZoneProvider timeZoneProvider;
    private final NetatmoDescriptionProvider stateDescriptionProvider;
    private final ApiBridge apiBridge;
    private final NetatmoServlet webhookServlet;

    @Activate
    public NetatmoHandlerFactory(@Reference HttpService httpService,
            @Reference NetatmoDescriptionProvider stateDescriptionProvider,
            @Reference TimeZoneProvider timeZoneProvider, @Reference ApiBridge apiBridge,
            @Reference NetatmoServlet webhookServlet) {
        this.webhookServlet = webhookServlet;
        this.timeZoneProvider = timeZoneProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.apiBridge = apiBridge;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return (thingTypeUID.getBindingId().equals(BINDING_ID));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        Bridge bridge = (Bridge) thing;
        BaseThingHandler handler;
        for (ModuleType mt : ModuleType.values()) {
            if (mt.matches(thingTypeUID)) {
                handler = build(bridge, mt);
                if (handler instanceof HomeSecurityHandler) {
                    ((HomeSecurityHandler) handler).setWebHookServlet(webhookServlet);
                }
                return handler;
            }
        }
        logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
        return null;
    }

    public @Nullable BaseThingHandler build(Bridge bridge, ModuleType moduleType) {
        List<AbstractChannelHelper> helpers = new ArrayList<>();
        if (moduleType.getSignalLevels() != NetatmoConstants.NO_RADIO) {
            helpers.add(new SignalHelper(bridge, timeZoneProvider, moduleType.getSignalLevels()));
        }
        try {
            for (Class<? extends AbstractChannelHelper> helperClass : moduleType.channelHelpers) {
                Constructor<?> constructor = helperClass.getConstructor(Thing.class, TimeZoneProvider.class);
                AbstractChannelHelper helper = (AbstractChannelHelper) constructor
                        .newInstance(new Object[] { bridge, timeZoneProvider });
                if (helper != null) {
                    helpers.add(helper);
                }
            }
            Constructor<?> constructor = moduleType.handlerClass.getConstructor(Bridge.class, List.class,
                    ApiBridge.class, TimeZoneProvider.class, NetatmoDescriptionProvider.class);
            return (BaseThingHandler) constructor.newInstance(
                    new Object[] { bridge, helpers, apiBridge, timeZoneProvider, stateDescriptionProvider });
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.warn("Error creating calling constructor : {}", e.getMessage());
        }
        return null;
    }
}
