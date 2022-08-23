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
package org.openhab.binding.insteon.internal.device.database;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.ProductData;

/**
 * The ModemDBEntry class holds a modem device entry
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemDBEntry {
    private InsteonAddress address;
    private @Nullable ProductData productData;
    private Set<ModemDBRecord> records = new LinkedHashSet<>();

    public ModemDBEntry(InsteonAddress address) {
        this.address = address;
    }

    public InsteonAddress getAddress() {
        return address;
    }

    public String getId() {
        return address.toString();
    }

    public @Nullable ProductData getProductData() {
        return productData;
    }

    public boolean hasProductData() {
        return productData != null;
    }

    public void setProductData(ProductData productData) {
        this.productData = productData;
    }

    public List<ModemDBRecord> getRecords() {
        return records.stream().collect(Collectors.toList());
    }

    public List<Integer> getControllerGroups() {
        return getRecords().stream().filter(ModemDBRecord::isController).map(ModemDBRecord::getGroup)
                .map(Integer::valueOf).collect(Collectors.toList());
    }

    public List<Integer> getResponderGroups() {
        return getRecords().stream().filter(ModemDBRecord::isResponder).map(ModemDBRecord::getGroup)
                .map(Integer::valueOf).collect(Collectors.toList());
    }

    public void addRecord(ModemDBRecord record) {
        records.add(record);
    }

    public void deleteRecord(ModemDBRecord record) {
        records.remove(record);
    }

    @Override
    public String toString() {
        String s = address + ":";
        List<Integer> controllers = getControllerGroups();
        if (controllers.isEmpty()) {
            s += " modem controls no groups";
        } else {
            s += " modem controls groups (" + toGroupString(controllers) + ")";
        }
        List<Integer> responders = getResponderGroups();
        if (responders.isEmpty()) {
            s += " and responds to no groups";
        } else {
            s += " and responds to groups (" + toGroupString(responders) + ")";
        }
        return s;
    }

    private String toGroupString(List<Integer> groups) {
        return groups.stream().map(String::valueOf).sorted().collect(Collectors.joining(","));
    }
}
