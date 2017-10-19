/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.blebox.devices;

/**
 * The {@link DeviceInfo} class defines common device info repsonse
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class DeviceInfo {
    public String deviceName;
    public String type;
    // Firmware version
    public String fv;
    // Hardware version
    public String hv;

    public String id;

    public String ip;

}
