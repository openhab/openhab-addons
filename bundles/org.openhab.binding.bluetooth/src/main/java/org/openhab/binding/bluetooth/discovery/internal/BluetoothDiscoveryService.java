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
package org.openhab.binding.bluetooth.discovery.internal;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDiscoveryListener;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
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
 * @author Connor Petty - Introduced connection based discovery and added roaming support
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.bluetooth")
public class BluetoothDiscoveryService extends AbstractDiscoveryService implements BluetoothDiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(BluetoothDiscoveryService.class);

    private static final int SEARCH_TIME = 15;

    private final Set<BluetoothAdapter> adapters = new CopyOnWriteArraySet<>();
    private final Set<BluetoothDiscoveryParticipant> participants = new CopyOnWriteArraySet<>();
    @NonNullByDefault({})
    private final Map<BluetoothAddress, DiscoveryCache> discoveryCaches = new ConcurrentHashMap<>();

    private final Set<ThingTypeUID> supportedThingTypes = new CopyOnWriteArraySet<>();

    public BluetoothDiscoveryService() {
        super(SEARCH_TIME);
        supportedThingTypes.add(BluetoothBindingConstants.THING_TYPE_BEACON);
    }

    @Override
    @Activate
    protected void activate(@Nullable Map<String, Object> configProperties) {
        logger.debug("Activating Bluetooth discovery service");
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating Bluetooth discovery service");
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.add(adapter);
        adapter.addDiscoveryListener(this);
    }

    protected void removeBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.remove(adapter);
        adapter.removeDiscoveryListener(this);
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

        // The method `removeOlderResults()` removes the Things from listeners like `Inbox`.
        // We therefore need to reset `latestSnapshot` so that the Things are notified again next time.
        // Results newer than `getTimestampOfLastScan()` will also be notified again but do not lead to duplicates.
        discoveryCaches.values().forEach(discoveryCache -> {
            discoveryCache.latestSnapshot.putValue(null);
        });
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deviceRemoved(BluetoothDevice device) {
        discoveryCaches.computeIfPresent(device.getAddress(), (addr, cache) -> cache.removeDiscoveries(device));
    }

    @Override
    public void deviceDiscovered(BluetoothDevice device) {
        logger.debug("Discovered bluetooth device '{}': {}", device.getName(), device);

        DiscoveryCache cache = Objects
                .requireNonNull(discoveryCaches.computeIfAbsent(device.getAddress(), addr -> new DiscoveryCache()));
        cache.handleDiscovery(device);
    }

    private static ThingUID createThingUIDWithBridge(DiscoveryResult result, BluetoothAdapter adapter) {
        return new ThingUID(result.getThingTypeUID(), adapter.getUID(), result.getThingUID().getId());
    }

    private static DiscoveryResult copyWithNewBridge(DiscoveryResult result, BluetoothAdapter adapter) {
        String label = result.getLabel();

        return DiscoveryResultBuilder.create(createThingUIDWithBridge(result, adapter))//
                .withBridge(adapter.getUID())//
                .withProperties(result.getProperties())//
                .withRepresentationProperty(result.getRepresentationProperty())//
                .withTTL(result.getTimeToLive())//
                .withLabel(label)//
                .build();
    }

    private class DiscoveryCache {

        private final Map<BluetoothAdapter, SnapshotFuture> discoveryFutures = new HashMap<>();
        private final Map<BluetoothAdapter, Set<DiscoveryResult>> discoveryResults = new ConcurrentHashMap<>();

        private ExpiringCache<BluetoothDeviceSnapshot> latestSnapshot = new ExpiringCache<>(Duration.ofMinutes(1),
                () -> null);

        /**
         * This is meant to be used as part of a Map.compute function
         *
         * @param device the device to remove from this cache
         * @return this DiscoveryCache if there are still snapshots, null otherwise
         */
        public synchronized @Nullable DiscoveryCache removeDiscoveries(final BluetoothDevice device) {
            // we remove any discoveries that have been published for this device
            BluetoothAdapter adapter = device.getAdapter();
            if (discoveryFutures.containsKey(adapter)) {
                @Nullable
                SnapshotFuture ssFuture = discoveryFutures.remove(adapter);
                if (ssFuture != null) {
                    ssFuture.future.thenAccept(result -> retractDiscoveryResult(adapter, result));
                }
            }
            if (discoveryFutures.isEmpty()) {
                return null;
            }
            return this;
        }

        public synchronized void handleDiscovery(BluetoothDevice device) {
            if (!discoveryFutures.isEmpty()) {
                CompletableFuture
                        // we have an ongoing futures so lets create our discovery after they all finish
                        .allOf(discoveryFutures.values().stream().map(sf -> sf.future)
                                .toArray(CompletableFuture[]::new))
                        .thenRun(() -> createDiscoveryFuture(device));
            } else {
                createDiscoveryFuture(device);
            }
        }

        private synchronized void createDiscoveryFuture(BluetoothDevice device) {
            BluetoothAdapter adapter = device.getAdapter();
            CompletableFuture<DiscoveryResult> future = null;

            BluetoothDeviceSnapshot snapshot = new BluetoothDeviceSnapshot(device);
            BluetoothDeviceSnapshot latestSnapshot = this.latestSnapshot.getValue();
            if (latestSnapshot != null) {
                snapshot.merge(latestSnapshot);

                if (snapshot.equals(latestSnapshot)) {
                    // this means that snapshot has no newer fields than the latest snapshot
                    if (discoveryFutures.containsKey(adapter)
                            && discoveryFutures.get(adapter).snapshot.equals(latestSnapshot)) {
                        // This adapter has already produced the most up-to-date result, so no further processing is
                        // necessary
                        return;
                    }

                    /*
                     * This isn't a new snapshot, but an up-to-date result from this adapter has not been produced yet.
                     * Since a result must have been produced for this snapshot, we search the results of the other
                     * adapters to find the future for the latest snapshot, then we modify it to make it look like it
                     * came from this adapter. This way we don't need to recompute the DiscoveryResult.
                     */
                    Optional<CompletableFuture<DiscoveryResult>> otherFuture = discoveryFutures.values().stream()
                            // make sure that we only get futures for the current snapshot
                            .filter(sf -> sf.snapshot.equals(latestSnapshot)).findAny().map(sf -> sf.future);
                    if (otherFuture.isPresent()) {
                        future = otherFuture.get();
                    }
                }
            }
            this.latestSnapshot.putValue(snapshot);

            if (future == null) {
                // we pass in the snapshot since it acts as a delegate for the device. It will also retain any new
                // fields added to the device as part of the discovery process.
                future = startDiscoveryProcess(snapshot);
            }

            if (discoveryFutures.containsKey(adapter)) {
                // now we need to make sure that we remove the old discovered result if it is different from the new
                // one.

                @Nullable
                SnapshotFuture oldSF = discoveryFutures.get(adapter);
                future = oldSF.future.thenCombine(future, (oldResult, newResult) -> {
                    logger.trace("\n old: {}\n new: {}", oldResult, newResult);
                    if (!oldResult.getThingUID().equals(newResult.getThingUID())) {
                        retractDiscoveryResult(adapter, oldResult);
                    }
                    return newResult;
                });
            }
            /*
             * this appends a post-process to any ongoing or completed discoveries with this device's address.
             * If this discoveryFuture is ongoing then this post-process will run asynchronously upon the future's
             * completion.
             * If this discoveryFuture is already completed then this post-process will run in the current thread.
             * We need to make sure that this is part of the future chain so that the call to 'thingRemoved'
             * in the 'removeDiscoveries' method above can be sure that it is running after the 'thingDiscovered'
             */
            future = future.thenApply(result -> {
                publishDiscoveryResult(adapter, result);
                return result;
            }).whenComplete((r, t) -> {
                if (t != null) {
                    logger.warn("Error occured during discovery of {}", device.getAddress(), t);
                }
            });

            // now save this snapshot for later
            discoveryFutures.put(adapter, new SnapshotFuture(snapshot, future));
        }

        private void publishDiscoveryResult(BluetoothAdapter adapter, DiscoveryResult result) {
            Set<DiscoveryResult> results = new HashSet<>();
            BiConsumer<BluetoothAdapter, DiscoveryResult> publisher = (a, r) -> {
                results.add(copyWithNewBridge(r, a));
            };

            publisher.accept(adapter, result);
            for (BluetoothDiscoveryParticipant participant : participants) {
                participant.publishAdditionalResults(result, publisher);
            }
            results.forEach(BluetoothDiscoveryService.this::thingDiscovered);
            discoveryResults.put(adapter, results);
        }

        /**
         * Called when a new discovery is published and thus requires the old discovery to be removed first.
         *
         * @param adapter to get the results to be removed
         * @param result unused
         */
        private void retractDiscoveryResult(BluetoothAdapter adapter, DiscoveryResult result) {
            Set<DiscoveryResult> results = discoveryResults.remove(adapter);
            if (results != null) {
                for (DiscoveryResult r : results) {
                    thingRemoved(r.getThingUID());
                }
            }
        }

        private CompletableFuture<DiscoveryResult> startDiscoveryProcess(BluetoothDeviceSnapshot device) {
            return CompletableFuture.supplyAsync(new BluetoothDiscoveryProcess(device, participants, adapters),
                    scheduler);
        }
    }

    private static class SnapshotFuture {
        public final BluetoothDeviceSnapshot snapshot;
        public final CompletableFuture<DiscoveryResult> future;

        public SnapshotFuture(BluetoothDeviceSnapshot snapshot, CompletableFuture<DiscoveryResult> future) {
            this.snapshot = snapshot;
            this.future = future;
        }
    }
}
