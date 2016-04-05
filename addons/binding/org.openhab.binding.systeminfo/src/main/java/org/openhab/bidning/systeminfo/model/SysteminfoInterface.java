package org.openhab.bidning.systeminfo.model;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;

public interface SysteminfoInterface {
    // Os info
    public StringType getOsFamily();

    public StringType getOsManufacturer();

    public StringType getOsVersion();

    // CPU info
    public StringType getCpuName();

    public StringType getCpuDescription();

    public DecimalType getCpuLogicalProcCount();

    public DecimalType getCpuPhysicalProcCount();

    public DecimalType getCpuLoad();

    // Memory info
    public DecimalType getMemoryTotal();

    public DecimalType getMemoryAvailable();

    public DecimalType getMemoryUsed();

    // Storage info
    public DecimalType getStorageTotal(int index);

    public DecimalType getStorageAvailable(int index);

    public DecimalType getStorageUsed(int index);

    public StringType getStorageName(int index);

    public StringType getStorageDescription(int index);

    // Network info
    public StringType getNetworkIP(int index);

    public StringType getNetworkName(int index);

    public StringType getNetworkAdapterName(int index);

    // Display info
    public StringType getDisplayInfo(int index);

    // Sensors info
    public DecimalType getSensorCpuTemp();

    public DecimalType getSensorCpuVoltage();

    public DecimalType getSensorFanSpeed(int index);

    // Battery info

    public DecimalType getBatteryRemainingTime(int index);

    public DecimalType getBatteryRemainingCapacity(int index);

    public StringType getBatteryName(int index);

}
