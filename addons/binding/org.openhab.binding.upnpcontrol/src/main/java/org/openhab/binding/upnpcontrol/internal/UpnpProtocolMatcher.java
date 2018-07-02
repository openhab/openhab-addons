/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrol.internal;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
public final class UpnpProtocolMatcher {

    private UpnpProtocolMatcher() {
    }

    public static boolean testProtocol(String protocol, List<String> protocolSet) {
        for (String p : protocolSet) {
            if (protocol.equals(p)) {
                return true;
            }
        }
        return false;
    }

    public static boolean testProtocolList(List<String> protocolList, List<String> protocolSet) {
        for (String p : protocolList) {
            if (testProtocol(p, protocolSet)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getProtocols(List<String> protocolList, List<String> protocolSet) {
        List<String> list = new ArrayList<>();
        for (String protocol : protocolList) {
            if (testProtocol(protocol, protocolSet)) {
                list.add(protocol);
            }
        }
        return list;
    }
}
