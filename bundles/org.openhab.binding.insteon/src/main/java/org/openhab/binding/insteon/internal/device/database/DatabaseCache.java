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
package org.openhab.binding.insteon.internal.device.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.ProductData;

/**
 * The {@link DatabaseCache} represents a database cache
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class DatabaseCache {
    private @Nullable Boolean complete;
    private @Nullable Integer delta;
    private @Nullable Boolean reload;
    private @Nullable List<DatabaseRecord> records;
    private @Nullable Map<String, ProductData> products;

    public boolean getComplete() {
        return Objects.requireNonNullElse(complete, false);
    }

    public int getDelta() {
        return Objects.requireNonNullElse(delta, -1);
    }

    public boolean getReload() {
        return Objects.requireNonNullElse(reload, false);
    }

    public List<DatabaseRecord> getRecords() {
        return Objects.requireNonNullElse(records, Collections.emptyList());
    }

    public Map<String, ProductData> getProducts() {
        return Objects.requireNonNullElse(products, Collections.emptyMap());
    }

    /**
     * Loads this database cache into a link database
     *
     * @param linkDB the link database to use
     */
    public void load(LinkDB linkDB) {
        // set link db delta if defined
        int delta = getDelta();
        if (delta != -1) {
            linkDB.setDatabaseDelta(delta);
        }

        // set link db reload if true
        boolean reload = getReload();
        if (reload) {
            linkDB.setReload(reload);
        }

        // load link db records if not empty
        List<LinkDBRecord> records = getRecords().stream().map(LinkDBRecord::new).toList();
        if (!records.isEmpty()) {
            linkDB.loadRecords(records);
        }
    }

    /**
     * Loads this database cache into a modem database
     *
     * @param modemDB the modem database to use
     */
    public void load(ModemDB modemDB) {
        // load modem db products if not empty
        Map<InsteonAddress, ProductData> products = getProducts().entrySet().stream()
                .collect(Collectors.toMap(entry -> new InsteonAddress(entry.getKey()), Map.Entry::getValue));
        if (!products.isEmpty()) {
            modemDB.loadProducts(products);
        }

        // load modem db records if not empty
        List<ModemDBRecord> records = getRecords().stream().map(ModemDBRecord::new).toList();
        if (!records.isEmpty()) {
            modemDB.loadRecords(records);
        }

        // set modem db complete if true
        boolean complete = getComplete();
        if (complete) {
            modemDB.setIsComplete(complete);
        }
    }

    /**
     * Class that represents a database cache builder
     */
    public static class Builder {
        private final DatabaseCache cache = new DatabaseCache();

        private Builder() {
        }

        public Builder withComplete(boolean complete) {
            cache.complete = complete;
            return this;
        }

        public Builder withDatabaseDelta(int delta) {
            cache.delta = delta;
            return this;
        }

        public Builder withReload(boolean reload) {
            cache.reload = reload;
            return this;
        }

        public Builder withRecords(List<? extends DatabaseRecord> records) {
            cache.records = records.stream().map(DatabaseRecord.class::cast).toList();
            return this;
        }

        public Builder withProducts(Map<InsteonAddress, ProductData> products) {
            cache.products = products.entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().toString(), Map.Entry::getValue));
            return this;
        }

        public DatabaseCache build() {
            return cache;
        }
    }

    /**
     * Factory method for creating a database cache builder
     *
     * @return the newly created database cache builder
     */
    public static Builder builder() {
        return new Builder();
    }
}
