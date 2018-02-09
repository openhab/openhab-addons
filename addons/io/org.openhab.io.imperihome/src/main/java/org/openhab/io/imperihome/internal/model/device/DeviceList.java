/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.device;

import java.util.Collection;

/**
 * Device list holder.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DeviceList {

    private Collection<AbstractDevice> devices;

    public Collection<AbstractDevice> getDevices() {
        return devices;
    }

    public void setDevices(Collection<AbstractDevice> devices) {
        this.devices = devices;
    }

    @Override
    public String toString() {
        return "DeviceList{" + "devices=" + devices + '}';
    }

}
