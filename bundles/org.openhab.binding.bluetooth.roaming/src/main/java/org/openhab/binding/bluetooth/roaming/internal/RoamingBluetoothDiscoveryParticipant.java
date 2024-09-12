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
package org.openhab.binding.bluetooth.roaming.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The {@link RoamingBluetoothDiscoveryParticipant} acts as the roaming adapter's gateway
 * to the osgi layer where it can find other adapter instances to use as delegates.
 * This class also serves to generate the default roaming adapter discovery result for the user.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
@Component(service = { BluetoothDiscoveryParticipant.class })
public class RoamingBluetoothDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    private final Set<BluetoothAdapter> adapters = new CopyOnWriteArraySet<>();
    private final Set<RoamingBluetoothAdapter> roamingAdapters = new CopyOnWriteArraySet<>();

    // private Optional<RoamingBluetoothAdapter> roamingAdapter = Optional.empty();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void setRoamingBluetoothAdapter(RoamingBluetoothAdapter roamingAdapter) {
        roamingAdapters.add(roamingAdapter);
        adapters.forEach(roamingAdapter::addBluetoothAdapter);
    }

    protected void unsetRoamingBluetoothAdapter(RoamingBluetoothAdapter roamingAdapter) {
        roamingAdapters.remove(roamingAdapter);
        adapters.forEach(roamingAdapter::removeBluetoothAdapter);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.add(adapter);

        roamingAdapters.forEach(ra -> {
            ra.addBluetoothAdapter(adapter);
        });
    }

    protected void removeBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.remove(adapter);

        roamingAdapters.forEach(ra -> {
            ra.removeBluetoothAdapter(adapter);
        });
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.emptySet();
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        return null;
    }

    @Override
    public void publishAdditionalResults(DiscoveryResult result,
            BiConsumer<BluetoothAdapter, DiscoveryResult> publisher) {
        // we create a roaming version of every discoveryResult.
        roamingAdapters.forEach(roamingAdapter -> {
            ThingUID adapterUID = result.getBridgeUID();
            if (adapterUID != null && roamingAdapter.isDiscoveryEnabled()
                    && roamingAdapter.isRoamingMember(adapterUID)) {
                publisher.accept(roamingAdapter, result);
            }
        });
    }
}
