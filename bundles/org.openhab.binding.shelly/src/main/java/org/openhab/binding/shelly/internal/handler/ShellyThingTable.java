/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.discovery.ShellyBasicDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

/***
 * The{@link ShellyThingTable} implements a simple table to allow dispatching incoming events to the proper thing
 * handler.
 * <p>
 * <b>Note:</b> This class is an OSGi component and is intended to be managed as a singleton by the framework.
 * Therefore, do <b>not</b> instantiate it directly via its constructor; always obtain the instance from OSGi.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = ShellyThingTable.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class ShellyThingTable {

    // All access must be guarded by "this"
    private final Map<String, ShellyThingInterface> table = new HashMap<>();

    // All access must be guarded by "this"
    private @Nullable ShellyBasicDiscoveryService discoveryService;

    public synchronized @Nullable ShellyThingInterface addThing(String key, ShellyThingInterface thing) {
        return table.put(key, thing);
    }

    public @Nullable ShellyThingInterface findThing(String key) {
        List<ShellyThingInterface> values;
        synchronized (this) {
            ShellyThingInterface result = table.get(key);
            if (result != null) {
                return result;
            }
            values = List.copyOf(table.values());
        }
        for (ShellyThingInterface thingInterface : values) {
            if (thingInterface.checkRepresentation(key)) {
                return thingInterface;
            }
        }
        return null;
    }

    public ShellyThingInterface getThing(String key) {
        ShellyThingInterface t = findThing(key);
        if (t == null) {
            throw new IllegalArgumentException("Unknown thing for key '" + key + "'");
        }
        return t;
    }

    public synchronized @Nullable ShellyThingInterface removeThing(String key) {
        return table.remove(key);
    }

    public synchronized Map<String, ShellyThingInterface> getAll() {
        return Map.copyOf(table);
    }

    public synchronized int size() {
        return table.size();
    }

    /**
     * Start the discovery service by registering the service with OSGi.
     *
     * @param bundleContext the {@link BundleContext} to use.
     * @throws IllegalStateException If the {@link BundleContext} is no longer valid.
     */
    public void startDiscoveryService(BundleContext bundleContext) throws IllegalStateException {
        synchronized (this) {
            ShellyBasicDiscoveryService discoveryService = this.discoveryService;
            if (discoveryService != null) {
                return;
            }
            this.discoveryService = discoveryService = new ShellyBasicDiscoveryService(bundleContext, this);
            discoveryService.registerDeviceDiscoveryService();
        }
    }

    public void startScan() {
        List<ShellyThingInterface> values;
        synchronized (this) {
            values = List.copyOf(table.values());
        }
        for (ShellyThingInterface thingInterface : values) {
            thingInterface.startScan();
        }
    }

    public void stopDiscoveryService() {
        synchronized (this) {
            ShellyBasicDiscoveryService discoveryService = this.discoveryService;
            this.discoveryService = null;
            if (discoveryService != null) {
                discoveryService.unregisterDeviceDiscoveryService();
            }
        }
    }

    public void discoveredResult(ThingTypeUID uid, String model, String serviceName, String address,
            Map<String, Object> properties) {
        ShellyBasicDiscoveryService discoveryService;
        synchronized (this) {
            discoveryService = this.discoveryService;
        }
        if (discoveryService != null) {
            discoveryService.discoveredResult(uid, model, serviceName, address, properties);
        }
    }

    public void discoveredResult(DiscoveryResult result) {
        ShellyBasicDiscoveryService discoveryService;
        synchronized (this) {
            discoveryService = this.discoveryService;
        }
        if (discoveryService != null) {
            discoveryService.discoveredResult(result);
        }
    }

    @Deactivate
    public void deactivate() {
        stopDiscoveryService();
    }
}
