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
package org.openhab.binding.bluetooth.roaming.internal;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * The {@link RoamingBluetoothDiscoveryParticipant} acts as the roaming adapter's gateway
 * to the osgi layer where it can find other adapter instances to use as delegates.
 * This class also serves to generate the default roaming adapter discovery result for the user.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { BluetoothDiscoveryParticipant.class })
public class RoamingBluetoothDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    private final Set<BluetoothAdapter> adapters = new CopyOnWriteArraySet<>();

    private Optional<RoamingBluetoothAdapter> roamingAdapter = Optional.empty();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    protected void setRoamingBluetoothAdapter(RoamingBluetoothAdapter roamingAdapter) {
        this.roamingAdapter = Optional.of(roamingAdapter);
        adapters.forEach(roamingAdapter::addBluetoothAdapter);
    }

    protected void unsetRoamingBluetoothAdapter(RoamingBluetoothAdapter roamingAdapter) {
        this.roamingAdapter = Optional.empty();
        adapters.forEach(roamingAdapter::removeBluetoothAdapter);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.add(adapter);

        roamingAdapter.ifPresent(ra -> {
            ra.addBluetoothAdapter(adapter);
        });
    }

    protected void removeBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.remove(adapter);

        roamingAdapter.ifPresent(ra -> {
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
        roamingAdapter.ifPresent(roamingAdapter -> {
            if (roamingAdapter.isDiscoveryEnabled()) {
                publisher.accept(roamingAdapter, result);
            }
        });
    }

}
