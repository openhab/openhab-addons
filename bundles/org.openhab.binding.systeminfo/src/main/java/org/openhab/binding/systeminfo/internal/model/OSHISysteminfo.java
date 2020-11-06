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
import java.math.RoundingMode;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    public String getOsFamily() {
        return operatingSystem.getFamily();
    }

    @Override
    public String getOsManufacturer() {
        return operatingSystem.getManufacturer();
    }

    @Override
    public String getOsVersion() {
        return operatingSystem.getVersionInfo().toString();
    }

    @Override
    public String getCpuName() {
        return cpu.getProcessorIdentifier().getName();
    }

    @Override
    public String getCpuDescription() {
        ProcessorIdentifier identifier = cpu.getProcessorIdentifier();
        String serialNumber = computerSystem.getSerialNumber();
        String architecture = identifier.isCpu64bit() ? "64 bit" : "32 bit";
        String format = "Model: %s %s, family: %s, vendor: %s, sn: %s, identifier: %s";
        return String.format(format, identifier.getModel(), architecture, identifier.getFamily(),
                identifier.getVendor(), serialNumber, identifier.getIdentifier());
    }

    @Override
    public BigDecimal getCpuLogicalCores() {
        return BigDecimal.valueOf(cpu.getLogicalProcessorCount());
    }

    @Override
    public BigDecimal getCpuPhysicalCores() {
        return BigDecimal.valueOf(cpu.getPhysicalProcessorCount());
    }

    @Override
    public @Nullable BigDecimal getStorageTotal(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long total = store.getTotalSpace();
        return total < 0 ? null : BigDecimal.valueOf(total);
    }

    @Override
    public @Nullable BigDecimal getStorageAvailable(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long available = store.getUsableSpace();
        return available < 0 ? null : BigDecimal.valueOf(available);
    }

    @Override
    public @Nullable BigDecimal getStorageUsed(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long used = store.getTotalSpace() - store.getUsableSpace();
        return used < 0 ? null : BigDecimal.valueOf(used);
    }

    @Override
    public @Nullable BigDecimal getStorageAvailablePercent(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long total = store.getTotalSpace();
        if (total > 0) {
            long available = store.getUsableSpace();
            BigDecimal percent = getPercentsValue((double) available / (double) total);
            return percent.signum() == -1 ? null : percent;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable BigDecimal getStorageUsedPercent(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        long total = store.getTotalSpace();
        if (total > 0) {
            long used = total - store.getUsableSpace();
            BigDecimal percent = getPercentsValue((double) used / (double) total);
            return percent.signum() == -1 ? null : percent;
        } else {
            return null;
        }
    }

    @Override
    public String getStorageName(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        return store.getName();
    }

    @Override
    public String getStorageType(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        return store.getType();
    }

    @Override
    public String getStorageDescription(int index) throws IllegalArgumentException {
        OSFileStore store = (OSFileStore) getDevice(fileStores, index);
        store.updateAtrributes();
        return store.getDescription();
    }

    @Override
    public String getDisplayInformation(int index) throws IllegalArgumentException {
        Display display = (Display) getDevice(displays, index);

        byte[] edid = display.getEdid();
        String manufacturer = EdidUtil.getManufacturerID(edid);
        String product = EdidUtil.getProductID(edid);
        String serialNumber = EdidUtil.getSerialNo(edid);
        int width = EdidUtil.getHcm(edid);
        int height = EdidUtil.getVcm(edid);

        String format = "Product %s, manufacturer %s, SN: %s, Width: %d, Height: %d";
        return String.format(format, product, manufacturer, serialNumber, width, height);
    }

    @Override
    public @Nullable BigDecimal getSensorsCpuTemperature() {
        BigDecimal temperature = new BigDecimal(sensors.getCpuTemperature());
        temperature = temperature.setScale(PRECISION_AFTER_DECIMAL_SIGN, RoundingMode.HALF_UP);
        return temperature.signum() == 1 ? temperature : null;
    }

    @Override
    public @Nullable BigDecimal getSensorsCpuVoltage() {
        BigDecimal voltage = new BigDecimal(sensors.getCpuVoltage());
        voltage = voltage.setScale(PRECISION_AFTER_DECIMAL_SIGN, RoundingMode.HALF_UP);
        return voltage.signum() == 1 ? voltage : null;
    }

    @Override
    public @Nullable BigDecimal getSensorsFanSpeed(int index) throws IllegalArgumentException {
        int speed = (int) getDevice(ArrayUtils.toObject(sensors.getFanSpeeds()), index);
        return speed > 0 ? BigDecimal.valueOf(speed) : null;
    }

    @Override
    public String getBatteryName(int index) throws IllegalArgumentException {
        PowerSource source = (PowerSource) getDevice(powerSources, index);
        source.updateAttributes();
        return source.getName();
    }

    @Override
    public @Nullable BigDecimal getBatteryRemainingTime(int index) throws IllegalArgumentException {
        PowerSource source = (PowerSource) getDevice(powerSources, index);
        source.updateAttributes();
        double remaining = source.getTimeRemainingEstimated();
        return remaining < 0.0 ? null : BigDecimal.valueOf(remaining);
    }

    @Override
    public BigDecimal getBatteryRemainingCapacity(int index) throws IllegalArgumentException {
        PowerSource source = (PowerSource) getDevice(powerSources, index);
        source.updateAttributes();
        double remaining = source.getRemainingCapacityPercent();
        if (remaining >= 0.0) {
            return getPercentsValue(remaining);
        } else {
            throw new IllegalArgumentException("Called with invalid device index");
        }
    }

    @Override
    public @Nullable BigDecimal getMemoryTotal() {
        long total = memory.getTotal();
        return total < 0 ? null : BigDecimal.valueOf(total);
    }

    @Override
    public @Nullable BigDecimal getMemoryAvailable() {
        long available = memory.getAvailable();
        return available < 0 ? null : BigDecimal.valueOf(available);
    }

    @Override
    public @Nullable BigDecimal getMemoryUsed() {
        long used = memory.getTotal() - memory.getAvailable();
        return used < 0 ? null : BigDecimal.valueOf(used);
    }

    @Override
    public @Nullable BigDecimal getMemoryAvailablePercent() {
        long total = memory.getTotal();
        if (total > 0) {
            long available = memory.getAvailable();
            BigDecimal percent = getPercentsValue((double) available / (double) total);
            return percent.signum() == -1 ? null : percent;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable BigDecimal getMemoryUsedPercent() {
        long total = memory.getTotal();
        if (total > 0) {
            long used = total - memory.getAvailable();
            BigDecimal percent = getPercentsValue((double) used / (double) total);
            return percent.signum() == -1 ? null : percent;
        } else {
            return null;
        }
    }

    @Override
    public String getDriveName(int deviceIndex) throws IllegalArgumentException {
        HWDiskStore drive = (HWDiskStore) getDevice(drives, deviceIndex);
        return drive.getName();
    }

    @Override
    public String getDriveModel(int deviceIndex) throws IllegalArgumentException {
        HWDiskStore drive = (HWDiskStore) getDevice(drives, deviceIndex);
        return drive.getModel();
    }

    @Override
    public String getDriveSerialNumber(int deviceIndex) throws IllegalArgumentException {
        HWDiskStore drive = (HWDiskStore) getDevice(drives, deviceIndex);
        return drive.getSerial();
    }

    @Override
    public @Nullable BigDecimal getSwapTotal() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long total = virtual.getSwapTotal();
        return total < 0 ? null : BigDecimal.valueOf(total);
    }

    @Override
    public @Nullable BigDecimal getSwapAvailable() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long available = virtual.getSwapTotal() - virtual.getSwapUsed();
        return available < 0 ? null : BigDecimal.valueOf(available);
    }

    @Override
    public @Nullable BigDecimal getSwapUsed() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long used = virtual.getSwapUsed();
        return used < 0 ? null : BigDecimal.valueOf(used);
    }

    @Override
    public @Nullable BigDecimal getSwapAvailablePercent() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long total = virtual.getSwapTotal();
        if (total > 0) {
            long available = total - virtual.getSwapUsed();
            BigDecimal percent = getPercentsValue((double) available / (double) total);
            return percent.signum() == -1 ? null : percent;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable BigDecimal getSwapUsedPercent() {
        VirtualMemory virtual = memory.getVirtualMemory();
        long total = virtual.getSwapTotal();
        if (total > 0) {
            long used = virtual.getSwapUsed();
            BigDecimal percent = getPercentsValue((double) used / (double) total);
            return percent.signum() == -1 ? null : percent;
        } else {
            return null;
        }
    }

    private BigDecimal getPercentsValue(double decimalFraction) {
        BigDecimal result = new BigDecimal(decimalFraction * 100.0);
        return result.setScale(PRECISION_AFTER_DECIMAL_SIGN, RoundingMode.HALF_UP);
    }

    @Override
    public @Nullable BigDecimal getCpuLoad() {
        double load = cpu.getSystemCpuLoadBetweenTicks(oldCpuTicks);
        oldCpuTicks = cpu.getSystemCpuLoadTicks();
        return load < 0 ? null : getPercentsValue(load);
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public @Nullable BigDecimal getCpuLoad1() {
        BigDecimal load = getAverageCpuLoad(1);
        return load.signum() == -1 ? null : load;
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public @Nullable BigDecimal getCpuLoad5() {
        BigDecimal load = getAverageCpuLoad(5);
        return load.signum() == -1 ? null : load;
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public @Nullable BigDecimal getCpuLoad15() {
        BigDecimal load = getAverageCpuLoad(15);
        return load.signum() == -1 ? null : load;
    }

    @Override
    public BigDecimal getCpuThreads() {
        return BigDecimal.valueOf(operatingSystem.getThreadCount());
    }

    @Override
    public BigDecimal getCpuUptime() {
        return BigDecimal.valueOf(operatingSystem.getSystemUptime());
    }

    private BigDecimal getAverageCpuLoad(int timeInMunutes) {
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
        return result.setScale(PRECISION_AFTER_DECIMAL_SIGN, RoundingMode.HALF_UP);
    }

    @Override
    public String getNetworkIp(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        String[] ipAddresses = network.getIPv4addr();
        return (String) getDevice(ipAddresses, 0);
    }

    @Override
    public String getNetworkName(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return network.getName();
    }

    @Override
    public String getNetworkDisplayName(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return network.getDisplayName();
    }

    @Override
    public String getNetworkMac(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return network.getMacaddr();
    }

    @Override
    public BigDecimal getNetworkPacketsReceived(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return BigDecimal.valueOf(network.getPacketsRecv());
    }

    @Override
    public BigDecimal getNetworkPacketsSent(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return BigDecimal.valueOf(network.getPacketsSent());
    }

    @Override
    public BigDecimal getNetworkDataSent(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return BigDecimal.valueOf(network.getBytesSent());
    }

    @Override
    public BigDecimal getNetworkDataReceived(int index) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(networks, index);
        network.updateAttributes();
        return BigDecimal.valueOf(network.getBytesRecv());
    }

    @Override
    public String getProcessName(BigDecimal pid) throws IllegalArgumentException {
        if (pid.compareTo(BigDecimal.ZERO) > 0) {
            OSProcess process = getProcess(pid.intValue());
            return process.getName();
        } else {
            throw new IllegalArgumentException("Called with invalid process ID");
        }
    }

    @Override
    public BigDecimal getProcessCpuUsage(BigDecimal pid) throws IllegalArgumentException {
        if (pid.compareTo(BigDecimal.ZERO) > 0) {
            OSProcess process = getProcess(pid.intValue());
            double cpuUsageRaw = (double) process.getKernelTime() / (double) process.getUpTime();
            cpuUsageRaw += (double) process.getUserTime() / (double) process.getUpTime();
            return getPercentsValue(cpuUsageRaw);
        } else {
            throw new IllegalArgumentException("Called with invalid process ID");
        }
    }

    @Override
    public BigDecimal getProcessResidentMemory(BigDecimal pid) throws IllegalArgumentException {
        if (pid.compareTo(BigDecimal.ZERO) > 0) {
            OSProcess process = getProcess(pid.intValue());
            return BigDecimal.valueOf(process.getResidentSetSize());
        } else {
            throw new IllegalArgumentException("Called with invalid process ID");
        }
    }

    @Override
    public BigDecimal getProcessVirtualMemory(BigDecimal pid) throws IllegalArgumentException {
        if (pid.compareTo(BigDecimal.ZERO) > 0) {
            OSProcess process = getProcess(pid.intValue());
            return BigDecimal.valueOf(process.getVirtualSize());
        } else {
            throw new IllegalArgumentException("Called with invalid process ID");
        }
    }

    @Override
    public String getProcessPath(BigDecimal pid) throws IllegalArgumentException {
        if (pid.compareTo(BigDecimal.ZERO) > 0) {
            OSProcess process = getProcess(pid.intValue());
            return process.getPath();
        } else {
            throw new IllegalArgumentException("Called with invalid process ID");
        }
    }

    @Override
    public BigDecimal getProcessThreads(BigDecimal pid) throws IllegalArgumentException {
        if (pid.compareTo(BigDecimal.ZERO) > 0) {
            OSProcess process = getProcess(pid.intValue());
            return BigDecimal.valueOf(process.getThreadCount());
        } else {
            throw new IllegalArgumentException("Called with invalid process ID");
        }
    }

    @Override
    public BigDecimal getProcessUpTime(BigDecimal pid) throws IllegalArgumentException {
        if (pid.compareTo(BigDecimal.ZERO) > 0) {
            OSProcess process = getProcess(pid.intValue());
            return BigDecimal.valueOf(process.getUpTime() / 1000);
        } else {
            throw new IllegalArgumentException("Called with invalid process ID");
        }
    }

    @Override
    public String getProcessUser(BigDecimal pid) throws IllegalArgumentException {
        if (pid.compareTo(BigDecimal.ZERO) > 0) {
            OSProcess process = getProcess(pid.intValue());
            return process.getUser();
        } else {
            throw new IllegalArgumentException("Called with invalid process ID");
        }
    }
}
