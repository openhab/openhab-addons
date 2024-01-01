/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.TclScriptDataEntry;
import org.openhab.binding.homematic.internal.model.TclScriptDataList;

/**
 * Parses a TclRega script result containing names for devices.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class CcuLoadDeviceNamesParser extends CommonRpcParser<TclScriptDataList, Void> {
    private Collection<HmDevice> devices;

    public CcuLoadDeviceNamesParser(Collection<HmDevice> devices) {
        this.devices = devices;
    }

    @Override
    public Void parse(TclScriptDataList resultList) throws IOException {
        if (resultList.getEntries() != null) {
            Map<String, HmDevice> devicesByAddress = new HashMap<>();
            for (HmDevice device : devices) {
                devicesByAddress.put(device.getAddress(), device);
            }

            for (TclScriptDataEntry entry : resultList.getEntries()) {
                HmDevice device = devicesByAddress.get(getSanitizedAddress(entry.name));
                if (device != null) {
                    device.setName(entry.value);
                }
            }
        }
        return null;
    }
}
