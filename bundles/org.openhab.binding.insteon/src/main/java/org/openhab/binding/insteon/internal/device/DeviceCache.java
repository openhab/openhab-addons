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
package org.openhab.binding.insteon.internal.device;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.database.DatabaseCache;
import org.openhab.binding.insteon.internal.device.database.LinkDB;
import org.openhab.binding.insteon.internal.device.database.ModemDB;
import org.openhab.binding.insteon.internal.device.feature.FeatureCache;

/**
 * The {@link DeviceCache} represents a device cache
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class DeviceCache {
    private @Nullable ProductData productData;
    private @Nullable InsteonEngine engine;
    private @Nullable DatabaseCache database;
    private @Nullable Map<String, FeatureCache> features;

    public @Nullable ProductData getProductData() {
        return productData;
    }

    public InsteonEngine getInsteonEngine() {
        return Objects.requireNonNullElse(engine, InsteonEngine.UNKNOWN);
    }

    public @Nullable DatabaseCache getDatabaseCache() {
        return database;
    }

    public Map<String, FeatureCache> getFeatureCaches() {
        return Objects.requireNonNullElse(features, Collections.emptyMap());
    }

    /**
     * Loads this device cache into a device
     *
     * @param device the device to use
     */
    public void load(Device device) {
        // load device feature caches
        getFeatureCaches().forEach((name, cache) -> {
            DeviceFeature feature = device.getFeature(name);
            if (feature != null) {
                cache.load(feature);
            }
        });

        if (device instanceof InsteonDevice insteonDevice) {
            // set device insteon engine if known
            InsteonEngine engine = getInsteonEngine();
            if (engine != InsteonEngine.UNKNOWN) {
                insteonDevice.setInsteonEngine(engine);
            }

            // load device database cache if defined
            DatabaseCache database = getDatabaseCache();
            if (database != null) {
                database.load(insteonDevice.getLinkDB());
            }
        } else if (device instanceof InsteonModem insteonModem) {
            // load modem database cache if defined
            DatabaseCache database = getDatabaseCache();
            if (database != null) {
                database.load(insteonModem.getDB());
            }
        }
    }

    /**
     * Class that represents a device cache builder
     */
    public static class Builder {
        private final DeviceCache cache = new DeviceCache();

        private Builder() {
        }

        public Builder withProductData(@Nullable ProductData productData) {
            cache.productData = productData;
            return this;
        }

        public Builder withInsteonEngine(InsteonEngine engine) {
            cache.engine = engine;
            return this;
        }

        public Builder withDatabase(LinkDB linkDB) {
            cache.database = DatabaseCache.builder().withDatabaseDelta(linkDB.getDatabaseDelta())
                    .withReload(linkDB.shouldReload()).withRecords(linkDB.getRecords()).build();
            return this;
        }

        public Builder withDatabase(ModemDB modemDB) {
            cache.database = DatabaseCache.builder().withProducts(modemDB.getProducts())
                    .withRecords(modemDB.getRecords()).build();
            return this;
        }

        public Builder withFeatures(List<DeviceFeature> features) {
            cache.features = features.stream().filter(feature -> !feature.isEventFeature() && !feature.isGroupFeature())
                    .collect(Collectors.toMap(DeviceFeature::getName, feature -> FeatureCache.builder()
                            .withState(feature.getState()).withLastMsgValue(feature.getLastMsgValue()).build()));
            return this;
        }

        public DeviceCache build() {
            return cache;
        }
    }

    /**
     * Factory method for creating a device cache builder
     *
     * @return the newly created device cache builder
     */
    public static Builder builder() {
        return new Builder();
    }
}
