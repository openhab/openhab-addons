package org.openhab.binding.roku.internal;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.roku.internal.protocol.RokuCommunication;

public class RokuState {
    public StringType udn;
    public StringType serial_number;
    public StringType device_id;
    public StringType advertising_id;
    public StringType vendor_name;
    public StringType model_name;
    public StringType model_number;
    public StringType model_region;
    public StringType wifi_mac;
    public StringType ethernet_mac;
    public StringType network_type;
    public StringType user_device_name;
    public StringType software_version;
    public StringType software_build;
    public StringType secure_device;
    public StringType language;
    public StringType country;
    public StringType locale;
    public StringType time_zone;
    public StringType time_zone_offset;
    public StringType power_mode;
    public StringType supports_suspend;
    public StringType developer_enabled;
    public StringType search_enabled;
    public StringType voice_search_enabled;
    public StringType notifications_enabled;
    public StringType headphones_connected;
    public StringType active_app;
    public StringType application_menu;
    public RawType active_app_img;
    private final RokuCommunication communication;

    public RokuState(RokuCommunication communication) {
        this.communication = communication;
    }

    public void updateDeviceInformation() throws IOException {
        communication.updateState(this);
    }

    public StringType getApplicationMenu() {
        return application_menu;
    }

    public RawType getActiveImage() {
        return active_app_img;
    }

    public StringType getUdn() {
        return udn;
    }

    public StringType getSerialNumber() {
        return serial_number;
    }

    public StringType getDeviceId() {
        return device_id;
    }

    public StringType getAdvertisingId() {
        return advertising_id;
    }

    public StringType getVendorName() {
        return vendor_name;
    }

    public StringType getModelName() {
        return model_name;
    }

    public StringType getModelNumber() {
        return model_number;
    }

    public StringType getModelRegion() {
        return model_region;
    }

    public StringType getWifiMac() {
        return wifi_mac;
    }

    public StringType getEthernetMac() {
        return ethernet_mac;
    }

    public StringType getNetworkType() {
        return network_type;
    }

    public StringType getUserDeviceName() {
        return user_device_name;
    }

    public StringType getSoftwareVersion() {
        return software_version;
    }

    public StringType getSoftwareBuild() {
        return software_build;
    }

    public StringType getSecureDevice() {
        return secure_device;
    }

    public StringType getLanguage() {
        return language;
    }

    public StringType getCountry() {
        return country;
    }

    public StringType getLocale() {
        return locale;
    }

    public StringType getTimeZone() {
        return time_zone;
    }

    public StringType getTimeZoneOffSet() {
        return time_zone_offset;
    }

    public StringType getPowerMode() {
        return power_mode;
    }

    public StringType getSupportSuspend() {
        return supports_suspend;
    }

    public StringType getDeveloperEnabled() {
        return developer_enabled;
    }

    public StringType getSearchEnabled() {
        return search_enabled;
    }

    public StringType getVoiceSearchEnabled() {
        return voice_search_enabled;
    }

    public StringType getNotificationsEnabled() {
        return notifications_enabled;
    }

    public StringType getHeadphonesConnected() {
        return headphones_connected;
    }

    public StringType getActive() {
        return active_app;
    }
}
