/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.binding.upnpcontrol.internal.audiosink.UpnpAudioSink;
import org.openhab.binding.upnpcontrol.internal.audiosink.UpnpAudioSinkReg;
import org.openhab.binding.upnpcontrol.internal.audiosink.UpnpNotificationAudioSink;
import org.openhab.binding.upnpcontrol.internal.config.UpnpControlBindingConfiguration;
import org.openhab.binding.upnpcontrol.internal.handler.UpnpHandler;
import org.openhab.binding.upnpcontrol.internal.handler.UpnpRendererHandler;
import org.openhab.binding.upnpcontrol.internal.handler.UpnpServerHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mark Herwege - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.upnpcontrol")
@NonNullByDefault
public class UpnpControlHandlerFactory extends BaseThingHandlerFactory implements UpnpAudioSinkReg, RegistryListener {
    final UpnpControlBindingConfiguration configuration = new UpnpControlBindingConfiguration();

    private final Logger logger = LoggerFactory.getLogger(UpnpControlHandlerFactory.class);

    private ConcurrentMap<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();
    private ConcurrentMap<String, UpnpRendererHandler> upnpRenderers = new ConcurrentHashMap<>();
    private ConcurrentMap<String, UpnpServerHandler> upnpServers = new ConcurrentHashMap<>();
    private ConcurrentMap<String, UpnpHandler> handlers = new ConcurrentHashMap<>();
    private ConcurrentMap<String, RemoteDevice> devices = new ConcurrentHashMap<>();

    private final UpnpIOService upnpIOService;
    private final UpnpService upnpService;
    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private final UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider;
    private final UpnpDynamicCommandDescriptionProvider upnpCommandDescriptionProvider;

    private String callbackUrl = "";

    @Activate
    public UpnpControlHandlerFactory(final @Reference UpnpIOService upnpIOService, @Reference UpnpService upnpService,
            final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference UpnpDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            final @Reference UpnpDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider,
            Map<String, Object> config) {
        this.upnpIOService = upnpIOService;
        this.upnpService = upnpService;
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
        this.upnpStateDescriptionProvider = dynamicStateDescriptionProvider;
        this.upnpCommandDescriptionProvider = dynamicCommandDescriptionProvider;

        upnpService.getRegistry().addListener(this);

        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        // We update instead of replace the configuration object, so that if the user updates the
        // configuration, the values are automatically available in all handlers. Because they all
        // share the same instance.
        configuration.update(new Configuration(config).as(UpnpControlBindingConfiguration.class));
        logger.debug("Updated binding configuration to {}", configuration);
    }

    @Deactivate
    protected void deActivate() {
        upnpService.getRegistry().removeListener(this);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_RENDERER)) {
            return addRenderer(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SERVER)) {
            return addServer(thing);
        }
        return null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        String key = thing.getUID().toString();

        if (thingTypeUID.equals(THING_TYPE_RENDERER)) {
            removeRenderer(key);
        } else if (thingTypeUID.equals(THING_TYPE_SERVER)) {
            removeServer(key);
        }
        super.unregisterHandler(thing);
    }

    private UpnpServerHandler addServer(Thing thing) {
        UpnpServerHandler handler = new UpnpServerHandler(thing, upnpIOService, upnpRenderers,
                upnpStateDescriptionProvider, upnpCommandDescriptionProvider, configuration);
        String key = thing.getUID().toString();
        upnpServers.put(key, handler);
        logger.debug("Media server handler created for {} with UID {}", thing.getLabel(), thing.getUID());

        String udn = handler.getUDN();
        if (udn != null) {
            handlers.put(udn, handler);
            remoteDeviceUpdated(null, devices.get(udn));
        }

        return handler;
    }

    private UpnpRendererHandler addRenderer(Thing thing) {
        callbackUrl = createCallbackUrl();
        UpnpRendererHandler handler = new UpnpRendererHandler(thing, upnpIOService, this, upnpStateDescriptionProvider,
                upnpCommandDescriptionProvider, configuration);
        String key = thing.getUID().toString();
        upnpRenderers.put(key, handler);
        upnpServers.forEach((thingId, value) -> value.addRendererOption(key));
        logger.debug("Media renderer handler created for {} with UID {}", thing.getLabel(), thing.getUID());

        String udn = handler.getUDN();
        if (udn != null) {
            handlers.put(udn, handler);
            remoteDeviceUpdated(null, devices.get(udn));
        }

        return handler;
    }

    private void removeServer(String key) {
        UpnpHandler handler = upnpServers.get(key);
        if (handler == null) {
            return;
        }
        logger.debug("Removing media server handler for {} with UID {}", handler.getThing().getLabel(),
                handler.getThing().getUID());
        handlers.remove(handler.getUDN());
        upnpServers.remove(key);
    }

    private void removeRenderer(String key) {
        UpnpHandler handler = upnpServers.get(key);
        if (handler == null) {
            return;
        }
        logger.debug("Removing media renderer handler for {} with UID {}", handler.getThing().getLabel(),
                handler.getThing().getUID());

        if (audioSinkRegistrations.containsKey(key)) {
            logger.debug("Removing audio sink registration for {}", handler.getThing().getLabel());
            ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(key);
            if (reg != null) {
                reg.unregister();
            }
            audioSinkRegistrations.remove(key);
        }

        String notificationKey = key + NOTIFICATION_AUDIOSINK_EXTENSION;
        if (audioSinkRegistrations.containsKey(notificationKey)) {
            logger.debug("Removing notification audio sink registration for {}", handler.getThing().getLabel());
            ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(notificationKey);
            if (reg != null) {
                reg.unregister();
            }
            audioSinkRegistrations.remove(notificationKey);
        }

        upnpServers.forEach((thingId, value) -> value.removeRendererOption(key));
        handlers.remove(handler.getUDN());
        upnpRenderers.remove(key);
    }

    @Override
    public void registerAudioSink(UpnpRendererHandler handler) {
        if (!(callbackUrl.isEmpty())) {
            UpnpAudioSink audioSink = new UpnpAudioSink(handler, audioHTTPServer, callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), audioSink, new Hashtable<String, Object>());
            Thing thing = handler.getThing();
            audioSinkRegistrations.put(thing.getUID().toString(), reg);
            logger.debug("Audio sink added for media renderer {}", thing.getLabel());

            UpnpNotificationAudioSink notificationAudioSink = new UpnpNotificationAudioSink(handler, audioHTTPServer,
                    callbackUrl);
            @SuppressWarnings("unchecked")
            ServiceRegistration<AudioSink> notificationReg = (ServiceRegistration<AudioSink>) bundleContext
                    .registerService(AudioSink.class.getName(), notificationAudioSink, new Hashtable<String, Object>());
            audioSinkRegistrations.put(thing.getUID().toString() + NOTIFICATION_AUDIOSINK_EXTENSION, notificationReg);
            logger.debug("Notification audio sink added for media renderer {}", thing.getLabel());
        }
    }

    private String createCallbackUrl() {
        if (!callbackUrl.isEmpty()) {
            return callbackUrl;
        }
        NetworkAddressService nwaService = networkAddressService;
        String ipAddress = nwaService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return "";
        }
        int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Cannot find port of the http service.");
            return "";
        }
        return "http://" + ipAddress + ":" + port;
    }

    @Override
    public void remoteDeviceDiscoveryStarted(@Nullable Registry registry, @Nullable RemoteDevice device) {
    }

    @Override
    public void remoteDeviceDiscoveryFailed(@Nullable Registry registry, @Nullable RemoteDevice device,
            @Nullable Exception ex) {
    }

    @Override
    public void remoteDeviceAdded(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }

        String udn = device.getIdentity().getUdn().getIdentifierString();
        if ("MediaServer".equals(device.getType().getType()) || "MediaRenderer".equals(device.getType().getType())) {
            devices.put(udn, device);
        }

        if (handlers.containsKey(udn)) {
            remoteDeviceUpdated(registry, device);
        }
    }

    @Override
    public void remoteDeviceUpdated(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }

        String udn = device.getIdentity().getUdn().getIdentifierString();
        UpnpHandler handler = handlers.get(udn);
        if (handler != null) {
            handler.updateDeviceConfig(device);
        }
    }

    @Override
    public void remoteDeviceRemoved(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }
        devices.remove(device.getIdentity().getUdn().getIdentifierString());
    }

    @Override
    public void localDeviceAdded(@Nullable Registry registry, @Nullable LocalDevice device) {
    }

    @Override
    public void localDeviceRemoved(@Nullable Registry registry, @Nullable LocalDevice device) {
    }

    @Override
    public void beforeShutdown(@Nullable Registry registry) {
        devices = new ConcurrentHashMap<>();
    }

    @Override
    public void afterShutdown() {
    }
}
