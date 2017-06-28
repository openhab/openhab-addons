/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.internal;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.roku.internal.protocol.RokuCommunication;

/**
 * The {@link RokuState} class defines various pieces of state a Roku device may have that will be translated to
 * functionality the binding offers.
 *
 * @author Jarod Peters - Initial contribution
 * @auther Shawn Wilsher - Overhaul of channels and properties
 */
public class RokuState {
    public StringType udn;
    public StringType serial_number;
    public StringType device_id;
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
    public StringType power_mode;
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

    public String getUdn() {
        return udn.toFullString();
    }

    public String getSerialNumber() {
        return serial_number.toFullString();
    }

    public String getDeviceId() {
        return device_id.toFullString();
    }

    public String getVendorName() {
        return vendor_name.toFullString();
    }

    public String getModelName() {
        return model_name.toFullString();
    }

    public String getModelNumber() {
        return model_number.toFullString();
    }

    public String getModelRegion() {
        return model_region.toFullString();
    }

    public String getWifiMac() {
        return wifi_mac.toFullString();
    }

    public String getEthernetMac() {
        return ethernet_mac.toFullString();
    }

    public String getNetworkType() {
        return network_type.toFullString();
    }

    public String getUserDeviceName() {
        return user_device_name.toFullString();
    }

    public String getSoftwareVersion() {
        return software_version + " build " + software_build;
    }

    public String getPowerMode() {
        return power_mode.toFullString();
    }

    public String getHeadphonesConnected() {
        return headphones_connected.toFullString();
    }

    public StringType getActive() {
        return active_app;
    }
}
