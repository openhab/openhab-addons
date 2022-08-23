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
package org.openhab.binding.insteon.internal.device;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.database.LinkDBRecord;

/**
 * Class that represents a device cache
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class DeviceCache {
    private @Nullable ProductData productData;
    private @Nullable InsteonEngine engine;
    private @Nullable Integer databaseDelta;
    private @Nullable List<LinkDBRecord> records;
    private @Nullable Map<String, FeatureCache> features;

    public DeviceCache(@Nullable ProductData productData, @Nullable InsteonEngine engine,
            @Nullable Integer databaseDelta, @Nullable List<LinkDBRecord> records,
            @Nullable Map<String, FeatureCache> features) {
        this.productData = productData;
        this.engine = engine;
        this.databaseDelta = databaseDelta;
        this.records = records;
        this.features = features;
    }

    public @Nullable ProductData getProductData() {
        return productData;
    }

    public @Nullable InsteonEngine getInsteonEngine() {
        return engine;
    }

    public @Nullable Integer getDatabaseDelta() {
        return databaseDelta;
    }

    public @Nullable List<LinkDBRecord> getDatabaseRecords() {
        return records;
    }

    public @Nullable Map<String, FeatureCache> getFeatureCaches() {
        return features;
    }

    /**
     * Factory method for creating a DeviceCache from an InsteonDevice
     *
     * @param device the device
     * @return the newly created DeviceCache
     */
    public static DeviceCache create(InsteonDevice device) {
        ProductData productData = device.getProductData();
        InsteonEngine engine = device.getInsteonEngine();
        int databaseDelta = device.getLinkDB().getDatabaseDelta();
        List<LinkDBRecord> records = device.getLinkDB().getRecords();
        Map<String, FeatureCache> features = device.getFeatures().stream()
                .filter(feature -> !feature.isEventFeature() && !feature.isGroupFeature())
                .collect(Collectors.toMap(DeviceFeature::getName, feature -> FeatureCache.create(feature)));

        return new DeviceCache(productData, engine, databaseDelta, records, features);
    }

    /**
     * Factory method for loading a DeviceCache into an InsteonDevice
     *
     * @param cache the device cache to load
     * @param device the device
     */
    public static void load(DeviceCache cache, InsteonDevice device) {
        // set device insteon engine if known
        InsteonEngine engine = cache.getInsteonEngine();
        if (engine != null && engine != InsteonEngine.UNKNOWN) {
            device.setInsteonEngine(engine);
        }

        // set device link db delta if defined
        Integer databaseDelta = cache.getDatabaseDelta();
        if (databaseDelta != null && databaseDelta != -1) {
            device.getLinkDB().setDatabaseDelta(databaseDelta);
        }

        // load device link db records if not empty
        List<LinkDBRecord> records = cache.getDatabaseRecords();
        if (records != null && !records.isEmpty()) {
            device.getLinkDB().loadRecords(records);
            device.getLinkDB().updateStatus();
            device.getLinkDB().logRecords();
        }

        // load device feature caches if defined
        Map<String, FeatureCache> features = cache.getFeatureCaches();
        if (features != null) {
            for (DeviceFeature feature : device.getFeatures()) {
                FeatureCache featureCache = features.get(feature.getName());
                if (featureCache != null) {
                    FeatureCache.load(featureCache, feature);
                }
            }
        }
    }
}
