/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public final class UpnpProtocolMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpnpProtocolMatcher.class);

    private UpnpProtocolMatcher() {
    }

    /**
     * Test if an UPnP protocol matches the object class. This method is used to filter resources for the primary
     * resource.
     *
     * @param protocol format: {@code <protocol>:<network>:<contentFormat>:<additionalInfo>}
     *            e.g. http-get:*:audio/mpeg:*
     * @param objectClass e.g. object.item.audioItem.musicTrack
     * @return true if protocol matches objectClass
     */
    public static boolean testProtocol(String protocol, String objectClass) {
        String[] protocolDetails = protocol.split(":");
        if (protocolDetails.length < 3) {
            LOGGER.debug("Protocol string {} not valid", protocol);
            return false;
        }
        String protocolType = protocolDetails[2].toLowerCase();
        int index = protocolType.indexOf("/");
        if (index <= 0) {
            LOGGER.debug("Protocol string {} not valid", protocol);
            return false;
        }
        protocolType = protocolType.substring(0, index);

        String[] objectClassDetails = objectClass.split("\\.");
        if (objectClassDetails.length < 3) {
            LOGGER.debug("Object class {} not valid", objectClass);
            return false;
        }
        String objectType = objectClassDetails[2].toLowerCase();

        LOGGER.debug("Matching protocol type '{}' with object type '{}'", protocolType, objectType);
        return objectType.startsWith(protocolType);
    }

    /**
     * Test if a UPnP protocol is in a set of protocols.
     * Ignore vendor specific additionalInfo part in UPnP protocol string.
     * Do all comparisons in lower case.
     *
     * @param protocol format: {@code <protocol>:<network>:<contentFormat>:<additionalInfo>}
     * @param protocolSet
     * @return true if protocol in protocolSet
     */
    public static boolean testProtocol(String protocol, List<String> protocolSet) {
        int index = protocol.lastIndexOf(":");
        if (index <= 0) {
            LOGGER.debug("Protocol {} not valid", protocol);
            return false;
        }
        String p = protocol.toLowerCase().substring(0, index);
        List<String> pSet = new ArrayList<>();
        protocolSet.forEach(f -> {
            int i = f.lastIndexOf(":");
            if (i <= 0) {
                LOGGER.debug("Protocol {} from set not valid", f);
            } else {
                pSet.add(f.toLowerCase().substring(0, i));
            }
        });
        LOGGER.trace("Testing {} in {}", p, pSet);
        return pSet.contains(p);
    }

    /**
     * Test if any of the UPnP protocols in protocolList can be found in a set of protocols.
     *
     * @param protocolList
     * @param protocolSet
     * @return true if one of the protocols in protocolSet
     */
    public static boolean testProtocolList(List<String> protocolList, List<String> protocolSet) {
        return protocolList.stream().anyMatch(p -> testProtocol(p, protocolSet));
    }

    /**
     * Return all UPnP protocols from protocolList that are part of a set of protocols.
     *
     * @param protocolList
     * @param protocolSet
     * @return sublist of protocolList
     */
    public static List<String> getProtocols(List<String> protocolList, List<String> protocolSet) {
        return protocolList.stream().filter(p -> testProtocol(p, protocolSet)).collect(Collectors.toList());
    }
}
