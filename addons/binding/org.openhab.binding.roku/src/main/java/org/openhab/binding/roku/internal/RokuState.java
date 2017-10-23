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
    public StringType serialNumber;
    public StringType deviceId;
    public StringType vendorName;
    public StringType modelName;
    public StringType modelNumber;
    public StringType modelRegion;
    public StringType wifiMac;
    public StringType ethernetMac;
    public StringType networkType;
    public StringType userDeviceName;
    public StringType softwareVersion;
    public StringType softwareBuild;
    public StringType powerMode;
    public StringType headphonesConnected;
    public StringType activeApp;
    public StringType applicationMenu;
    public RawType activeAppImg;
    private final RokuCommunication communication;

    public RokuState(RokuCommunication communication) {
        this.communication = communication;
    }

    public void updateDeviceInformation() throws IOException {
        communication.updateState(this);
    }

    public StringType getApplicationMenu() {
        return applicationMenu;
    }

    public RawType getActiveImage() {
        return activeAppImg;
    }

    public String getUdn() {
        return udn.toFullString();
    }

    public String getSerialNumber() {
        return serialNumber.toFullString();
    }

    public String getDeviceId() {
        return deviceId.toFullString();
    }

    public String getVendorName() {
        return vendorName.toFullString();
    }

    public String getModelName() {
        return modelName.toFullString();
    }

    public String getModelNumber() {
        return modelNumber.toFullString();
    }

    public String getModelRegion() {
        return modelRegion.toFullString();
    }

    public String getWifiMac() {
        return wifiMac.toFullString();
    }

    public String getEthernetMac() {
        return ethernetMac.toFullString();
    }

    public String getNetworkType() {
        return networkType.toFullString();
    }

    public String getUserDeviceName() {
        return userDeviceName.toFullString();
    }

    public String getSoftwareVersion() {
        return softwareVersion + " build " + softwareBuild;
    }

    public String getPowerMode() {
        return powerMode.toFullString();
    }

    public String getHeadphonesConnected() {
        return headphonesConnected.toFullString();
    }

    public StringType getActive() {
        return activeApp;
    }
}
