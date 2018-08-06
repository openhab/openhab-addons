/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrol.internal;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
public final class UpnpProtocolMatcher {

    private UpnpProtocolMatcher() {
    }

    public static boolean testProtocol(String protocol, List<String> protocolSet) {
        return protocolSet.contains(protocol);
    }

    public static boolean testProtocolList(List<String> protocolList, List<String> protocolSet) {
        return protocolList.stream().anyMatch(p -> testProtocol(p, protocolSet));
    }

    public static List<String> getProtocols(List<String> protocolList, List<String> protocolSet) {
        return protocolList.stream().filter(p -> testProtocol(p, protocolSet)).collect(Collectors.toList());
    }
}
