/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.systeminfo.internal.model;

import java.math.BigDecimal;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Display;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.EdidUtil;

/**
 * This implementation of {@link SysteminfoInterface} is using the open source library OSHI to provide system
 * information. OSHI is a free JNA-based (native) Operating System and Hardware Information library for Java.
 *
 * @author Svilen Valkanov
 * @author Lyubomir Papazov - Move the initialization logic that could potentially take long time to the
 *         initializeSysteminfo method
 *
 * @see <a href="https://github.com/oshi/oshi">OSHI github repository</a>
 *
 */
public class OshiSysteminfo implements SysteminfoInterface {

    HardwareAbstractionLayer hal;

    private Logger logger = LoggerFactory.getLogger(OshiSysteminfo.class);

    // Dynamic objects (may be queried repeatedly)
    private GlobalMemory memory;
    private CentralProcessor cpu;
    private Sensors sensors;

    // Static objects, should be recreated on each request
    private OperatingSystem operatingSystem;
    private NetworkIF[] networks;
    private Display[] displays;
    private OSFileStore[] fileStores;
    private PowerSource[] powerSources;
    private HWDiskStore[] drives;

    public static final int PRECISION_AFTER_DECIMAl_SIGN = 1;

    /**
     * Some of the methods used in this constructor execute native code and require execute permissions
     *
     */
    public OshiSysteminfo() {
        logger.debug("OshiSysteminfo service is created");
    }

    @Override
    public void initializeSysteminfo() {
        logger.debug("OshiSysteminfo service starts initializing");

        SystemInfo systemInfo = new SystemInfo();
        hal = systemInfo.getHardware();

        // Doesn't need regular update, they may be queried repeatedly
        memory = hal.getMemory();
        cpu = hal.getProcessor();
        sensors = hal.getSensors();

        // Static objects, should be recreated on each request. In OSHI 4.0.0. it is planned to change this mechanism -
        // see https://github.com/oshi/oshi/issues/310
        // TODO: Once the issue is resolved in OSHI , remove unnecessary object recreations from the public get methods
        operatingSystem = systemInfo.getOperatingSystem();
        networks = hal.getNetworkIFs();
        displays = hal.getDisplays();
        fileStores = operatingSystem.getFileSystem().getFileStores();
        powerSources = hal.getPowerSources();
        drives = hal.getDiskStores();

    }

    @SuppressWarnings("null")
    private Object getDevice(Object[] devices, int index) throws DeviceNotFoundException {
        if ((devices != null) && (devices.length <= index)) {
            throw new DeviceNotFoundException("Device with index: " + index + " can not be found!");
        }
        return devices[index];
    }

    private OSProcess getProcess(int pid) throws DeviceNotFoundException {
        OSProcess process = operatingSystem.getProcess(pid);
        if (process == null) {
            throw new DeviceNotFoundException("Error while getting information for process with PID " + pid);
        }
        return process;
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
        BigDecimal processorLoadPercent = getPercentsValue(processorLoad);
        return new DecimalType(processorLoadPercent);
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
    public DecimalType getStorageTotal(int index) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        fileStores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore fileStore = (OSFileStore) getDevice(fileStores, index);
        long totalSpace = fileStore.getTotalSpace();
        totalSpace = getSizeInMB(totalSpace);
        return new DecimalType(totalSpace);
    }

    @Override
    public DecimalType getStorageAvailable(int index) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        fileStores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore fileStore = (OSFileStore) getDevice(fileStores, index);
        long freeSpace = fileStore.getUsableSpace();
        freeSpace = getSizeInMB(freeSpace);
        return new DecimalType(freeSpace);
    }

    @Override
    public DecimalType getStorageUsed(int index) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        fileStores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore fileStore = (OSFileStore) getDevice(fileStores, index);
        long totalSpace = fileStore.getTotalSpace();
        long freeSpace = fileStore.getUsableSpace();
        long usedSpace = totalSpace - freeSpace;
        usedSpace = getSizeInMB(usedSpace);
        return new DecimalType(usedSpace);
    }

    @Override
    public DecimalType getStorageAvailablePercent(int deviceIndex) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        fileStores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore fileStore = (OSFileStore) getDevice(fileStores, deviceIndex);
        long totalSpace = fileStore.getTotalSpace();
        long freeSpace = fileStore.getUsableSpace();
        if (totalSpace > 0) {
            double freePercentDecimal = (double) freeSpace / (double) totalSpace;
            BigDecimal freePercent = getPercentsValue(freePercentDecimal);
            return new DecimalType(freePercent);
        } else {
            return null;
        }
    }

    @Override
    public DecimalType getStorageUsedPercent(int deviceIndex) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        fileStores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore fileStore = (OSFileStore) getDevice(fileStores, deviceIndex);
        long totalSpace = fileStore.getTotalSpace();
        long freeSpace = fileStore.getUsableSpace();
        long usedSpace = totalSpace - freeSpace;
        if (totalSpace > 0) {
            double usedPercentDecimal = (double) usedSpace / (double) totalSpace;
            BigDecimal usedPercent = getPercentsValue(usedPercentDecimal);
            return new DecimalType(usedPercent);
        } else {
            return null;
        }
    }

    @Override
    public StringType getStorageName(int index) throws DeviceNotFoundException {
        OSFileStore fileStore = (OSFileStore) getDevice(fileStores, index);
        String name = fileStore.getName();
        return new StringType(name);
    }

    @Override
    public StringType getStorageType(int deviceIndex) throws DeviceNotFoundException {
        OSFileStore fileStore = (OSFileStore) getDevice(fileStores, deviceIndex);
        String type = fileStore.getType();
        return new StringType(type);
    }

    @Override
    public StringType getStorageDescription(int index) throws DeviceNotFoundException {
        OSFileStore fileStore = (OSFileStore) getDevice(fileStores, index);
        String description = fileStore.getDescription();
        return new StringType(description);
    }

    @Override
    public StringType getNetworkIp(int index) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        networks = hal.getNetworkIFs();
        NetworkIF netInterface = (NetworkIF) getDevice(networks, index);
        String[] ipAddresses = netInterface.getIPv4addr();
        String ipv4 = (String) getDevice(ipAddresses, 0);
        return new StringType(ipv4);
    }

    @Override
    public StringType getNetworkName(int index) throws DeviceNotFoundException {
        NetworkIF netInterface = (NetworkIF) getDevice(networks, index);
        String name = netInterface.getName();
        return new StringType(name);
    }

    @Override
    public StringType getNetworkDisplayName(int index) throws DeviceNotFoundException {
        NetworkIF netInterface = (NetworkIF) getDevice(networks, index);
        String adapterName = netInterface.getDisplayName();
        return new StringType(adapterName);
    }

    @Override
    public StringType getDisplayInformation(int index) throws DeviceNotFoundException {
        Display display = (Display) getDevice(displays, index);

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
        BigDecimal cpuTemp = new BigDecimal(sensors.getCpuTemperature());
        cpuTemp = cpuTemp.setScale(PRECISION_AFTER_DECIMAl_SIGN, BigDecimal.ROUND_HALF_UP);
        return cpuTemp.signum() == 1 ? new DecimalType(cpuTemp) : null;
    }

    @Override
    public DecimalType getSensorsCpuVoltage() {
        BigDecimal cpuVoltage = new BigDecimal(sensors.getCpuVoltage());
        cpuVoltage = cpuVoltage.setScale(PRECISION_AFTER_DECIMAl_SIGN, BigDecimal.ROUND_HALF_UP);
        return cpuVoltage.signum() == 1 ? new DecimalType(cpuVoltage) : null;
    }

    @Override
    public DecimalType getSensorsFanSpeed(int index) throws DeviceNotFoundException {
        int[] fanSpeeds = sensors.getFanSpeeds();
        int speed = (int) getDevice(ArrayUtils.toObject(fanSpeeds), index);
        return speed > 0 ? new DecimalType(speed) : null;
    }

    @Override
    public DecimalType getBatteryRemainingTime(int index) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the battery data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        powerSources = hal.getPowerSources();
        PowerSource powerSource = (PowerSource) getDevice(powerSources, index);
        double remainingTimeInSeconds = powerSource.getTimeRemaining();
        // The getTimeRemaining() method returns (-1.0) if is calculating or (-2.0) if the time is unlimited.
        BigDecimal remainingTime = getTimeInMinutes(remainingTimeInSeconds);
        return remainingTime.signum() == 1 ? new DecimalType(remainingTime) : null;
    }

    @Override
    public DecimalType getBatteryRemainingCapacity(int index) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the battery data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        powerSources = hal.getPowerSources();
        PowerSource powerSource = (PowerSource) getDevice(powerSources, index);
        double remainingCapacity = powerSource.getRemainingCapacity();
        BigDecimal remainingCapacityPercents = getPercentsValue(remainingCapacity);
        return new DecimalType(remainingCapacityPercents);
    }

    @Override
    public StringType getBatteryName(int index) throws DeviceNotFoundException {
        PowerSource powerSource = (PowerSource) getDevice(powerSources, index);
        String name = powerSource.getName();
        return new StringType(name);
    }

    @Override
    public DecimalType getMemoryAvailablePercent() {
        long availableMemory = memory.getAvailable();
        long totalMemory = memory.getTotal();
        if (totalMemory > 0) {
            double freePercentDecimal = (double) availableMemory / (double) totalMemory;
            BigDecimal freePercent = getPercentsValue(freePercentDecimal);
            return new DecimalType(freePercent);
        } else {
            return null;
        }
    }

    @Override
    public DecimalType getMemoryUsedPercent() {
        long availableMemory = memory.getAvailable();
        long totalMemory = memory.getTotal();
        long usedMemory = totalMemory - availableMemory;
        if (totalMemory > 0) {
            double usedPercentDecimal = (double) usedMemory / (double) totalMemory;
            BigDecimal usedPercent = getPercentsValue(usedPercentDecimal);
            return new DecimalType(usedPercent);
        } else {
            return null;
        }
    }

    @Override
    public StringType getDriveName(int deviceIndex) throws DeviceNotFoundException {
        HWDiskStore drive = (HWDiskStore) getDevice(drives, deviceIndex);
        String name = drive.getName();
        return new StringType(name);
    }

    @Override
    public StringType getDriveModel(int deviceIndex) throws DeviceNotFoundException {
        HWDiskStore drive = (HWDiskStore) getDevice(drives, deviceIndex);
        String model = drive.getModel();
        return new StringType(model);
    }

    @Override
    public StringType getDriveSerialNumber(int deviceIndex) throws DeviceNotFoundException {
        HWDiskStore drive = (HWDiskStore) getDevice(drives, deviceIndex);
        String serialNumber = drive.getSerial();
        return new StringType(serialNumber);
    }

    @Override
    public DecimalType getSwapTotal() {
        long swapTotal = memory.getSwapTotal();
        swapTotal = getSizeInMB(swapTotal);
        return swapTotal > 0 ? new DecimalType(swapTotal) : null;
    }

    @Override
    public DecimalType getSwapAvailable() {
        long swapTotal = memory.getSwapTotal();
        long swapUsed = memory.getSwapUsed();
        long swapAvaialble = swapTotal - swapUsed;
        swapAvaialble = getSizeInMB(swapAvaialble);
        return swapAvaialble > 0 ? new DecimalType(swapAvaialble) : null;
    }

    @Override
    public DecimalType getSwapUsed() {
        long swapTotal = memory.getSwapUsed();
        swapTotal = getSizeInMB(swapTotal);
        return swapTotal > 0 ? new DecimalType(swapTotal) : null;
    }

    @Override
    public DecimalType getSwapAvailablePercent() {
        long usedSwap = memory.getSwapUsed();
        long totalSwap = memory.getSwapTotal();
        long freeSwap = totalSwap - usedSwap;
        if (totalSwap > 0) {
            double freePercentDecimal = (double) freeSwap / (double) totalSwap;
            BigDecimal freePercent = getPercentsValue(freePercentDecimal);
            return new DecimalType(freePercent);
        } else {
            return null;
        }
    }

    @Override
    public DecimalType getSwapUsedPercent() {
        long usedSwap = memory.getSwapUsed();
        long totalSwap = memory.getSwapTotal();
        if (totalSwap > 0) {
            double usedPercentDecimal = (double) usedSwap / (double) totalSwap;
            BigDecimal usedPercent = getPercentsValue(usedPercentDecimal);
            return new DecimalType(usedPercent);
        } else {
            return null;
        }
    }

    private long getSizeInMB(long sizeInBytes) {
        return sizeInBytes /= 1024 * 1024;
    }

    private BigDecimal getPercentsValue(double decimalFraction) {
        BigDecimal result = new BigDecimal(decimalFraction * 100);
        result = result.setScale(PRECISION_AFTER_DECIMAl_SIGN, BigDecimal.ROUND_HALF_UP);
        return result;
    }

    private BigDecimal getTimeInMinutes(double timeInSeconds) {
        BigDecimal timeInMinutes = new BigDecimal(timeInSeconds / 60);
        timeInMinutes = timeInMinutes.setScale(PRECISION_AFTER_DECIMAl_SIGN, BigDecimal.ROUND_UP);
        return timeInMinutes;
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public DecimalType getCpuLoad1() {
        BigDecimal avarageCpuLoad = getAvarageCpuLoad(1);
        return avarageCpuLoad.signum() == -1 ? null : new DecimalType(avarageCpuLoad);
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public DecimalType getCpuLoad5() {
        BigDecimal avarageCpuLoad = getAvarageCpuLoad(5);
        return avarageCpuLoad.signum() == -1 ? null : new DecimalType(avarageCpuLoad);
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public DecimalType getCpuLoad15() {
        BigDecimal avarageCpuLoad = getAvarageCpuLoad(15);
        return avarageCpuLoad.signum() == -1 ? null : new DecimalType(avarageCpuLoad);
    }

    private BigDecimal getAvarageCpuLoad(int timeInMunutes) {
        // This paramater is specified in OSHI Javadoc
        int index;
        switch (timeInMunutes) {
            case 1:
                index = 0;
                break;
            case 5:
                index = 1;
                break;
            case 15:
                index = 2;
                break;
            default:
                index = 2;
        }
        double processorLoads[] = cpu.getSystemLoadAverage(index + 1);
        BigDecimal result = new BigDecimal(processorLoads[index]);
        result = result.setScale(PRECISION_AFTER_DECIMAl_SIGN, BigDecimal.ROUND_HALF_UP);
        return result;
    }

    @Override
    public DecimalType getCpuUptime() {
        long seconds = cpu.getSystemUptime();
        return new DecimalType(getTimeInMinutes(seconds));
    }

    @Override
    public DecimalType getCpuThreads() {
        int threadCount = operatingSystem.getThreadCount();
        return new DecimalType(threadCount);
    }

    @Override
    public StringType getNetworkMac(int networkIndex) throws DeviceNotFoundException {
        NetworkIF network = (NetworkIF) getDevice(networks, networkIndex);
        String mac = network.getMacaddr();
        return new StringType(mac);
    }

    @Override
    public DecimalType getNetworkPacketsReceived(int networkIndex) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        networks = hal.getNetworkIFs();
        NetworkIF network = (NetworkIF) getDevice(networks, networkIndex);
        network.updateNetworkStats();
        long packRecv = network.getPacketsRecv();
        return new DecimalType(packRecv);
    }

    @Override
    public DecimalType getNetworkPacketsSent(int networkIndex) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        networks = hal.getNetworkIFs();
        NetworkIF network = (NetworkIF) getDevice(networks, networkIndex);
        network.updateNetworkStats();
        long packSent = network.getPacketsSent();
        return new DecimalType(packSent);
    }

    @Override
    public DecimalType getNetworkDataSent(int networkIndex) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        networks = hal.getNetworkIFs();
        NetworkIF network = (NetworkIF) getDevice(networks, networkIndex);
        network.updateNetworkStats();
        long bytesSent = network.getBytesSent();
        return new DecimalType(getSizeInMB(bytesSent));
    }

    @Override
    public DecimalType getNetworkDataReceived(int networkIndex) throws DeviceNotFoundException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        networks = hal.getNetworkIFs();
        NetworkIF network = (NetworkIF) getDevice(networks, networkIndex);
        network.updateNetworkStats();
        long bytesRecv = network.getBytesRecv();
        return new DecimalType(getSizeInMB(bytesRecv));
    }

    @Override
    public StringType getProcessName(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            String name = process.getName();
            return new StringType(name);
        } else {
            return null;
        }
    }

    @Override
    public DecimalType getProcessCpuUsage(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            double cpuUsageRaw = (process.getKernelTime() + process.getUserTime()) / process.getUpTime();
            BigDecimal cpuUsage = getPercentsValue(cpuUsageRaw);
            return new DecimalType(cpuUsage);
        } else {
            return null;
        }
    }

    @Override
    public DecimalType getProcessMemoryUsage(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            long memortInBytes = process.getResidentSetSize();
            long memoryInMB = getSizeInMB(memortInBytes);
            return new DecimalType(memoryInMB);
        } else {
            return null;
        }
    }

    @Override
    public StringType getProcessPath(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            String path = process.getPath();
            return new StringType(path);
        } else {
            return null;
        }
    }

    @Override
    public DecimalType getProcessThreads(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            int threadCount = process.getThreadCount();
            return new DecimalType(threadCount);
        } else {
            return null;
        }
    }

}
