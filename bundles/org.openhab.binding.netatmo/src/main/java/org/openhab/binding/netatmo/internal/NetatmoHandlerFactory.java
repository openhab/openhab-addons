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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.doc.ModuleType;
import org.openhab.binding.netatmo.internal.channelhelper.Co2ChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.HumidityChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.NoiseChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PressureChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.TemperatureChannelHelper;
import org.openhab.binding.netatmo.internal.handler.NetatmoThingHandlerBuilder;
import org.openhab.binding.netatmo.internal.handler.aircare.HealthIndexChannelHelper;
import org.openhab.binding.netatmo.internal.handler.aircare.NAHealthyHomeCoachHandler;
import org.openhab.binding.netatmo.internal.handler.energy.NADescriptionProvider;
import org.openhab.binding.netatmo.internal.handler.energy.NAHomeEnergyChannelHelper;
import org.openhab.binding.netatmo.internal.handler.energy.NAHomeEnergyHandler;
import org.openhab.binding.netatmo.internal.handler.energy.NAPlugChannelHelper;
import org.openhab.binding.netatmo.internal.handler.energy.NAPlugHandler;
import org.openhab.binding.netatmo.internal.handler.energy.NATherm1ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.energy.NATherm1Handler;
import org.openhab.binding.netatmo.internal.handler.security.NACameraChannelHelper;
import org.openhab.binding.netatmo.internal.handler.security.NACameraHandler;
import org.openhab.binding.netatmo.internal.handler.security.NAHomeSecurityChannelHelper;
import org.openhab.binding.netatmo.internal.handler.security.NAHomeSecurityHandler;
import org.openhab.binding.netatmo.internal.handler.security.NAPersonChannelHelper;
import org.openhab.binding.netatmo.internal.handler.security.NAPersonHandler;
import org.openhab.binding.netatmo.internal.handler.security.NAPresenceHandler;
import org.openhab.binding.netatmo.internal.handler.weather.NAMainHandler;
import org.openhab.binding.netatmo.internal.handler.weather.RainChannelHelper;
import org.openhab.binding.netatmo.internal.handler.weather.WindChannelHelper;
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

    private final TimeZoneProvider timeZoneProvider;

    private final ApiBridge apiBridge;
    private final NetatmoServlet webhookServlet;
    private final NADescriptionProvider stateDescriptionProvider;

    @Activate
    public NetatmoHandlerFactory(@Reference HttpService httpService,
            @Reference NADescriptionProvider stateDescriptionProvider, @Reference TimeZoneProvider timeZoneProvider,
            @Reference ApiBridge apiBridge, @Reference NetatmoServlet webhookServlet) {
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.timeZoneProvider = timeZoneProvider;
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
        if (ModuleType.NAHomeEnergy.matches(thingTypeUID)) {
            NAHomeEnergyHandler handler = (NAHomeEnergyHandler) NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NAHomeEnergy, stateDescriptionProvider)
                    .withHandler(NAHomeEnergyHandler.class).withChannelHelpers(Set.of(NAHomeEnergyChannelHelper.class))
                    .withApiBridge(apiBridge).build();
            return handler;
        } else if (ModuleType.NAHomeSecurity.matches(thingTypeUID)) {
            NAHomeSecurityHandler handler = (NAHomeSecurityHandler) NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NAHomeSecurity, stateDescriptionProvider)
                    .withHandler(NAHomeSecurityHandler.class)
                    .withChannelHelpers(Set.of(NAHomeSecurityChannelHelper.class)).withApiBridge(apiBridge).build();
            if (handler != null) {
                handler.setWebHookServlet(webhookServlet);
            }
            return handler;
        } else if (ModuleType.NAMain.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NAMain, stateDescriptionProvider)
                    .withHandler(NAMainHandler.class)
                    .withChannelHelpers(Set.of(PressureChannelHelper.class, NoiseChannelHelper.class,
                            HumidityChannelHelper.class, TemperatureChannelHelper.class, Co2ChannelHelper.class))
                    .withApiBridge(apiBridge).build();
        } else if (ModuleType.NAModule1.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NAModule1, stateDescriptionProvider)
                    .withChannelHelpers(Set.of(HumidityChannelHelper.class, TemperatureChannelHelper.class)).build();
        } else if (ModuleType.NAModule2.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NAModule2, stateDescriptionProvider)
                    .withChannelHelper(WindChannelHelper.class).build();
        } else if (ModuleType.NAModule3.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NAModule3, stateDescriptionProvider)
                    .withChannelHelper(RainChannelHelper.class).build();
        } else if (ModuleType.NAModule4.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NAModule4, stateDescriptionProvider)
                    .withChannelHelpers(
                            Set.of(HumidityChannelHelper.class, TemperatureChannelHelper.class, Co2ChannelHelper.class))
                    .build();
        } else if (ModuleType.NATherm1.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NATherm1, stateDescriptionProvider)
                    .withHandler(NATherm1Handler.class).withChannelHelper(NATherm1ChannelHelper.class).build();
        } else if (ModuleType.NAPlug.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NAPlug, stateDescriptionProvider)
                    .withHandler(NAPlugHandler.class).withChannelHelper(NAPlugChannelHelper.class)
                    .withApiBridge(apiBridge).build();
        } else if (ModuleType.NHC.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder.create(bridge, timeZoneProvider, ModuleType.NHC, stateDescriptionProvider)
                    .withHandler(NAHealthyHomeCoachHandler.class).withApiBridge(apiBridge)
                    .withChannelHelpers(Set.of(NoiseChannelHelper.class, HumidityChannelHelper.class,
                            PressureChannelHelper.class, TemperatureChannelHelper.class, Co2ChannelHelper.class,
                            HealthIndexChannelHelper.class))
                    .build();
        } else if (ModuleType.NACamera.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NACamera, stateDescriptionProvider)
                    .withApiBridge(apiBridge).withHandler(NACameraHandler.class)
                    .withChannelHelper(NACameraChannelHelper.class).build();
        } else if (ModuleType.NAPerson.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder
                    .create(bridge, timeZoneProvider, ModuleType.NAPerson, stateDescriptionProvider)
                    .withHandler(NAPersonHandler.class).withChannelHelper(NAPersonChannelHelper.class).build();
        } else if (ModuleType.NOC.matches(thingTypeUID)) {
            return NetatmoThingHandlerBuilder.create(bridge, timeZoneProvider, ModuleType.NOC, stateDescriptionProvider)
                    .withApiBridge(apiBridge).withHandler(NAPresenceHandler.class)
                    .withChannelHelper(NACameraChannelHelper.class).build();
        }
        logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
        return null;
    }
}
