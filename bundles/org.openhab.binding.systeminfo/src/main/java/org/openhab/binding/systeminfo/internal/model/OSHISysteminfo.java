/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Display;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;
import oshi.hardware.VirtualMemory;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.OSVersionInfo;
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
 *
 * @see <a href="https://github.com/oshi/oshi">OSHI GitHub repository</a>
 */
@NonNullByDefault
@Component(service = SysteminfoInterface.class)
public class OSHISysteminfo implements SysteminfoInterface {

    private final Logger logger = LoggerFactory.getLogger(OSHISysteminfo.class);

    private @NonNullByDefault({}) HardwareAbstractionLayer hal;
    private @NonNullByDefault({}) OperatingSystem operatingSystem;

    private @NonNullByDefault({}) ComputerSystem computerSystem;
    private @NonNullByDefault({}) CentralProcessor cpu;
    private @NonNullByDefault({}) Display[] displays;
    private @NonNullByDefault({}) HWDiskStore[] drives;
    private @NonNullByDefault({}) OSFileStore[] fileStores;
    private @NonNullByDefault({}) GlobalMemory memory;
    private @NonNullByDefault({}) NetworkIF[] networks;
    private @NonNullByDefault({}) PowerSource[] powerSources;
    private @NonNullByDefault({}) Sensors sensors;

    private long[] oldCpuTicks;

    public static final int PRECISION_AFTER_DECIMAL_SIGN = 2;

    /**
     * Some of the methods used in this constructor execute native code and require execute permissions
     *
     */
    public OSHISysteminfo() {
        logger.debug("OSHISysteminfo service is created");

        SystemInfo systemInfo = new SystemInfo();
        hal = systemInfo.getHardware();
        operatingSystem = systemInfo.getOperatingSystem();

        computerSystem = hal.getComputerSystem();
        cpu = hal.getProcessor();
        displays = hal.getDisplays();
        drives = hal.getDiskStores();
        fileStores = operatingSystem.getFileSystem().getFileStores();
        memory = hal.getMemory();
        networks = hal.getNetworkIFs();
        powerSources = hal.getPowerSources();
        sensors = hal.getSensors();

        oldCpuTicks = cpu.getSystemCpuLoadTicks();
    }

    private Object getDevice(Object @Nullable [] devices, int index) throws IllegalArgumentException {
        if ((devices == null) || (index >= devices.length)) {
            throw new IllegalArgumentException("Device with index " + index + " can not be found.");
        }
        return devices[index];
    }

    private OSProcess getProcess(int pid) throws IllegalArgumentException {
        OSProcess process = operatingSystem.getProcess(pid);
        if (process == null) {
            throw new IllegalArgumentException("Process with id " + pid + " can not be found.");
        }
        return process;
    }

    @Override
    public StringType getOsFamily() {
        return new StringType(operatingSystem.getFamily());
    }

    @Override
    public StringType getOsManufacturer() {
        return new StringType(operatingSystem.getManufacturer());
    }

    @Override
    public StringType getOsVersion() {
        OSVersionInfo osVersion = operatingSystem.getVersionInfo();
        return new StringType(osVersion.toString());
    }

    @Override
    public StringType getCpuName() {
        ProcessorIdentifier identifier = cpu.getProcessorIdentifier();
        return new StringType(identifier.getName());
    }

    @Override
    public StringType getCpuDescription() {
        ProcessorIdentifier identifier = cpu.getProcessorIdentifier();
        String serialNumber = computerSystem.getSerialNumber();
        String architecture = identifier.isCpu64bit() ? "64 bit" : "32 bit";
        String format = "Model: %s %s, family: %s, vendor: %s, sn: %s, identifier: %s";
        return new StringType(String.format(format, identifier.getModel(), architecture, identifier.getFamily(),
                identifier.getVendor(), serialNumber, identifier.getIdentifier()));
    }

    @Override
    public DecimalType getCpuLogicalCores() {
        return new DecimalType(cpu.getLogicalProcessorCount());
    }

    @Override
    public DecimalType getCpuPhysicalCores() {
        return new DecimalType(cpu.getPhysicalProcessorCount());
    }

    @Override
    public @Nullable DecimalType getStorageTotal(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long total = store.getTotalSpace();
        return total < 0 ? null : new DecimalType(getSizeInMB(total));
    }

    @Override
    public @Nullable DecimalType getStorageAvailable(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long available = store.getUsableSpace();
        return available < 0 ? null : new DecimalType(getSizeInMB(available));
    }

    @Override
    public @Nullable DecimalType getStorageUsed(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long used = store.getTotalSpace() - store.getUsableSpace();
        return used < 0 ? null : new DecimalType(getSizeInMB(used));
    }

    @Override
    public @Nullable DecimalType getStorageAvailablePercent(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long total = store.getTotalSpace();
        if (total > 0) {
            long available = store.getUsableSpace();
            BigDecimal percent = getPercentsValue((double) available / (double) total);
            return percent.signum() == -1 ? null : new DecimalType(percent);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getStorageUsedPercent(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long total = store.getTotalSpace();
        if (total > 0) {
            long used = total - store.getUsableSpace();
            BigDecimal percent = getPercentsValue((double) used / (double) total);
            return percent.signum() == -1 ? null : new DecimalType(percent);
        } else {
            return null;
        }
    }

    @Override
    public StringType getStorageName(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        return new StringType(store.getName());
    }

    @Override
    public StringType getStorageType(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        return new StringType(store.getType());
    }

    @Override
    public StringType getStorageDescription(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        return new StringType(store.getDescription());
    }

    @Override
    public StringType getDisplayInformation(int index) throws IllegalArgumentException {
        Display display = (Display) getDevice(displays, index);

        byte[] edid = display.getEdid();
        String manufacturer = EdidUtil.getManufacturerID(edid);
        String product = EdidUtil.getProductID(edid);
        String serialNumber = EdidUtil.getSerialNo(edid);
        int width = EdidUtil.getHcm(edid);
        int height = EdidUtil.getVcm(edid);

        String format = "Product %s, manufacturer %s, SN: %s, Width: %d, Height: %d";
        return new StringType(String.format(format, product, manufacturer, serialNumber, width, height));
    }

    @Override
    public @Nullable DecimalType getSensorsCpuTemperature() {
        BigDecimal temperature = new BigDecimal(sensors.getCpuTemperature());
        temperature = temperature.setScale(PRECISION_AFTER_DECIMAL_SIGN, BigDecimal.ROUND_HALF_UP);
        return temperature.signum() == 1 ? new DecimalType(temperature) : null;
    }

    @Override
    public @Nullable DecimalType getSensorsCpuVoltage() {
        BigDecimal voltage = new BigDecimal(sensors.getCpuVoltage());
        voltage = voltage.setScale(PRECISION_AFTER_DECIMAL_SIGN, BigDecimal.ROUND_HALF_UP);
        return voltage.signum() == 1 ? new DecimalType(voltage) : null;
    }

    @Override
    public @Nullable DecimalType getSensorsFanSpeed(int index) throws IllegalArgumentException {
        int speed = (int) getDevice(ArrayUtils.toObject(sensors.getFanSpeeds()), index);
        return speed > 0 ? new DecimalType(speed) : null;
    }

    @Override
    public StringType getBatteryName(int index) throws IllegalArgumentException {
        PowerSource source = (PowerSource) getDevice(powerSources, index);
        source.updateAttributes();
        return new StringType(source.getName());
    }

    @Override
    public @Nullable DecimalType getBatteryRemainingTime(int index) throws IllegalArgumentException {
        PowerSource source = (PowerSource) getDevice(powerSources, index);
        source.updateAttributes();
        double remaining = source.getTimeRemainingEstimated();
        return remaining < 0 ? null : new DecimalType(getTimeInMinutes(remaining));
    }

    @Override
    public @Nullable DecimalType getBatteryRemainingCapacity(int index) throws IllegalArgumentException {
        PowerSource source = (PowerSource) getDevice(powerSources, index);
        source.updateAttributes();
        double remaining = source.getRemainingCapacityPercent();
        return remaining < 0 ? null : new DecimalType(getPercentsValue(remaining));
    }

    @Override
    public @Nullable DecimalType getMemoryTotal() {
        long total = memory.getTotal();
        return total < 0 ? null : new DecimalType(getSizeInMB(total));
    }

    @Override
    public @Nullable DecimalType getMemoryAvailable() {
        long available = memory.getAvailable();
        return available < 0 ? null : new DecimalType(getSizeInMB(available));
    }

    @Override
    public @Nullable DecimalType getMemoryUsed() {
        long used = memory.getTotal() - memory.getAvailable();
        return used < 0 ? null : new DecimalType(getSizeInMB(used));
    }

    @Override
    public @Nullable DecimalType getMemoryAvailablePercent() {
        long total = memory.getTotal();
        if (total > 0) {
            long available = memory.getAvailable();
            BigDecimal percent = getPercentsValue((double) available / (double) total);
            return percent.signum() == -1 ? null : new DecimalType(percent);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getMemoryUsedPercent() {
        long total = memory.getTotal();
        if (total > 0) {
            long used = total - memory.getAvailable();
            BigDecimal percent = getPercentsValue((double) used / (double) total);
            return percent.signum() == -1 ? null : new DecimalType(percent);
        } else {
            return null;
        }
    }

    @Override
    public StringType getDriveName(int deviceIndex) throws IllegalArgumentException {
        HWDiskStore drive = (HWDiskStore) getDevice(drives, deviceIndex);
        return new StringType(drive.getName());
    }

    @Override
    public StringType getDriveModel(int deviceIndex) throws IllegalArgumentException {
        HWDiskStore drive = (HWDiskStore) getDevice(drives, deviceIndex);
        return new StringType(drive.getModel());
    }

    @Override
    public StringType getDriveSerialNumber(int deviceIndex) throws IllegalArgumentException {
        HWDiskStore drive = (HWDiskStore) getDevice(drives, deviceIndex);
        return new StringType(drive.getSerial());
    }

    @Override
    public @Nullable DecimalType getSwapTotal() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long total = virtual.getSwapTotal();
        return total < 0 ? null : new DecimalType(getSizeInMB(total));
    }

    @Override
    public @Nullable DecimalType getSwapAvailable() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long available = virtual.getSwapTotal() - virtual.getSwapUsed();
        return available < 0 ? null : new DecimalType(getSizeInMB(available));
    }

    @Override
    public @Nullable DecimalType getSwapUsed() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long used = virtual.getSwapUsed();
        return used < 0 ? null : new DecimalType(getSizeInMB(used));
    }

    @Override
    public @Nullable DecimalType getSwapAvailablePercent() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long total = virtual.getSwapTotal();
        if (total > 0) {
            long available = total - virtual.getSwapUsed();
            BigDecimal percent = getPercentsValue((double) available / (double) total);
            return percent.signum() == -1 ? null : new DecimalType(percent);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getSwapUsedPercent() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long total = virtual.getSwapTotal();
        if (total > 0) {
            long used = virtual.getSwapUsed();
            BigDecimal percent = getPercentsValue((double) used / (double) total);
            return percent.signum() == -1 ? null : new DecimalType(percent);
        } else {
            return null;
        }
    }

    private double getSizeInMB(long sizeInBytes) {
        double kBytes = Math.round(sizeInBytes / 1024.0);
        return Math.round(100.0 * (kBytes / 1024.0)) / 100.0;
    }

    private BigDecimal getPercentsValue(double decimalFraction) {
        BigDecimal result = new BigDecimal(decimalFraction * 100.0);
        return result.setScale(PRECISION_AFTER_DECIMAL_SIGN, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal getTimeInMinutes(double timeInSeconds) {
        BigDecimal timeInMinutes = new BigDecimal(timeInSeconds / 60.0);
        return timeInMinutes.setScale(PRECISION_AFTER_DECIMAL_SIGN, BigDecimal.ROUND_UP);
    }

    @Override
    public DecimalType getCpuLoad() {
        double load = cpu.getSystemCpuLoadBetweenTicks(oldCpuTicks);
        oldCpuTicks = cpu.getSystemCpuLoadTicks();
        return new DecimalType(getPercentsValue(load));
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public @Nullable DecimalType getCpuLoad1() {
        BigDecimal load = getAvarageCpuLoad(1);
        return load.signum() == -1 ? null : new DecimalType(load);
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public @Nullable DecimalType getCpuLoad5() {
        BigDecimal load = getAvarageCpuLoad(5);
        return load.signum() == -1 ? null : new DecimalType(load);
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public @Nullable DecimalType getCpuLoad15() {
        BigDecimal load = getAvarageCpuLoad(15);
        return load.signum() == -1 ? null : new DecimalType(load);
    }

    @Override
    public DecimalType getCpuThreads() {
        return new DecimalType(operatingSystem.getThreadCount());
    }

    @Override
    public DecimalType getCpuUptime() {
        long seconds = operatingSystem.getSystemUptime();
        return new DecimalType(getTimeInMinutes(seconds));
    }

    private BigDecimal getAvarageCpuLoad(int timeInMunutes) {
        // This parameter is specified in OSHI Javadoc
        int index;
        switch (timeInMunutes) {
            case 1: {
                index = 0;
                break;
            }
            case 5: {
                index = 1;
                break;
            }
            case 15:
            default: {
                index = 2;
                break;
            }
        }
        double processorLoads[] = cpu.getSystemLoadAverage(index + 1);
        BigDecimal result = new BigDecimal(processorLoads[index]);
        return result.setScale(PRECISION_AFTER_DECIMAL_SIGN, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public StringType getNetworkIp(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        String[] ipAddresses = network.getIPv4addr();
        return new StringType((String) getDevice(ipAddresses, 0));
    }

    @Override
    public StringType getNetworkName(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return new StringType(network.getName());
    }

    @Override
    public StringType getNetworkDisplayName(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return new StringType(network.getDisplayName());
    }

    @Override
    public StringType getNetworkMac(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return new StringType(network.getMacaddr());
    }

    @Override
    public DecimalType getNetworkPacketsReceived(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return new DecimalType(network.getPacketsRecv());
    }

    @Override
    public DecimalType getNetworkPacketsSent(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return new DecimalType(network.getPacketsSent());
    }

    @Override
    public DecimalType getNetworkDataSent(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return new DecimalType(getSizeInMB(network.getBytesSent()));
    }

    @Override
    public DecimalType getNetworkDataReceived(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return new DecimalType(getSizeInMB(network.getBytesRecv()));
    }

    @Override
    public @Nullable StringType getProcessName(int pid) throws IllegalArgumentException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            return new StringType(process.getName());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getProcessCpuUsage(int pid) throws IllegalArgumentException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            double cpuUsageRaw = (double) process.getKernelTime() / (double) process.getUpTime();
            cpuUsageRaw += (double) process.getUserTime() / (double) process.getUpTime();
            return new DecimalType(getPercentsValue(cpuUsageRaw));
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getProcessResidentMemory(int pid) throws IllegalArgumentException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            return new DecimalType(getSizeInMB(process.getResidentSetSize()));
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getProcessVirtualMemory(int pid) throws IllegalArgumentException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            return new DecimalType(getSizeInMB(process.getVirtualSize()));
        } else {
            return null;
        }
    }

    @Override
    public @Nullable StringType getProcessPath(int pid) throws IllegalArgumentException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            return new StringType(process.getPath());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getProcessThreads(int pid) throws IllegalArgumentException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            return new DecimalType(process.getThreadCount());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DecimalType getProcessUpTime(int pid) throws IllegalArgumentException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            return new DecimalType(process.getUpTime() / 1000);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable StringType getProcessUser(int pid) throws IllegalArgumentException {
        if (pid > 0) {
            OSProcess process = getProcess(pid);
            return new StringType(process.getUser());
        } else {
            return null;
        }
    }
}
