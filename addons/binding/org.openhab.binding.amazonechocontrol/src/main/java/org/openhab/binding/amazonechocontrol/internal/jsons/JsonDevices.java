package org.openhab.binding.amazonechocontrol.internal.jsons;

public class JsonDevices {

    public class Device {
        public String accountName;
        public String serialNumber;
        public String deviceOwnerCustomerId;
        public String deviceAccountId;
        public String deviceFamily;
        public String deviceType;
        public boolean online;

    }

    public Device[] devices;
}
