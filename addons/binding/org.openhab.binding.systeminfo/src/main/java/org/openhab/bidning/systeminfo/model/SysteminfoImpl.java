package org.openhab.bidning.systeminfo.model;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Display;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.util.EdidUtil;

public class SysteminfoImpl implements SysteminfoInterface {
    OperatingSystem operatingSystem;
    HardwareAbstractionLayer hal;
    Enumeration<NetworkInterface> nets;
    NetworkInterface netInterface;

    public SysteminfoImpl() {
        SystemInfo systemInfo = new SystemInfo();
        operatingSystem = systemInfo.getOperatingSystem();
        hal = systemInfo.getHardware();
        try {
            nets = NetworkInterface.getNetworkInterfaces();
            netInterface = nets.nextElement();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        String name = hal.getProcessor().getName();
        return new StringType(name);
    }

    @Override
    public StringType getCpuDescription() {
        CentralProcessor processor = hal.getProcessor();
        String model = processor.getModel();
        String family = processor.getFamily();
        String serialNumber = processor.getSystemSerialNumber();
        String identifier = processor.getIdentifier();
        String vendor = processor.getVendor();
        String architecture = processor.isCpu64bit() ? "64 bit" : "32 bit";
        String descriptionFormatString = "Model: %s %s,family: %s, vendor: %s, sn: %s, identifier: %s ";
        String description = String.format(descriptionFormatString, model, architecture, family, vendor, serialNumber,
                identifier);

        return new StringType(description);
    }

    @Override
    public DecimalType getCpuLogicalProcCount() {
        int logicalProcessorCount = hal.getProcessor().getLogicalProcessorCount();
        return new DecimalType(logicalProcessorCount);
    }

    @Override
    public DecimalType getCpuPhysicalProcCount() {
        int physicalProcessorCount = hal.getProcessor().getPhysicalProcessorCount();
        return new DecimalType(physicalProcessorCount);
    }

    @Override
    public DecimalType getCpuLoad() {
        double processorLoad = hal.getProcessor().getSystemCpuLoad();
        processorLoad *= 100;
        return new DecimalType(processorLoad);
    }

    @Override
    public DecimalType getMemoryTotal() {
        long totalMemory = hal.getMemory().getTotal();
        totalMemory = getSizeInMB(totalMemory);
        return new DecimalType(totalMemory);
    }

    @Override
    public DecimalType getMemoryAvailable() {
        long availableMemory = hal.getMemory().getAvailable();
        availableMemory = getSizeInMB(availableMemory);
        return new DecimalType(availableMemory);
    }

    @Override
    public DecimalType getMemoryUsed() {
        long totalMemory = hal.getMemory().getTotal();
        long availableMemory = hal.getMemory().getAvailable();
        long usedMemory = totalMemory - availableMemory;
        usedMemory = getSizeInMB(usedMemory);
        return null;
    }

    @Override
    public DecimalType getStorageTotal(int index) {
        long totalSpace = hal.getFileStores()[index].getTotalSpace();
        totalSpace = getSizeInMB(totalSpace);
        return new DecimalType(totalSpace);
    }

    @Override
    public DecimalType getStorageAvailable(int index) {
        long freeSpace = hal.getFileStores()[index].getUsableSpace();
        freeSpace = getSizeInMB(freeSpace);
        return new DecimalType(freeSpace);
    }

    @Override
    public DecimalType getStorageUsed(int index) {
        long totalSpace = hal.getFileStores()[index].getTotalSpace();
        long freeSpace = hal.getFileStores()[index].getUsableSpace();
        long usedSpace = totalSpace - freeSpace;
        usedSpace = getSizeInMB(usedSpace);
        return new DecimalType(usedSpace);
    }

    @Override
    public StringType getStorageName(int index) {
        String name = hal.getFileStores()[index].getName();
        return new StringType(name);
    }

    @Override
    public StringType getStorageDescription(int index) {
        String description = hal.getFileStores()[index].getDescription();
        return new StringType(description);
    }

    @Override
    public StringType getNetworkIP(int index) {

        InetAddress adapterName = netInterface.getInetAddresses().nextElement();
        return new StringType(adapterName.getHostAddress());
    }

    @Override
    public StringType getNetworkName(int index) {

        String name = netInterface.getName();
        return new StringType(name);
    }

    @Override
    public StringType getNetworkAdapterName(int index) {
        // TODO index is not working as expected

        String adapterName = netInterface.getDisplayName();
        return new StringType(adapterName);
    }

    @Override
    public StringType getDisplayInfo(int index) {
        Display display = hal.getDisplays()[index];

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
    public DecimalType getSensorCpuTemp() {
        double cpuTemp = hal.getSensors().getCpuTemperature();
        return new DecimalType(cpuTemp);
    }

    @Override
    public DecimalType getSensorCpuVoltage() {
        double cpuVoltage = hal.getSensors().getCpuVoltage();
        return new DecimalType(cpuVoltage);
    }

    @Override
    public DecimalType getSensorFanSpeed(int index) {
        int speed = hal.getSensors().getFanSpeeds()[index];
        return new DecimalType(speed);
    }

    @Override
    public DecimalType getBatteryRemainingTime(int index) {
        double remainingTime = hal.getPowerSources()[index].getTimeRemaining();
        // TODO add comments
        if (remainingTime > 0) {
            remainingTime /= 60;
        } else if (remainingTime == -2 || remainingTime == -1) {
            remainingTime = 9999;
        }

        return new DecimalType(remainingTime);
    }

    @Override
    public DecimalType getBatteryRemainingCapacity(int index) {
        double remainingCapacity = hal.getPowerSources()[index].getRemainingCapacity();
        return new DecimalType(remainingCapacity);
    }

    @Override
    public StringType getBatteryName(int index) {
        String name = hal.getPowerSources()[index].getName();
        return new StringType(name);
    }

    private long getSizeInMB(long sizeInBytes) {
        return sizeInBytes /= 1024 * 1024;
    }
}
