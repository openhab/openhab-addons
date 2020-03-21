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
package org.openhab.binding.bluetooth.discovery.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.UID;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothCompanyIdentifiers;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDiscoveryListener;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.binding.bluetooth.internal.RoamingBluetoothAdapter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothDiscoveryService} handles searching for BLE devices.
 *
 * @author Chris Jackson - Initial Contribution
 * @author Kai Kreuzer - Introduced BluetoothAdapters and BluetoothDiscoveryParticipants
 * @author Connor Petty - Added roaming support
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.bluetooth")
public class BluetoothDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(BluetoothDiscoveryService.class);

    private static final int SEARCH_TIME = 15;
    private static final int DISCOVERY_TTL = 30;

    private final Set<BluetoothAdapter> adapters = new CopyOnWriteArraySet<>();
    private final Set<BluetoothDiscoveryParticipant> participants = new CopyOnWriteArraySet<>();
    private final Map<UID, BluetoothDiscoveryListener> registeredListeners = new ConcurrentHashMap<>();

    private final Set<ThingTypeUID> supportedThingTypes = new CopyOnWriteArraySet<>();

    private Optional<RoamingBluetoothAdapter> roamingAdapter = Optional.empty();

    public BluetoothDiscoveryService() {
        super(SEARCH_TIME);
        supportedThingTypes.add(BluetoothBindingConstants.THING_TYPE_BEACON);
        supportedThingTypes.add(BluetoothBindingConstants.THING_TYPE_ROAMING);
    }

    @Override
    @Activate
    protected void activate(Map<String, Object> configProperties) {
        logger.debug("Activating Bluetooth discovery service");
        super.activate(configProperties);
        thingDiscovered(createRoamingAdapterDiscoveryResult());
    }

    private DiscoveryResult createRoamingAdapterDiscoveryResult() {
        return DiscoveryResultBuilder.create(new ThingUID(BluetoothBindingConstants.THING_TYPE_ROAMING, "ctrl"))
                .withLabel("Bluetooth Roaming Controller").build();
    }

    @Override
    @Modified
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating Bluetooth discovery service");
    }

    @Reference
    protected void setRoamingBluetoothAdapter(RoamingBluetoothAdapter roamingAdapter) {
        this.roamingAdapter = Optional.of(roamingAdapter);
        for (BluetoothAdapter adapter : adapters) {
            roamingAdapter.addBluetoothAdapter(adapter);
        }
    }

    protected void unsetRoamingBluetoothAdapter(RoamingBluetoothAdapter roamingAdapter) {
        this.roamingAdapter = Optional.empty();
        for (BluetoothAdapter adapter : adapters) {
            roamingAdapter.removeBluetoothAdapter(adapter);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.add(adapter);
        BluetoothDiscoveryListener listener = device -> deviceDiscovered(adapter, device);
        adapter.addDiscoveryListener(listener);
        registeredListeners.put(adapter.getUID(), listener);

        roamingAdapter.ifPresent(ra -> {
            ra.addBluetoothAdapter(adapter);
        });
    }

    protected void removeBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.remove(adapter);
        adapter.removeDiscoveryListener(registeredListeners.remove(adapter.getUID()));

        roamingAdapter.ifPresent(ra -> {
            ra.removeBluetoothAdapter(adapter);
        });
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addBluetoothDiscoveryParticipant(BluetoothDiscoveryParticipant participant) {
        this.participants.add(participant);
        supportedThingTypes.addAll(participant.getSupportedThingTypeUIDs());
    }

    protected void removeBluetoothDiscoveryParticipant(BluetoothDiscoveryParticipant participant) {
        supportedThingTypes.removeAll(participant.getSupportedThingTypeUIDs());
        this.participants.remove(participant);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return supportedThingTypes;
    }

    @Override
    public void startScan() {
        for (BluetoothAdapter adapter : adapters) {
            adapter.scanStart();
        }
    }

    @Override
    public void stopScan() {
        for (BluetoothAdapter adapter : adapters) {
            adapter.scanStop();
        }
        removeOlderResults(getTimestampOfLastScan());
    }

    private void deviceDiscovered(BluetoothAdapter adapter, BluetoothDevice device) {
        logger.debug("Discovered bluetooth device '{}': {}", device.getName(), device);
        for (BluetoothDiscoveryParticipant participant : participants) {
            try {
                DiscoveryResult result = participant.createResult(device);
                if (result != null) {
                    thingDiscovered(result);
                    return;
                }
            } catch (RuntimeException e) {
                logger.warn("Participant '{}' threw an exception", participant.getClass().getName(), e);
            }
        }

        // We did not find a thing type for this device, so let's treat it as a generic one
        String label = device.getName();
        if (label == null || label.length() == 0 || label.equals(device.getAddress().toString().replace(':', '-'))) {
            label = "Bluetooth Device";
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
        Integer txPower = device.getTxPower();
        if (txPower != null && txPower > 0) {
            properties.put(BluetoothBindingConstants.PROPERTY_TXPOWER, Integer.toString(txPower));
        }
        String manufacturer = BluetoothCompanyIdentifiers.get(device.getManufacturerId());
        if (manufacturer == null) {
            logger.debug("Unknown manufacturer Id ({}) found on bluetooth device.", device.getManufacturerId());
        } else {
            properties.put(Thing.PROPERTY_VENDOR, manufacturer);
            label += " (" + manufacturer + ")";
        }

        ThingUID thingUID = new ThingUID(BluetoothBindingConstants.THING_TYPE_BEACON, adapter.getUID(),
                device.getAddress().toString().toLowerCase().replace(":", ""));

        // Create the discovery result and add to the inbox
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS).withTTL(DISCOVERY_TTL)
                .withBridge(adapter.getUID()).withLabel(label).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    protected void thingDiscovered(DiscoveryResult discoveryResult) {
        super.thingDiscovered(discoveryResult);

        roamingAdapter.ifPresent(adapter -> {
            if (adapter.isDiscoveryEnabled()) {
                // we create a roaming version of every discoveryResult.
                super.thingDiscovered(copyWithNewBridge(discoveryResult, adapter));
            }
        });
    }

    private static ThingUID createThingUIDWithBridge(DiscoveryResult result, BluetoothAdapter adapter) {
        return new ThingUID(result.getThingTypeUID(), adapter.getUID(), result.getThingUID().getId());
    }

    private static DiscoveryResult copyWithNewBridge(DiscoveryResult result, BluetoothAdapter adapter) {
        return DiscoveryResultBuilder.create(createThingUIDWithBridge(result, adapter)).withBridge(adapter.getUID())
                .withProperties(result.getProperties()).withRepresentationProperty(result.getRepresentationProperty())
                .withTTL(result.getTimeToLive()).withLabel(result.getLabel()).build();
    }

}
