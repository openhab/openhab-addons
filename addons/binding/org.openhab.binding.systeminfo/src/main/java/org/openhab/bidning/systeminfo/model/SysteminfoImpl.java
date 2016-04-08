/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.bidning.systeminfo.model;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Display;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.EdidUtil;

/**
 * This implementation of {@link SysteminfoInterface} is using the open source library Oshi to provide all information
 * except the network information, which is provided from JDK.
 *
 * @author Svilen Valkanov
 *
 */
public class SysteminfoImpl implements SysteminfoInterface {
    private OperatingSystem operatingSystem;
    private ArrayList<NetworkInterface> networks;
    private Display[] displays;
    private OSFileStore[] fileStores;
    private GlobalMemory memory;
    private PowerSource[] powerSources;
    private CentralProcessor cpu;

    private Sensors sensors;

    /**
     *
     * @throws SocketException when it is not able to access the network information.
     */
    public SysteminfoImpl(SystemInfo systemInfo) throws SocketException {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        operatingSystem = systemInfo.getOperatingSystem();
        displays = hal.getDisplays();
        fileStores = hal.getFileStores();
        memory = hal.getMemory();
        powerSources = hal.getPowerSources();
        cpu = hal.getProcessor();
        sensors = hal.getSensors();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        if (networkInterfaces != null) {
            networks = Collections.list(networkInterfaces);
        }

    }

    @Override
    public StringType getOsFamily() {
        String osFamily = operatingSystem.getFamily();
        return new StringType(osFamily);
    }

    @Override
    public StringType getOsManufacturer() {
        String osManufacturer = operatingSystem.getManufacturer();
        return new StringType(osManufacturer);
    }

    @Override
    public StringType getOsVersion() {
        String osVersion = operatingSystem.getVersion().toString();
        return new StringType(osVersion);
    }

    @Override
    public StringType getCpuName() {
        String name = cpu.getName();
        return new StringType(name);
    }

    @Override
    public StringType getCpuDescription() {
        String model = cpu.getModel();
        String family = cpu.getFamily();
        String serialNumber = cpu.getSystemSerialNumber();
        String identifier = cpu.getIdentifier();
        String vendor = cpu.getVendor();
        String architecture = cpu.isCpu64bit() ? "64 bit" : "32 bit";
        String descriptionFormatString = "Model: %s %s,family: %s, vendor: %s, sn: %s, identifier: %s ";
        String description = String.format(descriptionFormatString, model, architecture, family, vendor, serialNumber,
                identifier);

        return new StringType(description);
    }

    @Override
    public DecimalType getCpuLogicalCores() {
        int logicalProcessorCount = cpu.getLogicalProcessorCount();
        return new DecimalType(logicalProcessorCount);
    }

    @Override
    public DecimalType getCpuPhysicalCores() {
        int physicalProcessorCount = cpu.getPhysicalProcessorCount();
        return new DecimalType(physicalProcessorCount);
    }

    @Override
    public DecimalType getCpuLoad() {
        double processorLoad = cpu.getSystemCpuLoad();
        processorLoad = getPercentsValue(processorLoad);
        return new DecimalType(processorLoad);
    }

    @Override
    public DecimalType getMemoryTotal() {
        long totalMemory = memory.getTotal();
        totalMemory = getSizeInMB(totalMemory);
        return new DecimalType(totalMemory);
    }

    @Override
    public DecimalType getMemoryAvailable() {
        long availableMemory = memory.getAvailable();
        availableMemory = getSizeInMB(availableMemory);
        return new DecimalType(availableMemory);
    }

    @Override
    public DecimalType getMemoryUsed() {
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;
        usedMemory = getSizeInMB(usedMemory);
        return new DecimalType(usedMemory);
    }

    @Override
    public DecimalType getStorageTotal(int index) {
        if (fileStores.length <= index) {
            return null;
        }
        long totalSpace = fileStores[index].getTotalSpace();
        totalSpace = getSizeInMB(totalSpace);
        return new DecimalType(totalSpace);
    }

    @Override
    public DecimalType getStorageAvailable(int index) {
        if (fileStores.length <= index) {
            return null;
        }
        long freeSpace = fileStores[index].getUsableSpace();
        freeSpace = getSizeInMB(freeSpace);
        return new DecimalType(freeSpace);
    }

    @Override
    public DecimalType getStorageUsed(int index) {
        if (fileStores.length <= index) {
            return null;
        }
        long totalSpace = fileStores[index].getTotalSpace();
        long freeSpace = fileStores[index].getUsableSpace();
        long usedSpace = totalSpace - freeSpace;
        usedSpace = getSizeInMB(usedSpace);
        return new DecimalType(usedSpace);
    }

    @Override
    public StringType getStorageName(int index) {
        if (fileStores.length <= index) {
            return null;
        }
        String name = fileStores[index].getName();
        return new StringType(name);
    }

    @Override
    public StringType getStorageDescription(int index) {
        if (fileStores.length <= index) {
            return null;
        }
        String description = fileStores[index].getDescription();
        return new StringType(description);
    }

    @Override
    public StringType getNetworkIp(int index) {
        if (networks != null && networks.size() <= index) {
            return null;
        }
        NetworkInterface netInterface = networks.get(index);
        InetAddress adapterName = netInterface.getInetAddresses().nextElement();
        return new StringType(adapterName.getHostAddress());
    }

    @Override
    public StringType getNetworkName(int index) {
        if (networks != null && networks.size() <= index) {
            return null;
        }
        NetworkInterface netInterface = networks.get(index);
        String name = netInterface.getName();
        return new StringType(name);
    }

    @Override
    public StringType getNetworkAdapterName(int index) {
        if (networks != null && networks.size() <= index) {
            return null;
        }
        NetworkInterface netInterface = networks.get(index);
        String adapterName = netInterface.getDisplayName();
        return new StringType(adapterName);
    }

    @Override
    public StringType getDisplayInformation(int index) {
        if (displays.length <= index) {
            return null;
        }
        Display display = displays[index];

        byte[] edid = display.getEdid();
        String manufacturer = EdidUtil.getManufacturerID(edid);
        String product = EdidUtil.getProductID(edid);
        String serialNumber = EdidUtil.getSerialNo(edid);
        int width = EdidUtil.getHcm(edid);
        int height = EdidUtil.getVcm(edid);

        String edidFormatString = "Product %s, manufacturer %s, SN: %s, Width: %d, Height: %d";
        String edidInfo = String.format(edidFormatString, product, manufacturer, serialNumber, width, height);
        return new StringType(edidInfo);
    }

    @Override
    public DecimalType getSensorsCpuTemperature() {
        double cpuTemp = sensors.getCpuTemperature();
        return new DecimalType(cpuTemp);
    }

    @Override
    public DecimalType getSensorsCpuVoltage() {
        double cpuVoltage = sensors.getCpuVoltage();
        return new DecimalType(cpuVoltage);
    }

    @Override
    public DecimalType getSensorsFanSpeed(int index) {
        int[] fanSpeeds = sensors.getFanSpeeds();
        if (fanSpeeds.length <= index) {
            return null;
        }
        int speed = fanSpeeds[index];
        return new DecimalType(speed);
    }

    @Override
    public DecimalType getBatteryRemainingTime(int index) {
        if (powerSources.length < index) {
            return null;
        }
        double remainingTime = powerSources[index].getTimeRemaining();
        // The getTimeRemaining() method returns (-1.0) if is calculating or (-2.0) if the time is unlimited
        if (remainingTime > 0) {
            remainingTime = getTimeInMinutes(remainingTime);
        } else if (remainingTime == -2 || remainingTime == -1) {
            remainingTime = 999;
        }

        return new DecimalType(remainingTime);
    }

    @Override
    public DecimalType getBatteryRemainingCapacity(int index) {
        if (powerSources.length < index) {
            return null;
        }
        double remainingCapacity = powerSources[index].getRemainingCapacity();
        remainingCapacity = getPercentsValue(remainingCapacity);
        return new DecimalType(remainingCapacity);
    }

    @Override
    public StringType getBatteryName(int index) {
        if (powerSources.length < index) {
            return null;
        }
        String name = powerSources[index].getName();
        return new StringType(name);
    }

    private long getSizeInMB(long sizeInBytes) {
        return sizeInBytes /= 1024 * 1024;
    }

    private double getPercentsValue(double decimalFraction) {
        return decimalFraction * 100;
    }

    private double getTimeInMinutes(double timeInSeconds) {
        return timeInSeconds / 100;
    }

    @Override
    public DecimalType getMemoryAvailablePercent() {
        long availableMemory = memory.getAvailable();
        long totalMemory = memory.getTotal();
        double freePercentDecimal = (double) availableMemory / (double) totalMemory;
        double freePercent = getPercentsValue(freePercentDecimal);
        return new DecimalType(freePercent);
    }

    @Override
    public DecimalType getStorageAvailablePercent(int deviceIndex) {
        if (fileStores.length <= deviceIndex) {
            return null;
        }
        long freeStorage = fileStores[deviceIndex].getUsableSpace();
        long totalStorage = fileStores[deviceIndex].getTotalSpace();
        double freePercentDecimal = (double) freeStorage / (double) totalStorage;
        double freePercent = getPercentsValue(freePercentDecimal);
        return new DecimalType(freePercent);
    }

}
