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
package org.openhab.binding.systeminfo.internal.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.dimension.DataAmount;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
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
 * @author Svilen Valkanov - Initial contribution
 * @author Lyubomir Papazov - Move the initialization logic that could potentially take long time to the
 *         initializeSysteminfo method
 * @author Christoph Weitkamp - Update to OSHI 3.13.0 - Replaced deprecated method
 *         CentralProcessor#getSystemSerialNumber()
 * @author Wouter Born - Update to OSHI 4.0.0 and add null annotations
 * @author Mark Herwege - Add dynamic creation of extra channels
 * @author Mark Herwege - Use units of measure
 *
 * @see <a href="https://github.com/oshi/oshi">OSHI GitHub repository</a>
 */
@NonNullByDefault
@Component(service = SysteminfoInterface.class)
public class OSHISysteminfo implements SysteminfoInterface {

    private final Logger logger = LoggerFactory.getLogger(OSHISysteminfo.class);

    private @NonNullByDefault({}) HardwareAbstractionLayer hal;

    // Dynamic objects (may be queried repeatedly)
    private @NonNullByDefault({}) GlobalMemory memory;
    private @NonNullByDefault({}) CentralProcessor cpu;
    private @NonNullByDefault({}) Sensors sensors;

    // Static objects, should be recreated on each request
    private @NonNullByDefault({}) ComputerSystem computerSystem;
    private @NonNullByDefault({}) OperatingSystem operatingSystem;
    private @NonNullByDefault({}) List<NetworkIF> networks;
    private @NonNullByDefault({}) List<Display> displays;
    private @NonNullByDefault({}) List<OSFileStore> fileStores;
    private @NonNullByDefault({}) List<PowerSource> powerSources;
    private @NonNullByDefault({}) List<HWDiskStore> drives;

    // Array containing cpu tick info to calculate CPU load, according to oshi doc:
    // 8 long values representing time spent in User, Nice, System, Idle, IOwait, IRQ, SoftIRQ, and Steal states
    private long[] ticks = new long[8];
    // Map containing previous process state to calculate load by process
    private Map<Integer, OSProcess> processTicks = new HashMap<>();

    public static final int PRECISION_AFTER_DECIMAL_SIGN = 1;

    /**
     * Some of the methods used in this constructor execute native code and require execute permissions
     *
     */
    public OSHISysteminfo() {
        logger.debug("OSHISysteminfo service is created");
    }

    @Override
    public void initializeSysteminfo() {
        logger.debug("OSHISysteminfo service starts initializing");

        SystemInfo systemInfo = new SystemInfo();
        hal = systemInfo.getHardware();

        // Doesn't need regular update, they may be queried repeatedly
        memory = hal.getMemory();
        cpu = hal.getProcessor();
        sensors = hal.getSensors();

        computerSystem = hal.getComputerSystem();
        operatingSystem = systemInfo.getOperatingSystem();
        networks = hal.getNetworkIFs();
        displays = hal.getDisplays();
        fileStores = operatingSystem.getFileSystem().getFileStores();
        powerSources = hal.getPowerSources();
        drives = hal.getDiskStores();
    }

    private <T> T getDevice(List<@Nullable T> devices, int index) throws DeviceNotFoundException {
        if (devices.size() <= index) {
            throw new DeviceNotFoundException("Device with index: " + index + " can not be found!");
        }
        return (T) devices.get(index);
    }

    private <T> T getDevice(T @Nullable [] devices, int index) throws DeviceNotFoundException {
        if (devices == null || devices.length <= index) {
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
        String osVersion = operatingSystem.getVersionInfo().toString();
        return new StringType(osVersion);
    }

    @Override
    public StringType getCpuName() {
        String name = cpu.getProcessorIdentifier().getName();
        return new StringType(name);
    }

    @Override
    public StringType getCpuDescription() {
        String model = cpu.getProcessorIdentifier().getModel();
        String family = cpu.getProcessorIdentifier().getFamily();
        String serialNumber = computerSystem.getSerialNumber();
        String identifier = cpu.getProcessorIdentifier().getIdentifier();
        String vendor = cpu.getProcessorIdentifier().getVendor();
        String architecture = cpu.getProcessorIdentifier().isCpu64bit() ? "64 bit" : "32 bit";
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
    public QuantityType<DataAmount> getMemoryTotal() {
        long totalMemory = memory.getTotal();
        totalMemory = getSizeInMB(totalMemory);
        return new QuantityType<>(totalMemory, Units.MEBIBYTE);
    }

    @Override
    public QuantityType<DataAmount> getMemoryAvailable() {
        long availableMemory = memory.getAvailable();
        availableMemory = getSizeInMB(availableMemory);
        return new QuantityType<>(availableMemory, Units.MEBIBYTE);
    }

    @Override
    public QuantityType<DataAmount> getMemoryUsed() {
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;
        usedMemory = getSizeInMB(usedMemory);
        return new QuantityType<>(usedMemory, Units.MEBIBYTE);
    }

    @Override
    public QuantityType<DataAmount> getStorageTotal(int index) throws DeviceNotFoundException {
        OSFileStore fileStore = getDevice(fileStores, index);
        fileStore.updateAttributes();
        long totalSpace = fileStore.getTotalSpace();
        totalSpace = getSizeInMB(totalSpace);
        return new QuantityType<>(totalSpace, Units.MEBIBYTE);
    }

    @Override
    public QuantityType<DataAmount> getStorageAvailable(int index) throws DeviceNotFoundException {
        OSFileStore fileStore = getDevice(fileStores, index);
        fileStore.updateAttributes();
        long freeSpace = fileStore.getUsableSpace();
        freeSpace = getSizeInMB(freeSpace);
        return new QuantityType<>(freeSpace, Units.MEBIBYTE);
    }

    @Override
    public QuantityType<DataAmount> getStorageUsed(int index) throws DeviceNotFoundException {
        OSFileStore fileStore = getDevice(fileStores, index);
        fileStore.updateAttributes();
        long totalSpace = fileStore.getTotalSpace();
        long freeSpace = fileStore.getUsableSpace();
        long usedSpace = totalSpace - freeSpace;
        usedSpace = getSizeInMB(usedSpace);
        return new QuantityType<>(usedSpace, Units.MEBIBYTE);
    }

    @Override
    public @Nullable PercentType getStorageAvailablePercent(int deviceIndex) throws DeviceNotFoundException {
        OSFileStore fileStore = getDevice(fileStores, deviceIndex);
        fileStore.updateAttributes();
        long totalSpace = fileStore.getTotalSpace();
        long freeSpace = fileStore.getUsableSpace();
        if (totalSpace > 0) {
            double freePercentDecimal = (double) freeSpace / (double) totalSpace;
            BigDecimal freePercent = getPercentsValue(freePercentDecimal);
            return new PercentType(freePercent);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable PercentType getStorageUsedPercent(int deviceIndex) throws DeviceNotFoundException {
        OSFileStore fileStore = getDevice(fileStores, deviceIndex);
        fileStore.updateAttributes();
        long totalSpace = fileStore.getTotalSpace();
        long freeSpace = fileStore.getUsableSpace();
        long usedSpace = totalSpace - freeSpace;
        if (totalSpace > 0) {
            double usedPercentDecimal = (double) usedSpace / (double) totalSpace;
            BigDecimal usedPercent = getPercentsValue(usedPercentDecimal);
            return new PercentType(usedPercent);
        } else {
            return null;
        }
    }

    @Override
    public StringType getStorageName(int index) throws DeviceNotFoundException {
        OSFileStore fileStore = getDevice(fileStores, index);
        String name = fileStore.getName();
        return new StringType(name);
    }

    @Override
    public StringType getStorageType(int deviceIndex) throws DeviceNotFoundException {
        OSFileStore fileStore = getDevice(fileStores, deviceIndex);
        String type = fileStore.getType();
        return new StringType(type);
    }

    @Override
    public StringType getStorageDescription(int index) throws DeviceNotFoundException {
        OSFileStore fileStore = getDevice(fileStores, index);
        String description = fileStore.getDescription();
        return new StringType(description);
    }

    @Override
    public StringType getNetworkIp(int index) throws DeviceNotFoundException {
        NetworkIF netInterface = getDevice(networks, index);
        netInterface.updateAttributes();
        String[] ipAddresses = netInterface.getIPv4addr();
        String ipv4 = getDevice(ipAddresses, 0);
        return new StringType(ipv4);
    }

    @Override
    public StringType getNetworkName(int index) throws DeviceNotFoundException {
        NetworkIF netInterface = getDevice(networks, index);
        String name = netInterface.getName();
        return new StringType(name);
    }

    @Override
    public StringType getNetworkDisplayName(int index) throws DeviceNotFoundException {
        NetworkIF netInterface = getDevice(networks, index);
        String adapterName = netInterface.getDisplayName();
        return new StringType(adapterName);
    }

    @Override
    public StringType getDisplayInformation(int index) throws DeviceNotFoundException {
        Display display = getDevice(displays, index);

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
    public @Nullable QuantityType<Temperature> getSensorsCpuTemperature() {
        BigDecimal cpuTemp = new BigDecimal(sensors.getCpuTemperature());
        cpuTemp = cpuTemp.setScale(PRECISION_AFTER_DECIMAL_SIGN, RoundingMode.HALF_UP);
        return cpuTemp.signum() == 1 ? new QuantityType<>(cpuTemp, SIUnits.CELSIUS) : null;
    }

    @Override
    public @Nullable QuantityType<ElectricPotential> getSensorsCpuVoltage() {
        BigDecimal cpuVoltage = new BigDecimal(sensors.getCpuVoltage());
        cpuVoltage = cpuVoltage.setScale(PRECISION_AFTER_DECIMAL_SIGN, RoundingMode.HALF_UP);
        return cpuVoltage.signum() == 1 ? new QuantityType<>(cpuVoltage, Units.VOLT) : null;
    }

    @Override
    public @Nullable DecimalType getSensorsFanSpeed(int index) throws DeviceNotFoundException {
        int[] fanSpeeds = sensors.getFanSpeeds();
        int speed = 0; // 0 means unable to measure speed
        if (index < fanSpeeds.length) {
            speed = fanSpeeds[index];
        } else {
            throw new DeviceNotFoundException();
        }
        return speed > 0 ? new DecimalType(speed) : null;
    }

    @Override
    public @Nullable QuantityType<Time> getBatteryRemainingTime(int index) throws DeviceNotFoundException {
        PowerSource powerSource = getDevice(powerSources, index);
        powerSource.updateAttributes();
        double remainingTimeInSeconds = powerSource.getTimeRemainingEstimated();
        // The getTimeRemaining() method returns (-1.0) if is calculating or (-2.0) if the time is unlimited.
        BigDecimal remainingTime = getTimeInMinutes(remainingTimeInSeconds);
        return remainingTime.signum() == 1 ? new QuantityType<>(remainingTime, Units.MINUTE) : null;
    }

    @Override
    public PercentType getBatteryRemainingCapacity(int index) throws DeviceNotFoundException {
        PowerSource powerSource = getDevice(powerSources, index);
        powerSource.updateAttributes();
        double remainingCapacity = powerSource.getRemainingCapacityPercent();
        BigDecimal remainingCapacityPercents = getPercentsValue(remainingCapacity);
        return new PercentType(remainingCapacityPercents);
    }

    @Override
    public StringType getBatteryName(int index) throws DeviceNotFoundException {
        PowerSource powerSource = getDevice(powerSources, index);
        String name = powerSource.getName();
        return new StringType(name);
    }

    @Override
    public @Nullable PercentType getMemoryAvailablePercent() {
        long availableMemory = memory.getAvailable();
        long totalMemory = memory.getTotal();
        if (totalMemory > 0) {
            double freePercentDecimal = (double) availableMemory / (double) totalMemory;
            BigDecimal freePercent = getPercentsValue(freePercentDecimal);
            return new PercentType(freePercent);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable PercentType getMemoryUsedPercent() {
        long availableMemory = memory.getAvailable();
        long totalMemory = memory.getTotal();
        long usedMemory = totalMemory - availableMemory;
        if (totalMemory > 0) {
            double usedPercentDecimal = (double) usedMemory / (double) totalMemory;
            BigDecimal usedPercent = getPercentsValue(usedPercentDecimal);
            return new PercentType(usedPercent);
        } else {
            return null;
        }
    }

    @Override
    public StringType getDriveName(int deviceIndex) throws DeviceNotFoundException {
        HWDiskStore drive = getDevice(drives, deviceIndex);
        String name = drive.getName();
        return new StringType(name);
    }

    @Override
    public StringType getDriveModel(int deviceIndex) throws DeviceNotFoundException {
        HWDiskStore drive = getDevice(drives, deviceIndex);
        String model = drive.getModel();
        return new StringType(model);
    }

    @Override
    public StringType getDriveSerialNumber(int deviceIndex) throws DeviceNotFoundException {
        HWDiskStore drive = getDevice(drives, deviceIndex);
        String serialNumber = drive.getSerial();
        return new StringType(serialNumber);
    }

    @Override
    public QuantityType<DataAmount> getSwapTotal() {
        long swapTotal = memory.getVirtualMemory().getSwapTotal();
        swapTotal = getSizeInMB(swapTotal);
        return new QuantityType<>(swapTotal, Units.MEBIBYTE);
    }

    @Override
    public QuantityType<DataAmount> getSwapAvailable() {
        long swapTotal = memory.getVirtualMemory().getSwapTotal();
        long swapUsed = memory.getVirtualMemory().getSwapUsed();
        long swapAvailable = swapTotal - swapUsed;
        swapAvailable = getSizeInMB(swapAvailable);
        return new QuantityType<>(swapAvailable, Units.MEBIBYTE);
    }

    @Override
    public QuantityType<DataAmount> getSwapUsed() {
        long swapUsed = memory.getVirtualMemory().getSwapUsed();
        swapUsed = getSizeInMB(swapUsed);
        return new QuantityType<>(swapUsed, Units.MEBIBYTE);
    }

    @Override
    public @Nullable PercentType getSwapAvailablePercent() {
        long swapTotal = memory.getVirtualMemory().getSwapTotal();
        long swapUsed = memory.getVirtualMemory().getSwapUsed();
        long swapAvailable = swapTotal - swapUsed;
        if (swapTotal > 0) {
            double swapAvailablePercentDecimal = (double) swapAvailable / (double) swapTotal;
            BigDecimal swapAvailablePercent = getPercentsValue(swapAvailablePercentDecimal);
            return new PercentType(swapAvailablePercent);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable PercentType getSwapUsedPercent() {
        long swapTotal = memory.getVirtualMemory().getSwapTotal();
        long swapUsed = memory.getVirtualMemory().getSwapUsed();
        if (swapTotal > 0) {
            double swapUsedPercentDecimal = (double) swapUsed / (double) swapTotal;
            BigDecimal swapUsedPercent = getPercentsValue(swapUsedPercentDecimal);
            return new PercentType(swapUsedPercent);
        } else {
            return null;
        }
    }

    private long getSizeInMB(long sizeInBytes) {
        return Math.round(sizeInBytes / (1024D * 1024));
    }

    private BigDecimal getPercentsValue(double decimalFraction) {
        BigDecimal result = new BigDecimal(decimalFraction * 100);
        result = result.setScale(PRECISION_AFTER_DECIMAL_SIGN, RoundingMode.HALF_UP);
        return result;
    }

    private BigDecimal getTimeInMinutes(double timeInSeconds) {
        BigDecimal timeInMinutes = new BigDecimal(timeInSeconds / 60);
        timeInMinutes = timeInMinutes.setScale(PRECISION_AFTER_DECIMAL_SIGN, RoundingMode.UP);
        return timeInMinutes;
    }

    @Override
    public @Nullable PercentType getSystemCpuLoad() {
        PercentType load = (ticks[0] > 0) ? new PercentType(getPercentsValue(cpu.getSystemCpuLoadBetweenTicks(ticks)))
                : null;
        ticks = cpu.getSystemCpuLoadTicks();
        return load;
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public @Nullable DecimalType getCpuLoad1() {
        BigDecimal avarageCpuLoad = getAvarageCpuLoad(1);
        return avarageCpuLoad.signum() == -1 ? null : new DecimalType(avarageCpuLoad);
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public @Nullable DecimalType getCpuLoad5() {
        BigDecimal avarageCpuLoad = getAvarageCpuLoad(5);
        return avarageCpuLoad.signum() == -1 ? null : new DecimalType(avarageCpuLoad);
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public @Nullable DecimalType getCpuLoad15() {
        BigDecimal avarageCpuLoad = getAvarageCpuLoad(15);
        return avarageCpuLoad.signum() == -1 ? null : new DecimalType(avarageCpuLoad);
    }

    private BigDecimal getAvarageCpuLoad(int timeInMinutes) {
        // This parameter is specified in OSHI Javadoc
        int index;
        switch (timeInMinutes) {
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
        result = result.setScale(PRECISION_AFTER_DECIMAL_SIGN, RoundingMode.HALF_UP);
        return result;
    }

    @Override
    public QuantityType<Time> getCpuUptime() {
        long seconds = operatingSystem.getSystemUptime();
        return new QuantityType<>(getTimeInMinutes(seconds), Units.MINUTE);
    }

    @Override
    public DecimalType getCpuThreads() {
        int threadCount = operatingSystem.getThreadCount();
        return new DecimalType(threadCount);
    }

    @Override
    public StringType getNetworkMac(int networkIndex) throws DeviceNotFoundException {
        NetworkIF network = getDevice(networks, networkIndex);
        String mac = network.getMacaddr();
        return new StringType(mac);
    }

    @Override
    public DecimalType getNetworkPacketsReceived(int networkIndex) throws DeviceNotFoundException {
        NetworkIF network = getDevice(networks, networkIndex);
        network.updateAttributes();
        long packRecv = network.getPacketsRecv();
        return new DecimalType(packRecv);
    }

    @Override
    public DecimalType getNetworkPacketsSent(int networkIndex) throws DeviceNotFoundException {
        NetworkIF network = getDevice(networks, networkIndex);
        network.updateAttributes();
        long packSent = network.getPacketsSent();
        return new DecimalType(packSent);
    }

    @Override
    public QuantityType<DataAmount> getNetworkDataSent(int networkIndex) throws DeviceNotFoundException {
        NetworkIF network = getDevice(networks, networkIndex);
        network.updateAttributes();
        long bytesSent = network.getBytesSent();
        return new QuantityType<>(getSizeInMB(bytesSent), Units.MEBIBYTE);
    }

    @Override
    public QuantityType<DataAmount> getNetworkDataReceived(int networkIndex) throws DeviceNotFoundException {
        NetworkIF network = getDevice(networks, networkIndex);
        network.updateAttributes();
        long bytesRecv = network.getBytesRecv();
        return new QuantityType<>(getSizeInMB(bytesRecv), Units.MEBIBYTE);
    }

    @Override
    public int getCurrentProcessID() {
        return operatingSystem.getProcessId();
    }

    @Override
    public @Nullable StringType getProcessName(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            String name = process.getName();
            return new StringType(name);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getProcessCpuUsage(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            DecimalType load = (processTicks.containsKey(pid))
                    ? new DecimalType(getPercentsValue(process.getProcessCpuLoadBetweenTicks(processTicks.get(pid))))
                    : null;
            processTicks.put(pid, process);
            return load;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable QuantityType<DataAmount> getProcessMemoryUsage(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            long memortInBytes = process.getResidentSetSize();
            long memoryInMB = getSizeInMB(memortInBytes);
            return new QuantityType<>(memoryInMB, Units.MEBIBYTE);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable StringType getProcessPath(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            String path = process.getPath();
            return new StringType(path);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getProcessThreads(int pid) throws DeviceNotFoundException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            int threadCount = process.getThreadCount();
            return new DecimalType(threadCount);
        } else {
            return null;
        }
    }

    @Override
    public int getNetworkIFCount() {
        return networks.size();
    }

    @Override
    public int getDisplayCount() {
        return displays.size();
    }

    @Override
    public int getFileOSStoreCount() {
        return fileStores.size();
    }

    @Override
    public int getPowerSourceCount() {
        return powerSources.size();
    }

    @Override
    public int getDriveCount() {
        return drives.size();
    }

    @Override
    public int getFanCount() {
        return sensors.getFanSpeeds().length;
    }
}
