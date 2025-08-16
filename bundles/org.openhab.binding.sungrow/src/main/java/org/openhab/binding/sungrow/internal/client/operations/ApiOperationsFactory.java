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
package org.openhab.binding.sungrow.internal.client.operations;

import java.util.List;

/**
 * @author Christian Kemper - Initial contribution
 */
public class ApiOperationsFactory {

    public static PlantList getPlantList() {
        return new PlantList();
    }

    public static DeviceList getDeviceList(String plantId) {
        return new DeviceList(plantId);
    }

    public static BasicPlantInfo getBasicPlantInfo(String deviceSerialNumber) {
        return new BasicPlantInfo(deviceSerialNumber);
    }

    public static RealtimeData getRealtimeData(List<String> serials) {
        RealtimeData realTimeData = new RealtimeData();
        realTimeData.getRequest().setSerials(serials);
        return realTimeData;
    }
}
