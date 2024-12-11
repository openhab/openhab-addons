package org.openhab.binding.smartthings.internal.dto;

public class SmartthingsDeviceHub {

    public String hubEui;
    public String firmwareVersion;

    public record hubDrivers(String driverVersion, String driverId, String channelId) {
    }

    public hubDrivers[] hubDrivers;

    public class hubData {
        public String zwaveStaticDsk;
        public Boolean zwaveS2;

        public String hardwareType;
        public String hardwareId;
        public String zigbeeFirmware;
        public Boolean zigbee3;
        public String zigbeeOta;
        public String otaEnable;
        public Boolean zigbeeUnsecureRejoin;
        public Boolean zigbeeRequiresExternalHardware;
        public Boolean threadRequiresExternalHardware;
        public String failoverAvailability;
        public String primarySupportAvailability;
        public String secondarySupportAvailability;
        public String zigbeeAvailability;
        public String zwaveAvailability;
        public String threadAvailability;
        public String lanAvailability;
        public String matterAvailability;
        public String localVirtualDeviceAvailability;
        public String childDeviceAvailability;
        public String edgeDriversAvailability;
        public String hubReplaceAvailability;
        public String hubLocalApiAvailability;
        public Boolean zigbeeManualFirmwareUpdateSupported;
        public Boolean matterRendezvousHedgeSupported;
        public String matterSoftwareComponentVersion;
        public String matterDeviceDiagnosticsAvailability;
        public String zigbeeDeviceDiagnosticsAvailability;
        public String hedgeTlsCertificate;
        public String zigbeeChannel;
        public String zigbeePanId;
        public String zigbeeEui;
        public String zigbeeNodeID;
        public String zwaveNodeID;
        public String zwaveHomeID;
        public String zwaveSucID;
        public String zwaveVersion;
        public String zwaveRegion;
        public String macAddress;
        public String localIP;
        public Boolean zigbeeRadioFunctional;
        public Boolean zwaveRadioFunctional;
        public Boolean zigbeeRadioEnabled;
        public Boolean zwaveRadioEnabled;
        public Boolean zigbeeRadioDetected;
        public Boolean zwaveRadioDetected;
    }

    public hubData hubData;
}
