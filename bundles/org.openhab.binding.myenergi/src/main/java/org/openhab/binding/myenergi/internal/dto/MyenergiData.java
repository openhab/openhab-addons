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
package org.openhab.binding.myenergi.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.exception.RecordNotFoundException;

/**
 * The {@link MyenergiData} is a structure to hold cached information from the API.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class MyenergiData {

    private List<HarviSummary> harvis = new ArrayList<HarviSummary>();
    private List<ZappiSummary> zappis = new ArrayList<ZappiSummary>();
    private List<EddiSummary> eddis = new ArrayList<EddiSummary>();
    private String activeServer = "";
    private String firmwareVersion = "";

    public List<HarviSummary> getHarvis() {
        return harvis;
    }

    public synchronized void setHarvis(List<HarviSummary> harvis) {
        this.harvis = harvis;
    }

    public List<ZappiSummary> getZappis() {
        return zappis;
    }

    public synchronized void setZappis(List<ZappiSummary> zappis) {
        this.zappis = zappis;
    }

    public List<EddiSummary> getEddis() {
        return eddis;
    }

    public synchronized void setEddis(List<EddiSummary> eddis) {
        this.eddis = eddis;
    }

    public String getActiveServer() {
        return activeServer;
    }

    public void setActiveServer(String activeServer) {
        this.activeServer = activeServer;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public HarviSummary getHarviBySerialNumber(long serialNumber) throws RecordNotFoundException {
        for (HarviSummary device : harvis) {
            if (serialNumber == device.serialNumber) {
                return device;
            }
        }
        throw new RecordNotFoundException();
    }

    public ZappiSummary getZappiBySerialNumber(long zappiSerialNumber) throws RecordNotFoundException {
        for (ZappiSummary device : zappis) {
            if (zappiSerialNumber == device.serialNumber) {
                return device;
            }
        }
        throw new RecordNotFoundException();
    }

    public EddiSummary getEddiBySerialNumber(long serialNumber) throws RecordNotFoundException {
        for (EddiSummary device : eddis) {
            if (serialNumber == device.serialNumber) {
                return device;
            }
        }
        throw new RecordNotFoundException();
    }

    public void clear() {
        harvis = new ArrayList<HarviSummary>();
        zappis = new ArrayList<ZappiSummary>();
        activeServer = "";
        firmwareVersion = "";
    }

    public void addHarvi(HarviSummary device) {
        harvis.add(device);
    }

    public void addZappi(ZappiSummary device) {
        zappis.add(device);
    }

    public void addEddi(EddiSummary device) {
        eddis.add(device);
    }

    public void addAllHarvis(List<HarviSummary> list) {
        harvis.addAll(list);
    }

    public void addAllZappis(List<ZappiSummary> list) {
        zappis.addAll(list);
    }

    public void addAllEddis(List<EddiSummary> list) {
        eddis.addAll(list);
    }

    public void updateEddi(EddiSummary device) {
        try {
            eddis.remove(getEddiBySerialNumber(device.serialNumber));
        } catch (RecordNotFoundException e) {
            // we don't do anything if the device doesn't already exist in the list.
        }
        eddis.add(device);
    }

    public void updateZappi(ZappiSummary device) {
        try {
            zappis.remove(getZappiBySerialNumber(device.serialNumber));
        } catch (RecordNotFoundException e) {
            // we don't do anything if the device doesn't already exist in the list.
        }
        zappis.add(device);
    }

    public void updateHarvi(HarviSummary device) {
        try {
            harvis.remove(getHarviBySerialNumber(device.serialNumber));
        } catch (RecordNotFoundException e) {
            // we don't do anything if the device doesn't already exist in the list.
        }
        harvis.add(device);
    }

    @Override
    public String toString() {
        return "MyEnergiData [harvis=" + harvis + ", zappis=" + zappis + ", eddis=" + eddis + ", activeServer="
                + activeServer + ", firmwareVersion=" + firmwareVersion + "]";
    }
}
