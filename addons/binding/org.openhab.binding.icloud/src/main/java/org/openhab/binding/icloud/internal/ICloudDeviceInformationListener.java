package org.openhab.binding.icloud.internal;

import java.util.List;

import org.openhab.binding.icloud.internal.json.DeviceInformation;

/**
 * Classes that implement this interface are interested in device information updates.
 *
 * @author Patrik Gfeller
 *
 */
public interface ICloudDeviceInformationListener {
    void deviceInformationUpdate(List<DeviceInformation> deviceInformationList);
}
