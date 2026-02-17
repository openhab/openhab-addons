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
package org.openhab.binding.linkplay.internal;

import static org.openhab.binding.linkplay.internal.LinkPlayBindingConstants.*;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.binding.linkplay.internal.client.upnp.LinkPlayUpnpDeviceListener;
import org.openhab.binding.linkplay.internal.client.upnp.LinkPlayUpnpRegistry;
import org.openhab.binding.linkplay.internal.group.LinkPlayGroupService;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.common.ThreadPoolManager;
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
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkPlayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.linkplay", service = ThingHandlerFactory.class)
public class LinkPlayHandlerFactory extends BaseThingHandlerFactory implements RegistryListener, LinkPlayUpnpRegistry {
    private final Logger logger = LoggerFactory.getLogger(LinkPlayHandlerFactory.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_PLAYER);
    protected final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(LinkPlayHandlerFactory.class.getName());
    private final UpnpIOService upnpIOService;
    private final LinkPlayGroupService linkPlayGroupService;
    private final UpnpService upnpService;
    private final LinkPlayCommandDescriptionProvider linkPlayCommandDescriptionProvider;
    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private ConcurrentMap<String, RemoteDevice> devices = new ConcurrentHashMap<>();
    private ConcurrentMap<String, LinkPlayUpnpDeviceListener> deviceListeners = new ConcurrentHashMap<>();
    private Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();
    private final HttpClient httpClient;

    @Activate
    public LinkPlayHandlerFactory(final @Reference UpnpIOService upnpIOService, @Reference UpnpService upnpService,
            final @Reference LinkPlayGroupService linkPlayGroupService,
            final @Reference LinkPlayCommandDescriptionProvider linkPlayCommandDescriptionProvider,
            final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService) {
        this.upnpIOService = upnpIOService;
        this.linkPlayGroupService = linkPlayGroupService;
        this.upnpService = upnpService;
        this.linkPlayCommandDescriptionProvider = linkPlayCommandDescriptionProvider;
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
        upnpService.getRegistry().addListener(this);
        httpClient = new HttpClient(new SslContextFactory.Client(true));
        httpClient.setConnectTimeout(30000);
        httpClient.setName("LinkPlayHTTPClient");
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.debug("Failed to start HTTP client: {}", e.getMessage(), e);
            throw new IllegalStateException("Could not create HTTP client", e);
        }
    }

    @Deactivate
    protected void deactivate() {
        upnpService.getRegistry().removeListener(this);
        devices.clear();
        deviceListeners.clear();
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Failed to stop HTTP client: {}", e.getMessage(), e);
        }
        audioSinkRegistrations.values().forEach(registration -> registration.unregister());
        audioSinkRegistrations.clear();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_PLAYER.equals(thingTypeUID)) {
            LinkPlayHandler handler = new LinkPlayHandler(thing, httpClient, this, upnpIOService, upnpService,
                    linkPlayGroupService, linkPlayCommandDescriptionProvider);
            LinkPlayAudioSink audioSink = new LinkPlayAudioSink(handler, audioHTTPServer, createCallbackUrl());
            ServiceRegistration<AudioSink> audioSinkRegistration = bundleContext.registerService(AudioSink.class,
                    audioSink, new Hashtable<>());
            audioSinkRegistrations.put(thing.getUID().toString(), audioSinkRegistration);
            return handler;
        }
        return null;
    }

    @Override
    public void addDeviceListener(String udn, LinkPlayUpnpDeviceListener listener) {
        deviceListeners.put(udn, listener);
        // trigger listeners but not in the same thread
        scheduler.execute(() -> {
            if (!devices.containsKey(udn)) {
                Collection<RemoteDevice> devices = upnpService.getRegistry().getRemoteDevices();
                for (RemoteDevice device : devices) {
                    if (!device.isRoot() || !device.getIdentity().getUdn().getIdentifierString().equals(udn)) {
                        continue;
                    }
                    remoteDeviceAdded(null, device);
                }
            } else {
                remoteDeviceUpdated(null, devices.get(udn));
            }
        });
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof LinkPlayHandler handler) {
            ServiceRegistration<AudioSink> audioSinkRegistration = audioSinkRegistrations
                    .get(handler.getThing().getUID().toString());
            if (audioSinkRegistration != null) {
                audioSinkRegistration.unregister();
                audioSinkRegistrations.remove(handler.getThing().getUID().toString());
            }
        }
    }

    @Override
    public void removeDeviceListener(String udn) {
        deviceListeners.remove(udn);
    }

    @Override
    public void remoteDeviceDiscoveryStarted(@Nullable Registry registry, @Nullable RemoteDevice device) {
    }

    @Override
    public void remoteDeviceDiscoveryFailed(@Nullable Registry registry, @Nullable RemoteDevice device,
            @Nullable Exception e) {
    }

    @Override
    public void remoteDeviceAdded(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }

        logger.trace("remoteDeviceAdded: {}", device.getIdentity().getUdn().getIdentifierString());
        if ("MediaRenderer".equals(device.getType().getType())) {
            remoteDeviceUpdated(null, device);
        }
    }

    @Override
    public void remoteDeviceUpdated(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }
        String udn = device.getIdentity().getUdn().getIdentifierString();
        devices.put(udn, device);
        LinkPlayUpnpDeviceListener deviceListener = deviceListeners.get(udn);
        if (deviceListener != null) {
            deviceListener.updateDeviceConfig(device);
        }
    }

    @Override
    public void remoteDeviceRemoved(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }
        logger.trace("remoteDeviceRemoved: {}", device.getIdentity().getUdn().getIdentifierString());
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
        devices.clear();
    }

    @Override
    public void afterShutdown() {
    }

    private @Nullable String createCallbackUrl() {
        final String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return null;
        }

        final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            logger.warn("Cannot find port of the http service.");
            return null;
        }

        return "http://" + ipAddress + ":" + port;
    }
}
