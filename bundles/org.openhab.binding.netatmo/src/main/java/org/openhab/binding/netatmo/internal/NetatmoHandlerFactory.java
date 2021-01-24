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
import static org.openhab.binding.netatmo.internal.api.ModuleType.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.handler.HomeSecurityHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoHandlerBuilder;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
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

    private final TimeZoneProvider tzProvider;
    private final ApiBridge apiBridge;
    private final NetatmoServlet webhookServlet;
    private final NetatmoDescriptionProvider descProvider;

    @Activate
    public NetatmoHandlerFactory(@Reference HttpService httpService,
            @Reference NetatmoDescriptionProvider stateDescriptionProvider,
            @Reference TimeZoneProvider timeZoneProvider, @Reference ApiBridge apiBridge,
            @Reference NetatmoServlet webhookServlet) {
        this.descProvider = stateDescriptionProvider;
        this.tzProvider = timeZoneProvider;
        this.apiBridge = apiBridge;
        this.webhookServlet = webhookServlet;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return (thingTypeUID.getBindingId().equals(BINDING_ID));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        Bridge bridge = (Bridge) thing;
        if (NAHomeEnergy.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NAHomeEnergy).build();
        } else if (NAMain.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NAMain).build();
        } else if (NAModule1.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NAModule1).build();
        } else if (NAModule2.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NAModule2).build();
        } else if (NAModule3.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NAModule3).build();
        } else if (NAModule4.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NAModule4).build();
        } else if (NATherm1.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NATherm1).build();
        } else if (NAPlug.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NAPlug).build();
        } else if (NHC.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NHC).build();
        } else if (NACamera.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NACamera).build();
        } else if (NAPerson.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NAPerson).build();
        } else if (NOC.matches(thingTypeUID)) {
            return new NetatmoHandlerBuilder(bridge, tzProvider, descProvider, apiBridge, NOC).build();
        } else if (NAHomeSecurity.matches(thingTypeUID)) {
            HomeSecurityHandler handler = (HomeSecurityHandler) new NetatmoHandlerBuilder(bridge, tzProvider,
                    descProvider, apiBridge, NAHomeSecurity).build();
            if (handler != null) {
                handler.setWebHookServlet(webhookServlet);
            }
            return handler;
        }
        logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
        return null;
    }
}
