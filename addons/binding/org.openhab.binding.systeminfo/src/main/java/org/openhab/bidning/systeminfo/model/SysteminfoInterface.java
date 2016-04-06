package org.openhab.bidning.systeminfo.model;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * {@link SysteminfoInterface} defines the methods needed to provide this binding with the required system information.
 *
 * @author svilen.valkanov
 *
 */
// TODO javadoc
public interface SysteminfoInterface {
    // Os info

    public StringType getOsFamily();

    public StringType getOsManufacturer();

    public StringType getOsVersion();

    // CPU info
    public StringType getCpuName();

    /**
     * Get description about the CPU e.g(model,family, vendor, serial number, identifier, architecture(32bit or
     * 64bit)";)
     * 
     * @return
     */
    public StringType getCpuDescription();

    /**
     * Get the number of logical CPUs/cores available for processing.
     *
     * @return
     */
    public DecimalType getCpuLogicalCores();

    /**
     * Get the number of physical CPUs/cores available for processing.
     *
     * @return
     */
    public DecimalType getCpuPhysicalCores();

    /**
     * Get the average CPU load for all logical processors
     *
     * @return the load as percentage value /0-100/
     */
    public DecimalType getCpuLoad();

    // Memory info
    // TODO maybe methods for free amount as percentage should be added
    /**
     * Returns the total size of RAM memory installed on the machine.
     *
     * @return memory size in MB
     */
    public DecimalType getMemoryTotal();

    /**
     * Returns the available size of RAM memory installed on the machine.
     *
     * @return memory size in MB
     */
    public DecimalType getMemoryAvailable();

    /**
     * Returns the used size of RAM memory installed on the machine.
     *
     * @return memory size in MB
     */
    public DecimalType getMemoryUsed();

    // Storage info
    /**
     * Returns the total space of the storage device.
     *
     * @param deviceIndex - the index of the physical device if more than one exist.
     * @return storage size in MB
     */
    public DecimalType getStorageTotal(int deviceIndex);

    public DecimalType getStorageAvailable(int deviceIndex);

    public DecimalType getStorageUsed(int deviceIndex);

    public StringType getStorageName(int deviceIndex);

    public StringType getStorageDescription(int deviceIndex);

    // Network info
    /**
     * Get the Host IP address of the network.
     *
     * @param networkIndex
     * @return 32-bit IPv4 address
     */
    public StringType getNetworkIp(int networkIndex);

    /**
     * Get the name of this network.
     *
     * @param networkIndex
     * @return
     */
    public StringType getNetworkName(int networkIndex);

    /**
     * Get human readable description of the network device.
     *
     * @param networkIndex
     * @return
     */
    public StringType getNetworkAdapterName(int networkIndex);

    // Display info
    /**
     * Get information about the display device as product number, manufacturer, serial number, width and height in cm";
     *
     * @param deviceIndex
     * @return
     */
    public StringType getDisplayInformation(int deviceIndex);

    // Sensors info
    /**
     * Get the information from the CPU temperature sensors.
     *
     * @return CPU Temperature in degrees Celsius if available, 0 otherwise.
     */
    public DecimalType getSensorsCpuTemperature();

    /**
     * Get the information for the CPU voltage.
     *
     * @return CPU Voltage in Volts if available, 0 otherwise.
     */
    public DecimalType getSensorsCpuVoltage();

    /**
     *
     * @param deviceIndex
     * @return Speed in rpm for the device or 0 if unable to measure fan speed
     */
    public DecimalType getSensorsFanSpeed(int deviceIndex);

    // Battery info
    /**
     * Get estimated time remaining on the power source.
     *
     * @param deviceIndex
     * @return minutes remaining charge
     */
    public DecimalType getBatteryRemainingTime(int deviceIndex);

    /**
     * Battery remaining capacity.
     *
     * @param deviceIndex
     * @return percentage value /0-100/
     */
    public DecimalType getBatteryRemainingCapacity(int deviceIndex);

    public StringType getBatteryName(int deviceIndex);

}
