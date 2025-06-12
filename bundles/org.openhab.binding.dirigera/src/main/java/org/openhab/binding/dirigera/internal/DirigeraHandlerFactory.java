/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal;

import static org.openhab.binding.dirigera.internal.Constants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryService;
import org.openhab.binding.dirigera.internal.handler.DirigeraHandler;
import org.openhab.binding.dirigera.internal.handler.airpurifier.AirPurifierHandler;
import org.openhab.binding.dirigera.internal.handler.blind.BlindHandler;
import org.openhab.binding.dirigera.internal.handler.controller.BlindsControllerHandler;
import org.openhab.binding.dirigera.internal.handler.controller.DoubleShortcutControllerHandler;
import org.openhab.binding.dirigera.internal.handler.controller.LightControllerHandler;
import org.openhab.binding.dirigera.internal.handler.controller.ShortcutControllerHandler;
import org.openhab.binding.dirigera.internal.handler.controller.SoundControllerHandler;
import org.openhab.binding.dirigera.internal.handler.light.ColorLightHandler;
import org.openhab.binding.dirigera.internal.handler.light.DimmableLightHandler;
import org.openhab.binding.dirigera.internal.handler.light.SwitchLightHandler;
import org.openhab.binding.dirigera.internal.handler.light.TemperatureLightHandler;
import org.openhab.binding.dirigera.internal.handler.plug.PowerPlugHandler;
import org.openhab.binding.dirigera.internal.handler.plug.SimplePlugHandler;
import org.openhab.binding.dirigera.internal.handler.plug.SmartPlugHandler;
import org.openhab.binding.dirigera.internal.handler.repeater.RepeaterHandler;
import org.openhab.binding.dirigera.internal.handler.scene.SceneHandler;
import org.openhab.binding.dirigera.internal.handler.sensor.AirQualityHandler;
import org.openhab.binding.dirigera.internal.handler.sensor.ContactSensorHandler;
import org.openhab.binding.dirigera.internal.handler.sensor.MotionLightSensorHandler;
import org.openhab.binding.dirigera.internal.handler.sensor.MotionSensorHandler;
import org.openhab.binding.dirigera.internal.handler.sensor.WaterSensorHandler;
import org.openhab.binding.dirigera.internal.handler.speaker.SpeakerHandler;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DirigeraHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.dirigera", service = ThingHandlerFactory.class)
public class DirigeraHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(DirigeraHandlerFactory.class);
    private final DirigeraStateDescriptionProvider stateProvider;
    private final DirigeraDiscoveryService discoveryService;
    private final DirigeraCommandProvider commandProvider;
    private final LocationProvider locationProvider;
    private final Storage<String> bindingStorage;
    private final HttpClient insecureClient;

    @Activate
    public DirigeraHandlerFactory(@Reference StorageService storageService,
            final @Reference DirigeraDiscoveryService discovery, final @Reference LocationProvider locationProvider,
            final @Reference DirigeraCommandProvider commandProvider,
            final @Reference DirigeraStateDescriptionProvider stateProvider) {
        this.locationProvider = locationProvider;
        this.commandProvider = commandProvider;
        this.discoveryService = discovery;
        this.stateProvider = stateProvider;

        this.insecureClient = new HttpClient(new SslContextFactory.Client(true));
        insecureClient.setUserAgentField(null);
        try {
            this.insecureClient.start();
            // from https://github.com/jetty-project/jetty-reactive-httpclient/issues/33#issuecomment-777771465
            insecureClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.start()
            logger.warn("DIRIGERA FACTORY Failed to start http client: {}", e.getMessage());
            throw new IllegalStateException("Could not create HttpClient", e);
        }
        bindingStorage = storageService.getStorage(BINDING_ID);
    }

    @Deactivate
    public void deactivate() {
        try {
            insecureClient.stop();
        } catch (Exception e) {
            logger.warn("Failed to stop http client: {}", e.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            return new DirigeraHandler((Bridge) thing, insecureClient, bindingStorage, discoveryService,
                    locationProvider, commandProvider, bundleContext);
        } else if (THING_TYPE_COLOR_LIGHT.equals(thingTypeUID)) {
            return new ColorLightHandler(thing, COLOR_LIGHT_MAP, stateProvider);
        } else if (THING_TYPE_TEMPERATURE_LIGHT.equals(thingTypeUID)) {
            return new TemperatureLightHandler(thing, TEMPERATURE_LIGHT_MAP, stateProvider);
        } else if (THING_TYPE_DIMMABLE_LIGHT.equals(thingTypeUID)) {
            return new DimmableLightHandler(thing, TEMPERATURE_LIGHT_MAP);
        } else if (THING_TYPE_SWITCH_LIGHT.equals(thingTypeUID)) {
            return new SwitchLightHandler(thing, TEMPERATURE_LIGHT_MAP);
        } else if (THING_TYPE_MOTION_SENSOR.equals(thingTypeUID)) {
            return new MotionSensorHandler(thing, MOTION_SENSOR_MAP);
            // } else if (THING_TYPE_LIGHT_SENSOR.equals(thingTypeUID)) {
            // return new LightSensorHandler(thing, LIGHT_SENSOR_MAP);
        } else if (THING_TYPE_MOTION_LIGHT_SENSOR.equals(thingTypeUID)) {
            return new MotionLightSensorHandler(thing, MOTION_LIGHT_SENSOR_MAP);
        } else if (THING_TYPE_CONTACT_SENSOR.equals(thingTypeUID)) {
            return new ContactSensorHandler(thing, CONTACT_SENSOR_MAP);
        } else if (THING_TYPE_SIMPLE_PLUG.equals(thingTypeUID)) {
            return new SimplePlugHandler(thing, SMART_PLUG_MAP);
        } else if (THING_TYPE_POWER_PLUG.equals(thingTypeUID)) {
            return new PowerPlugHandler(thing, SMART_PLUG_MAP);
        } else if (THING_TYPE_SMART_PLUG.equals(thingTypeUID)) {
            return new SmartPlugHandler(thing, SMART_PLUG_MAP);
        } else if (THING_TYPE_SPEAKER.equals(thingTypeUID)) {
            return new SpeakerHandler(thing, SPEAKER_MAP);
        } else if (THING_TYPE_SCENE.equals(thingTypeUID)) {
            return new SceneHandler(thing, SCENE_MAP);
        } else if (THING_TYPE_REPEATER.equals(thingTypeUID)) {
            return new RepeaterHandler(thing, REPEATER_MAP);
        } else if (THING_TYPE_LIGHT_CONTROLLER.equals(thingTypeUID)) {
            return new LightControllerHandler(thing, LIGHT_CONTROLLER_MAP);
        } else if (THING_TYPE_BLIND_CONTROLLER.equals(thingTypeUID)) {
            return new BlindsControllerHandler(thing, BLIND_CONTROLLER_MAP);
        } else if (THING_TYPE_SOUND_CONTROLLER.equals(thingTypeUID)) {
            return new SoundControllerHandler(thing, SOUND_CONTROLLER_MAP);
        } else if (THING_TYPE_SINGLE_SHORTCUT_CONTROLLER.equals(thingTypeUID)) {
            return new ShortcutControllerHandler(thing, SHORTCUT_CONTROLLER_MAP, bindingStorage);
        } else if (THING_TYPE_DOUBLE_SHORTCUT_CONTROLLER.equals(thingTypeUID)) {
            return new DoubleShortcutControllerHandler(thing, SHORTCUT_CONTROLLER_MAP, bindingStorage);
        } else if (THING_TYPE_AIR_QUALITY.equals(thingTypeUID)) {
            return new AirQualityHandler(thing, AIR_QUALITY_MAP);
        } else if (THING_TYPE_WATER_SENSOR.equals(thingTypeUID)) {
            return new WaterSensorHandler(thing, WATER_SENSOR_MAP);
        } else if (THING_TYPE_BLIND.equals(thingTypeUID)) {
            return new BlindHandler(thing, BLINDS_MAP);
        } else if (THING_TYPE_AIR_PURIFIER.equals(thingTypeUID)) {
            return new AirPurifierHandler(thing, AIR_PURIFIER_MAP);
        } else {
            logger.debug("DIRIGERA FACTORY Request for {} doesn't match {}", thingTypeUID, THING_TYPE_GATEWAY);
            return null;
        }
    }
}
