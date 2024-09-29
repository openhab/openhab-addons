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
package org.openhab.binding.tapocontrol.internal.discovery.dto;

import static org.openhab.binding.tapocontrol.internal.helpers.utils.TapoUtils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;

/**
 * TapoCloud DeviceList Data Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDiscoveryResultList implements Iterable<TapoDiscoveryResult> {

    @Expose
    private List<TapoDiscoveryResult> deviceList = new ArrayList<>(0);

    /* init new emty list */
    public TapoDiscoveryResultList() {
    }

    /**
     * Add device to devicelist. Overwrite fields of device with new data if already exists and field has values
     * 
     * @param result
     */
    public void addResult(TapoDiscoveryResult result) {
        TapoDiscoveryResult old = getDeviceByMac(result.deviceMac());
        if (old == null) {
            deviceList.add(result);
        } else {
            /* create new discoveryresult and overwrite empty values */
            boolean factoryDefault = compareValuesAgainstComparator(old.factoryDefault(), result.factoryDefault(),
                    false);
            boolean isSupportIOT = compareValuesAgainstComparator(old.isSupportIOT(), result.isSupportIOT(), false);
            TapoDiscoveryResult.EncryptionShema enctyptionShema = compareValuesAgainstComparator(old.encryptionShema(),
                    result.encryptionShema(), new TapoDiscoveryResult.EncryptionShema(false, "", 0, 0));
            int role = compareValuesAgainstComparator(old.role(), result.role(), 0);
            int status = compareValuesAgainstComparator(old.status(), result.status(), 0);
            String alias = compareValuesAgainstComparator(old.alias(), result.alias(), "");
            String appServerUrl = compareValuesAgainstComparator(old.alias(), result.alias(), "");
            String deviceHwVer = compareValuesAgainstComparator(old.deviceHwVer(), result.deviceHwVer(), "");
            String deviceId = compareValuesAgainstComparator(old.deviceId(), result.deviceId(), "");
            String deviceMac = compareValuesAgainstComparator(old.deviceMac(), result.deviceMac(), "");
            String deviceModel = compareValuesAgainstComparator(old.deviceModel(), result.deviceModel(), "");
            String deviceRegion = compareValuesAgainstComparator(old.deviceRegion(), result.deviceRegion(), "");
            String deviceType = compareValuesAgainstComparator(old.deviceType(), result.deviceType(), "");
            String fwId = compareValuesAgainstComparator(old.fwId(), result.fwId(), "");
            String fwVer = compareValuesAgainstComparator(old.fwVer(), result.fwVer(), "");
            String hwId = compareValuesAgainstComparator(old.hwId(), result.hwId(), "");
            String ip = compareValuesAgainstComparator(old.ip(), result.ip(), "");
            String isSameRegion = compareValuesAgainstComparator(old.isSameRegion(), result.isSameRegion(), "");
            String oemId = compareValuesAgainstComparator(old.oemId(), result.oemId(), "");

            /* add new result */
            deviceList.remove(old);
            deviceList.add(new TapoDiscoveryResult(factoryDefault, isSupportIOT, enctyptionShema, role, status, alias,
                    appServerUrl, deviceHwVer, deviceId, deviceMac, deviceModel, deviceModel, deviceRegion, deviceType,
                    fwId, fwVer, hwId, ip, isSameRegion, oemId));
        }
    }

    public void clear() {
        deviceList = new ArrayList<>(0);
    }

    /*
     * check if list contains element with mac
     */
    public boolean containsDeviceWithMac(final String mac) {
        return deviceList.stream().anyMatch(o -> mac.equals(o.deviceMac()));
    }

    /*
     * return element which contains mac
     */
    public @Nullable TapoDiscoveryResult getDeviceByMac(final String mac) {
        return deviceList.stream().filter(o -> mac.equals(o.deviceMac())).findFirst().orElse(null);
    }

    public List<TapoDiscoveryResult> deviceList() {
        return Objects.requireNonNullElse(deviceList, List.of());
    }

    @Override
    public Iterator<TapoDiscoveryResult> iterator() {
        return deviceList.iterator();
    }

    public int size() {
        return deviceList.size();
    }
}
