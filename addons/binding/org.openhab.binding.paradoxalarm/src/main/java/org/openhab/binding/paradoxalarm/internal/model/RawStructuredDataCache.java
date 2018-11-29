/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link RawStructuredDataCache} This is intermediate singleton object where the raw data retrieved from
 * communicators is stored. Write access is used from communicator, read access is used from the model.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class RawStructuredDataCache {

    private List<byte[]> partitionStateFlags = new ArrayList<>();
    private ZoneStateFlags zoneStateFlags = new ZoneStateFlags();
    private List<String> partitionLabels = new ArrayList<>();
    private List<String> zoneLabels = new ArrayList<>();
    private byte[] panelInfoBytes = new byte[0];

    private static RawStructuredDataCache instance;

    private RawStructuredDataCache() {

    }

    public static RawStructuredDataCache getInstance() {
        synchronized (RawStructuredDataCache.class) {
            if (instance == null) {
                instance = new RawStructuredDataCache();
            }
            return instance;
        }
    }

    public List<byte[]> getPartitionStateFlags() {
        return partitionStateFlags;
    }

    public void setPartitionStateFlags(List<byte[]> currentPartitionFlags) {
        this.partitionStateFlags = currentPartitionFlags;
    }

    public ZoneStateFlags getZoneStateFlags() {
        return zoneStateFlags;
    }

    public void setZoneStateFlags(ZoneStateFlags zoneStateFlags) {
        this.zoneStateFlags = zoneStateFlags;
    }

    public List<String> getPartitionLabels() {
        return partitionLabels;
    }

    public void setPartitionLabels(List<String> partitionLabels) {
        this.partitionLabels = partitionLabels;
    }

    public List<String> getZoneLabels() {
        return zoneLabels;
    }

    public void setZoneLabels(List<String> zoneLabels) {
        this.zoneLabels = zoneLabels;
    }

    public byte[] getPanelInfoBytes() {
        return panelInfoBytes;
    }

    public void setPanelInfoBytes(byte[] panelInfoBytes) {
        this.panelInfoBytes = panelInfoBytes;
    }
}
