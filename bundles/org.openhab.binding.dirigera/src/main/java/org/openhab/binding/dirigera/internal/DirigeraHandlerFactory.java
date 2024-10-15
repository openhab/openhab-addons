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
package org.openhab.binding.dirigera.internal;

import static org.openhab.binding.dirigera.internal.Constants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.binding.dirigera.internal.handler.ColorLightHandler;
import org.openhab.binding.dirigera.internal.handler.ContactSensorHandler;
import org.openhab.binding.dirigera.internal.handler.DirigeraHandler;
import org.openhab.binding.dirigera.internal.handler.LightSensorHandler;
import org.openhab.binding.dirigera.internal.handler.MotionSensorHandler;
import org.openhab.binding.dirigera.internal.handler.SmartPlugHandler;
import org.openhab.binding.dirigera.internal.handler.SpeakerHandler;
import org.openhab.binding.dirigera.internal.handler.TemperatureLightHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
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
    private final DirigeraDiscoveryManager discoveryManager;
    private final TimeZoneProvider timeZoneProvider;
    private final Storage<String> bindingStorage;
    private final HttpClient insecureClient;

    @Activate
    public DirigeraHandlerFactory(@Reference HttpClientFactory hcf, @Reference StorageService storageService,
            final @Reference NetworkAddressService networkService, final @Reference DirigeraDiscoveryManager manager,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.discoveryManager = manager;
        this.timeZoneProvider = timeZoneProvider;
        this.insecureClient = new HttpClient(new SslContextFactory.Client(true));
        insecureClient.setUserAgentField(null);
        try {
            this.insecureClient.start();
            // from https://github.com/jetty-project/jetty-reactive-httpclient/issues/33#issuecomment-777771465
            insecureClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.start()
            logger.warn("Failed to start http client: {}", e.getMessage());
            throw new IllegalStateException("Could not create HttpClient", e);
        }
        String ip = networkService.getPrimaryIpv4HostAddress();
        if (ip == null) {
            logger.warn("Cannot find host IP");
            ip = "";
        } else {
            manager.initialize(insecureClient, ip);
        }
        bindingStorage = storageService.getStorage(BINDING_ID);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean isSupported = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        // logger.warn("Request for {} is suppoerted {}", thingTypeUID, isSupported);
        return isSupported;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            return new DirigeraHandler((Bridge) thing, insecureClient, bindingStorage, discoveryManager,
                    timeZoneProvider);
        } else if (THING_TYPE_COLOR_LIGHT.equals(thingTypeUID)) {
            return new ColorLightHandler(thing, COLOR_LIGHT_MAP);
        } else if (THING_TYPE_TEMPERATURE_LIGHT.equals(thingTypeUID)) {
            return new TemperatureLightHandler(thing, TEMPERATURE_LIGHT_MAP);
        } else if (THING_TYPE_MOTION_SENSOR.equals(thingTypeUID)) {
            return new MotionSensorHandler(thing, MOTION_SENSOR_MAP);
        } else if (THING_TYPE_LIGHT_SENSOR.equals(thingTypeUID)) {
            return new LightSensorHandler(thing, LIGHT_SENSOR_MAP);
        } else if (THING_TYPE_CONTACT_SENSOR.equals(thingTypeUID)) {
            return new ContactSensorHandler(thing, CONTACT_SENSOR_MAP);
        } else if (THING_TYPE_SMART_PLUG.equals(thingTypeUID)) {
            return new SmartPlugHandler(thing, SMART_PLUG_MAP);
        } else if (THING_TYPE_SPEAKER.equals(thingTypeUID)) {
            return new SpeakerHandler(thing, SPEAKER_MAP);
        } else {
            logger.info("Request for {} doesn't match {}", thingTypeUID, THING_TYPE_GATEWAY);
            return null;
        }
    }
}
