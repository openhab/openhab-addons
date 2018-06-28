/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Parses a delete device event received from a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DeleteDevicesParser extends CommonRpcParser<Object[], List<String>> {
    @Override
    public List<String> parse(Object[] message) throws IOException {
        List<String> adresses = new ArrayList<String>();
        if (message != null && message.length > 1) {
            Object[] data = (Object[]) message[1];
            for (int i = 0; i < message.length; i++) {
                String address = getSanitizedAddress(data[i]);
                boolean isDevice = !StringUtils.contains(address, ":")
                        && !StringUtils.startsWithIgnoreCase(address, "BidCos");
                if (isDevice) {
                    adresses.add(address);
                }

            }
        }
        return adresses;
    }

}
