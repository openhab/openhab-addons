/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.handler;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lightify.internal.discovery.LightifyDeviceDiscoveryService;
import org.openhab.binding.lightify.internal.link.LightifyLink;
import org.osgi.framework.ServiceRegistration;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.lightify.internal.LightifyConstants.PROPERTY_ADDRESS;
import static org.openhab.binding.lightify.internal.LightifyConstants.THING_TYPE_LIGHTIFY_GATEWAY;

/**
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class GatewayHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_TYPES = Collections.singleton(THING_TYPE_LIGHTIFY_GATEWAY);

    private LightifyDeviceDiscoveryService discoveryService;
    private ServiceRegistration<?> serviceRegistration;

    private String address;
    private LightifyLink lightifyLink;
    private ScheduledFuture<?> futureConnect;

    public GatewayHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        address = getConfig().get(PROPERTY_ADDRESS).toString();
        scheduleConnect(true);
        serviceRegistration = registerDeviceDiscoveryService();
    }

    @Override
    public void dispose() {
        if (lightifyLink != null) {
            lightifyLink.disconnect();
        }
        if (serviceRegistration != null) {
            discoveryService.deactivate();
            discoveryService = null;
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
    }

    public LightifyLink getLightifyLink() {
        return lightifyLink;
    }

    private ServiceRegistration<?> registerDeviceDiscoveryService() {
        discoveryService = new LightifyDeviceDiscoveryService(this);
        discoveryService.activate();
        return registerService(DiscoveryService.class, discoveryService);
    }

    private void scheduleConnect(boolean immediate) {
        final long delay = immediate ? 0 : 10;
        if (futureConnect != null) {
            futureConnect.cancel(true);
            futureConnect = null;
        }
        futureConnect = scheduler.schedule(() -> {
            lightifyLink = new LightifyLink(address);
            updateStatus(ThingStatus.ONLINE);
            searchDevices();
        }, delay, TimeUnit.SECONDS);

    }

    private void searchDevices() {
        discoveryService.startScan(null);
    }

    private <S> ServiceRegistration<S> registerService(Class<S> serviceType, S service) {
        return bundleContext.registerService(serviceType, service, new Hashtable<>());
    }
}
