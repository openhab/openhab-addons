/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.osgi.service.component.annotations.Component;
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
 * @see <a href="https://github.com/oshi/oshi">OSHI github repository</a>
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

    private long[] oldCpuTicks;

    // Static objects, should be recreated on each request
    private OperatingSystem operatingSystem;
    private Display[] displays;
    private HWDiskStore[] drives;

    public static final int PRECISION_AFTER_DECIMAL_SIGN = 2;

    /**
     * Some of the methods used in this constructor execute native code and require execute permissions
     *
     */
    public OSHISysteminfo() {
        logger.debug("OSHISysteminfo service is created");

        SystemInfo systemInfo = new SystemInfo();
        hal = systemInfo.getHardware();

        // Doesn't need regular update, they may be queried repeatedly
        memory = hal.getMemory();
        cpu = hal.getProcessor();
        sensors = hal.getSensors();

        oldCpuTicks = cpu.getSystemCpuLoadTicks();

        // Static objects, should be recreated on each request. In OSHI 4.0.0. it is planned to change this mechanism -
        // see https://github.com/oshi/oshi/issues/310
        // TODO: Once the issue is resolved in OSHI , remove unnecessary object recreations from the public get methods
        operatingSystem = systemInfo.getOperatingSystem();
        displays = hal.getDisplays();
        drives = hal.getDiskStores();
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
        return new StringType(operatingSystem.getVersion().toString());
    }

    @Override
    public StringType getCpuName() {
        return new StringType(cpu.getName());
    }

    @Override
    public StringType getCpuDescription() {
        final String model = cpu.getModel();
        final String family = cpu.getFamily();
        final String id = cpu.getProcessorID();
        final String identifier = cpu.getIdentifier();
        final String vendor = cpu.getVendor();
        final String architecture = cpu.isCpu64bit() ? "64 bit" : "32 bit";

        String format = "Model: %s %s, family: %s, vendor: %s, sn: %s, identifier: %s";
        return new StringType(String.format(format, model, architecture, family, vendor, id, identifier));
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
    public DecimalType getStorageTotal(int index) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        OSFileStore[] stores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore store = (OSFileStore) getDevice(stores, index);
        return new DecimalType(getSizeInMB(store.getTotalSpace()));
    }

    @Override
    public DecimalType getStorageAvailable(int index) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        OSFileStore[] stores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore store = (OSFileStore) getDevice(stores, index);
        return new DecimalType(getSizeInMB(store.getUsableSpace()));
    }

    @Override
    public DecimalType getStorageUsed(int index) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        OSFileStore[] stores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore store = (OSFileStore) getDevice(stores, index);
        return new DecimalType(getSizeInMB(store.getTotalSpace() - store.getUsableSpace()));
    }

    @Override
    public DecimalType getStorageAvailablePercent(int index) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        OSFileStore[] stores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore store = (OSFileStore) getDevice(stores, index);

        long total = store.getTotalSpace();
        if (total <= 0) {
            throw new IllegalArgumentException("Storage device with index " + index + " has no space.");
        }
        return new DecimalType(getPercentsValue((double) store.getUsableSpace() / (double) total));
    }

    @Override
    public DecimalType getStorageUsedPercent(int index) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the storage data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        OSFileStore[] stores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore store = (OSFileStore) getDevice(stores, index);

        long total = store.getTotalSpace();
        if (total <= 0) {
            throw new IllegalArgumentException("Storage device with index " + index + " has no space.");
        }
        return new DecimalType(getPercentsValue((double) (total - store.getUsableSpace()) / (double) total));
    }

    @Override
    public StringType getStorageName(int index) throws IllegalArgumentException {
        OSFileStore[] stores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore store = (OSFileStore) getDevice(stores, index);
        return new StringType(store.getName());
    }

    @Override
    public StringType getStorageType(int index) throws IllegalArgumentException {
        OSFileStore[] stores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore store = (OSFileStore) getDevice(stores, index);
        return new StringType(store.getType());
    }

    @Override
    public StringType getStorageDescription(int index) throws IllegalArgumentException {
        OSFileStore[] stores = operatingSystem.getFileSystem().getFileStores();
        OSFileStore store = (OSFileStore) getDevice(stores, index);
        return new StringType(store.getDescription());
    }

    @Override
    public StringType getNetworkIp(int index) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        NetworkIF netInterface = (NetworkIF) getDevice(hal.getNetworkIFs(), index);
        String[] ipAddresses = netInterface.getIPv4addr();
        return new StringType((String) getDevice(ipAddresses, 0));
    }

    @Override
    public StringType getNetworkName(int index) throws IllegalArgumentException {
        NetworkIF netInterface = (NetworkIF) getDevice(hal.getNetworkIFs(), index);
        return new StringType(netInterface.getName());
    }

    @Override
    public StringType getNetworkDisplayName(int index) throws IllegalArgumentException {
        NetworkIF netInterface = (NetworkIF) getDevice(hal.getNetworkIFs(), index);
        return new StringType(netInterface.getDisplayName());
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
    public DecimalType getSensorsCpuTemperature() throws IllegalArgumentException {
        BigDecimal temperature = new BigDecimal(sensors.getCpuTemperature());
        temperature = temperature.setScale(PRECISION_AFTER_DECIMAL_SIGN, BigDecimal.ROUND_HALF_UP);

        if (temperature.signum() < 0) {
            throw new IllegalArgumentException("CPU reported negative temperature.");
        }
        return new DecimalType(temperature);
    }

    @Override
    public DecimalType getSensorsCpuVoltage() throws IllegalArgumentException {
        BigDecimal voltage = new BigDecimal(sensors.getCpuVoltage());
        voltage = voltage.setScale(PRECISION_AFTER_DECIMAL_SIGN, BigDecimal.ROUND_HALF_UP);

        if (voltage.signum() < 0) {
            throw new IllegalArgumentException("CPU reported negative voltage.");
        }
        return new DecimalType(voltage);
    }

    @Override
    public DecimalType getSensorsFanSpeed(int index) throws IllegalArgumentException {
        int speed = (int) getDevice(ArrayUtils.toObject(sensors.getFanSpeeds()), index);

        if (speed < 0) {
            throw new IllegalArgumentException("Fan reported negative speed.");
        }
        return new DecimalType(speed);
    }

    @Override
    public DecimalType getBatteryRemainingTime(int index) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the battery data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        PowerSource source = (PowerSource) getDevice(hal.getPowerSources(), index);
        // The getTimeRemaining() method returns (-1.0) if is calculating or (-2.0) if the time is unlimited.
        BigDecimal remainingTime = getTimeInMinutes(source.getTimeRemaining());

        if (remainingTime.doubleValue() < -1.0) {
            throw new IllegalArgumentException("Battery is charging.");
        } else if (remainingTime.doubleValue() < 0.0) {
            throw new IllegalArgumentException("Battery time is unknown.");
        }
        return new DecimalType(remainingTime);
    }

    @Override
    public DecimalType getBatteryRemainingCapacity(int index) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the battery data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        PowerSource source = (PowerSource) getDevice(hal.getPowerSources(), index);
        return new DecimalType(getPercentsValue(source.getRemainingCapacity()));
    }

    @Override
    public StringType getBatteryName(int index) throws IllegalArgumentException {
        PowerSource source = (PowerSource) getDevice(hal.getPowerSources(), index);
        return new StringType(source.getName());
    }

    @Override
    public DecimalType getMemoryTotal() {
        long total = memory.getTotal();
        return new DecimalType(getSizeInMB(total));
    }

    @Override
    public DecimalType getMemoryAvailable() {
        long available = memory.getAvailable();
        return new DecimalType(getSizeInMB(available));
    }

    @Override
    public DecimalType getMemoryUsed() {
        long used = memory.getTotal() - memory.getAvailable();
        return new DecimalType(getSizeInMB(used));
    }

    @Override
    public DecimalType getMemoryAvailablePercent() throws IllegalArgumentException {
        long total = memory.getTotal();
        if (total <= 0) {
            throw new IllegalArgumentException("Main memory has no space.");
        }
        return new DecimalType(getPercentsValue((double) memory.getAvailable() / (double) total));
    }

    @Override
    public DecimalType getMemoryUsedPercent() throws IllegalArgumentException {
        long total = memory.getTotal();
        if (total <= 0) {
            throw new IllegalArgumentException("Main memory has no space.");
        }
        return new DecimalType(getPercentsValue((double) (total - memory.getAvailable()) / (double) total));
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
    public DecimalType getSwapTotal() {
        VirtualMemory swap = memory.getVirtualMemory();
        return new DecimalType(getSizeInMB(swap.getSwapTotal()));
    }

    @Override
    public DecimalType getSwapAvailable() {
        VirtualMemory swap = memory.getVirtualMemory();
        return new DecimalType(getSizeInMB(swap.getSwapTotal() - swap.getSwapUsed()));
    }

    @Override
    public DecimalType getSwapUsed() {
        VirtualMemory swap = memory.getVirtualMemory();
        return new DecimalType(getSizeInMB(swap.getSwapUsed()));
    }

    @Override
    public DecimalType getSwapAvailablePercent() throws IllegalArgumentException {
        VirtualMemory swap = memory.getVirtualMemory();

        long total = swap.getSwapTotal();
        if (total <= 0) {
            throw new IllegalArgumentException("Swap memory has no space.");
        }
        return new DecimalType(getPercentsValue((double) (total - swap.getSwapUsed()) / total));
    }

    @Override
    public DecimalType getSwapUsedPercent() throws IllegalArgumentException {
        VirtualMemory swap = memory.getVirtualMemory();

        long total = swap.getSwapTotal();
        if (total <= 0) {
            throw new IllegalArgumentException("Swap memory has no space.");
        }
        return new DecimalType(getPercentsValue((double) swap.getSwapUsed() / total));
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
    public DecimalType getCpuLoad1() throws IllegalArgumentException {
        BigDecimal load = getAvarageCpuLoad(1);
        if (load.doubleValue() < 0.0) {
            throw new IllegalArgumentException("CPU reported negative load.");
        }
        return new DecimalType(load);
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public DecimalType getCpuLoad5() throws IllegalArgumentException {
        BigDecimal load = getAvarageCpuLoad(5);
        if (load.doubleValue() < 0.0) {
            throw new IllegalArgumentException("CPU reported negative load.");
        }
        return new DecimalType(load);
    }

    /**
     * {@inheritDoc}
     *
     * This information is available only on Mac and Linux OS.
     */
    @Override
    public DecimalType getCpuLoad15() throws IllegalArgumentException {
        BigDecimal load = getAvarageCpuLoad(15);
        if (load.doubleValue() < 0.0) {
            throw new IllegalArgumentException("CPU reported negative load.");
        }
        return new DecimalType(load);
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
    public DecimalType getCpuUptime() {
        long seconds = operatingSystem.getSystemUptime();
        return new DecimalType(getTimeInMinutes(seconds));
    }

    @Override
    public DecimalType getCpuThreads() {
        int threadCount = operatingSystem.getThreadCount();
        return new DecimalType(threadCount);
    }

    @Override
    public StringType getNetworkMac(int networkIndex) throws IllegalArgumentException {
        NetworkIF network = (NetworkIF) getDevice(hal.getNetworkIFs(), networkIndex);
        return new StringType(network.getMacaddr());
    }

    @Override
    public DecimalType getNetworkPacketsReceived(int networkIndex) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        NetworkIF network = (NetworkIF) getDevice(hal.getNetworkIFs(), networkIndex);
        network.updateAttributes();
        return new DecimalType(network.getPacketsRecv());
    }

    @Override
    public DecimalType getNetworkPacketsSent(int networkIndex) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        NetworkIF network = (NetworkIF) getDevice(hal.getNetworkIFs(), networkIndex);
        network.updateAttributes();
        long packSent = network.getPacketsSent();
        return new DecimalType(packSent);
    }

    @Override
    public DecimalType getNetworkDataSent(int networkIndex) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        NetworkIF network = (NetworkIF) getDevice(hal.getNetworkIFs(), networkIndex);
        network.updateAttributes();
        long bytesSent = network.getBytesSent();
        return new DecimalType(getSizeInMB(bytesSent));
    }

    @Override
    public DecimalType getNetworkDataReceived(int networkIndex) throws IllegalArgumentException {
        // In the current OSHI version a new query is required for the network data values to be updated
        // In OSHI 4.0.0. it is planned to change this mechanism - see https://github.com/oshi/oshi/issues/310
        NetworkIF network = (NetworkIF) getDevice(hal.getNetworkIFs(), networkIndex);
        network.updateAttributes();
        return new DecimalType(getSizeInMB(network.getBytesRecv()));
    }

    @Override
    public StringType getProcessName(int pid) throws IllegalArgumentException {
        OSProcess process = getProcess(pid);
        return new StringType(process.getName());
    }

    @Override
    public DecimalType getProcessCpuUsage(int pid) throws IllegalArgumentException {
        OSProcess process = getProcess(pid);
        double cpuUsageRaw = (double) process.getKernelTime() / (double) process.getUpTime();
        cpuUsageRaw += (double) process.getUserTime() / (double) process.getUpTime();
        return new DecimalType(getPercentsValue(cpuUsageRaw));
    }

    @Override
    public DecimalType getProcessResidentMemory(int pid) throws IllegalArgumentException {
        OSProcess process = getProcess(pid);
        long memory = process.getResidentSetSize();
        return new DecimalType(getSizeInMB(memory));
    }

    @Override
    public DecimalType getProcessVirtualMemory(int pid) throws IllegalArgumentException {
        OSProcess process = getProcess(pid);
        long memory = process.getVirtualSize();
        return new DecimalType(getSizeInMB(memory));
    }

    @Override
    public StringType getProcessPath(int pid) throws IllegalArgumentException {
        OSProcess process = getProcess(pid);
        return new StringType(process.getPath());
    }

    @Override
    public DecimalType getProcessThreads(int pid) throws IllegalArgumentException {
        OSProcess process = getProcess(pid);
        return new DecimalType(process.getThreadCount());
    }

    @Override
    public DecimalType getProcessUpTime(int pid) throws IllegalArgumentException {
        OSProcess process = getProcess(pid);
        return new DecimalType(process.getUpTime() / 1000);
    }

    @Override
    public StringType getProcessUser(int pid) throws IllegalArgumentException {
        OSProcess process = getProcess(pid);
        return new StringType(process.getUser());
    }

}
