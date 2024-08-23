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
package org.openhab.binding.sonos.internal;

import static org.openhab.binding.sonos.internal.SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.sonos.internal.config.ZonePlayerConfiguration.UDN;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonos.internal.handler.ZonePlayerHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonosHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.sonos")
public class SonosHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SonosHandlerFactory.class);

    // Bindings should not use the ThingRegistry! See https://github.com/openhab/openhab-addons/pull/6080 and
    // https://github.com/eclipse/smarthome/issues/5182
    private final ThingRegistry thingRegistry;
    private final UpnpIOService upnpIOService;
    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private final SonosStateDescriptionOptionProvider stateDescriptionProvider;

    private final Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    // optional OPML URL that can be configured through configuration admin
    private @Nullable String opmlUrl;

    // url (scheme+server+port) to use for playing notification sounds
    private @Nullable String callbackUrl;

    @Activate
    public SonosHandlerFactory(final @Reference ThingRegistry thingRegistry,
            final @Reference UpnpIOService upnpIOService, final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference SonosStateDescriptionOptionProvider stateDescriptionProvider) {
        this.thingRegistry = thingRegistry;
        this.upnpIOService = upnpIOService;
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        opmlUrl = (String) properties.get("opmlUrl");
        callbackUrl = (String) properties.get("callbackUrl");
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID sonosDeviceUID = getPlayerUID(thingTypeUID, thingUID, configuration);
            logger.debug("Creating a sonos thing with ID '{}'", sonosDeviceUID);
            return super.createThing(thingTypeUID, configuration, sonosDeviceUID, null);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the sonos binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            logger.debug("Creating a ZonePlayerHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get(UDN));

            ZonePlayerHandler handler = new ZonePlayerHandler(thingRegistry, thing, upnpIOService, opmlUrl,
                    stateDescriptionProvider);

            // register the speaker as an audio sink
            String callbackUrl = createCallbackUrl();
            SonosAudioSink audioSink = new SonosAudioSink(handler, audioHTTPServer, callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) getBundleContext()
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<>());
            audioSinkRegistrations.put(thing.getUID().toString(), reg);

            return handler;
        }
        return null;
    }

    private @Nullable String createCallbackUrl() {
        if (callbackUrl != null) {
            return callbackUrl;
        } else {
            final String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
            if (ipAddress == null) {
                logger.warn("No network interface could be found.");
                return null;
            }

            // we do not use SSL as it can cause certificate validation issues.
            final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
            if (port == -1) {
                logger.warn("Cannot find port of the http service.");
                return null;
            }

            return "http://" + ipAddress + ":" + port;
        }
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(thing.getUID().toString());
        if (reg != null) {
            reg.unregister();
        }
    }

    private ThingUID getPlayerUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID, Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        } else {
            String udn = (String) configuration.get(UDN);
            return new ThingUID(thingTypeUID, udn);
        }
    }
}
