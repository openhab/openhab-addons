/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
