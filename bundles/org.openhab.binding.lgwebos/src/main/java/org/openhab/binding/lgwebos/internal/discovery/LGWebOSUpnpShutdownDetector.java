/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lgwebos.internal.discovery;

import static org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class detects TV shutdown much before TV closes the websocket by listening to the upnp registry.
 *
 * Upnp devices send a good-bye broadcast on normal shutdown. This is true also for webos devices.
 * This seems to be the only way to detect instantly that the TV was turned off by remote control and much before the
 * device actually closes the websocket connection.
 *
 * However, not all users do use Upnp, so this use case is an optional optimization.
 *
 *
 * @author Sebastian Prehn - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true, configurationPid = "discovery.lgwebos.shutdown")
public class LGWebOSUpnpShutdownDetector implements RegistryListener {

    private final Logger logger = LoggerFactory.getLogger(LGWebOSUpnpShutdownDetector.class);

    private final ThingRegistry thingRegistry;
    private final UpnpService upnpService;

    @Activate
    public LGWebOSUpnpShutdownDetector(final @Reference UpnpService upnpService,
            final @Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
        this.upnpService = upnpService;
        upnpService.getRegistry().addListener(this);
    }

    @Deactivate
    protected void deactivate() {
        upnpService.getRegistry().removeListener(this);
    }

    @Override
    public void remoteDeviceDiscoveryStarted(@Nullable Registry registry, @Nullable RemoteDevice device) {
        // nothing to do
    }

    @Override
    public void remoteDeviceDiscoveryFailed(@Nullable Registry registry, @Nullable RemoteDevice device,
            @Nullable Exception ex) {
        // nothing to do
    }

    @Override
    public void remoteDeviceAdded(@Nullable Registry registry, @Nullable RemoteDevice device) {
        // nothing to do
    }

    @Override
    public void remoteDeviceUpdated(@Nullable Registry registry, @Nullable RemoteDevice device) {
        // nothing to do
    }

    @Override
    public void remoteDeviceRemoved(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }
        String ip = device.getIdentity().getDescriptorURL().getHost();
        thingRegistry.getAll().stream().filter(thing -> THING_TYPE_WEBOSTV.equals(thing.getThingTypeUID()))
                .filter(thing -> ip.equals(thing.getConfiguration().get(CONFIG_HOST))).forEach(thing -> {
                    ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        ((LGWebOSHandler) handler).postUpdate(CHANNEL_POWER, OnOffType.OFF);
                    }
                });
    }

    @Override
    public void localDeviceAdded(@Nullable Registry registry, @Nullable LocalDevice device) {
        // nothing to do
    }

    @Override
    public void localDeviceRemoved(@Nullable Registry registry, @Nullable LocalDevice device) {
        // nothing to do
    }

    @Override
    public void beforeShutdown(@Nullable Registry registry) {
        // nothing to do
    }

    @Override
    public void afterShutdown() {
        // nothing to do
    }

}
