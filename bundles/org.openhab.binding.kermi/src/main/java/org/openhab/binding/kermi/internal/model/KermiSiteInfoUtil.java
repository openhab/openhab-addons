/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.kermi.internal.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.openhab.binding.kermi.internal.KermiBindingConstants;
import org.openhab.binding.kermi.internal.KermiCommunicationException;
import org.openhab.binding.kermi.internal.api.Datapoint;
import org.openhab.binding.kermi.internal.api.DeviceInfo;
import org.openhab.binding.kermi.internal.api.KermiHttpUtil;
import org.openhab.binding.kermi.internal.api.MenuEntry;
import org.openhab.binding.kermi.internal.api.MenuEntryResponse;
import org.openhab.binding.kermi.internal.api.MenuGetChildEntriesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;

import tech.units.indriya.unit.Units;

/**
 * @author Marco Descher - Initial contribution
 */
public class KermiSiteInfoUtil {

    private static Logger logger = LoggerFactory.getLogger(KermiSiteInfoUtil.class);

    /**
     * Collects the datapoints for a given device, if a cache file is available, the cache is loaded
     *
     * @param httpUtil
     * @param deviceInfo
     * @return
     * @throws KermiCommunicationException
     * @throws IOException
     * @throws JsonIOException
     */
    public static List<Datapoint> collectDeviceDatapoints(KermiHttpUtil httpUtil, DeviceInfo deviceInfo)
            throws KermiCommunicationException {

        List<Datapoint> dataPoints = loadDeviceDatapointsCache(deviceInfo);
        if (dataPoints == null) {
            logger.info("Collecting Datapoints for Device {}", deviceInfo.getDeviceId());
            MenuGetChildEntriesResponse rootResponse = httpUtil.getMenuChildEntries(deviceInfo.getDeviceId(),
                    KermiBindingConstants.DEVICE_ID_HEATPUMP_MANAGER);
            MenuEntryResponse rootChildEntry = rootResponse.getResponseData();

            dataPoints = new ArrayList<Datapoint>();
            collectAndTraverse(httpUtil, deviceInfo.getDeviceId(), dataPoints, rootChildEntry);
            storeDeviceDatapointsCache(deviceInfo, dataPoints);
        }

        return dataPoints;
    }

    private static void storeDeviceDatapointsCache(DeviceInfo deviceInfo, List<Datapoint> dataPoints)
            throws KermiCommunicationException {
        File file = new File(KermiBindingConstants.getKermiUserDataFolder(),
                deviceInfo.getDeviceId() + "-" + deviceInfo.getSerial().trim() + ".json");

        ListDatapointCacheFile listDatapointCacheFile = new ListDatapointCacheFile();
        listDatapointCacheFile.setDeviceId(deviceInfo.getDeviceId());
        listDatapointCacheFile.setSerial(deviceInfo.getSerial());
        listDatapointCacheFile.setAddress(deviceInfo.getAddress());
        listDatapointCacheFile.setName(deviceInfo.getName());

        // clean the values, we don't need them
        List<Datapoint> datapointsWithoutValues = dataPoints.stream().map(dp -> {
            dp.setDatapointValue(null);
            return dp;
        }).collect(Collectors.toList());
        listDatapointCacheFile.setDatapoints(datapointsWithoutValues);
        try (FileWriter filewriter = new FileWriter(file, StandardCharsets.UTF_8)) {
            new Gson().toJson(listDatapointCacheFile, filewriter);
        } catch (JsonIOException | IOException e) {
            throw new KermiCommunicationException(e);
        }
    }

    private static List<Datapoint> loadDeviceDatapointsCache(DeviceInfo deviceInfo) {
        File file = new File(KermiBindingConstants.getKermiUserDataFolder(),
                deviceInfo.getDeviceId() + "-" + deviceInfo.getSerial().trim() + ".json");
        if (file.exists()) {
            try (JsonReader reader = new JsonReader(new FileReader(file, StandardCharsets.UTF_8))) {
                logger.debug("Loading cached datapoints for device {}", deviceInfo.getDeviceId());
                ListDatapointCacheFile cacheFile = new Gson().fromJson(reader, ListDatapointCacheFile.class);
                return cacheFile.getDatapoints();
            } catch (IOException e) {
                logger.warn("Error loading device datapoint cache file", e);
            }
        }

        return null;
    }

    private static void collectAndTraverse(KermiHttpUtil httpUtil, String deviceId, List<Datapoint> dataPoints,
            MenuEntryResponse menuEntry) throws KermiCommunicationException {
        if (!menuEntry.getBundles().isEmpty()) {
            menuEntry.getBundles().forEach(bundle -> dataPoints.addAll(bundle.getDatapoints()));
        }
        List<MenuEntry> menuEntries = menuEntry.getMenuEntries();
        for (MenuEntry me : menuEntries) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                // just some throttling
            }
            MenuGetChildEntriesResponse menuChildEntry = httpUtil.getMenuChildEntries(deviceId, me.getMenuEntryId());
            collectAndTraverse(httpUtil, deviceId, dataPoints, menuChildEntry.getResponseData());
        }
    }

    /**
     *
     * @param unitString
     * @return <code>null</code> if unit could not be determined
     */
    public static Unit<?> determineUnitByString(String unitString) {
        if ("kW".equals(unitString)) {
            return Units.WATT;
        } else if ("°C".equals(unitString)) {
            return Units.CELSIUS;
        }
        return null;
    }
}
