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
package org.openhab.binding.insteon.internal.device.database;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.ProductData;

/**
 * The {@link ModemDBEntry} holds a modem database entry for a device
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemDBEntry {
    private InsteonAddress address;
    private ModemDB modemDB;
    private @Nullable ProductData productData;
    private Set<Integer> controllers = new TreeSet<>();
    private Set<Integer> responders = new TreeSet<>();

    public ModemDBEntry(InsteonAddress address, ModemDB modemDB) {
        this.address = address;
        this.modemDB = modemDB;
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

    public List<ModemDBRecord> getRecords() {
        return modemDB.getRecords(address);
    }

    public boolean hasRecords() {
        return !getRecords().isEmpty();
    }

    public synchronized List<Integer> getControllerGroups() {
        return controllers.stream().toList();
    }

    public synchronized List<Integer> getResponderGroups() {
        return responders.stream().toList();
    }

    public synchronized void addControllerGroup(int group) {
        controllers.add(group);
    }

    public synchronized void addResponderGroup(int group) {
        responders.add(group);
    }

    public synchronized void removeControllerGroup(int group) {
        controllers.remove(group);
    }

    public synchronized void removeResponderGroup(int group) {
        responders.remove(group);
    }

    public synchronized void setProductData(ProductData productData) {
        this.productData = productData;
    }

    @Override
    public String toString() {
        String s = address + ":";
        if (controllers.isEmpty()) {
            s += " modem controls no groups";
        } else {
            s += " modem controls groups " + controllers;
        }
        if (responders.isEmpty()) {
            s += " and responds to no groups";
        } else {
            s += " and responds to groups " + responders;
        }
        return s;
    }
}
