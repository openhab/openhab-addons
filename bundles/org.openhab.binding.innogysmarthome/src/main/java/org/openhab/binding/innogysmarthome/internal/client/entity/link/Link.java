/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client.entity.link;

import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.message.Message;

/**
 * Defines the data structure for a {@link Link}. This is the basic component used to link different data types in the
 * innogy API.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Link {

    public static final String LINK_TYPE_CAPABILITY = "/capability/";
    public static final String LINK_TYPE_DEVICE = "/device/";
    public static final String LINK_TYPE_MESSAGE = "/message/";
    public static final String LINK_TYPE_SHC = "/desc/device/SHC.RWE/";
    public static final String LINK_TYPE_UNKNOWN = "unknown";

    /**
     * Returns the Type of the {@link Link}.
     *
     * @return {@link #LINK_TYPE_CAPABILITY},{@link #LINK_TYPE_DEVICE}, {@link #LINK_TYPE_MESSAGE},
     *         {@link #LINK_TYPE_SHC} or {@link #LINK_TYPE_UNKNOWN}
     */
    public static String getLinkType(String link) {
        if (link.startsWith(LINK_TYPE_CAPABILITY)) {
            return LINK_TYPE_CAPABILITY;
        } else if (link.startsWith(LINK_TYPE_DEVICE)) {
            return LINK_TYPE_DEVICE;
        } else if (link.startsWith(LINK_TYPE_MESSAGE)) {
            return LINK_TYPE_MESSAGE;
        } else if (link.startsWith(LINK_TYPE_SHC)) {
            return LINK_TYPE_SHC;
        } else {
            return LINK_TYPE_UNKNOWN;
        }
    }

    /**
     * Returns the id of the {@link Link} or null, if the link does not have an id or even no value.
     *
     * @return String the id of the link or null
     */
    public static String getId(String link) {
        if (link != null) {
            final String linkType = getLinkType(link);
            if (linkType != null && !Link.LINK_TYPE_UNKNOWN.equals(linkType) && !Link.LINK_TYPE_SHC.equals(linkType)) {
                return link.replace(linkType, "");
            }
        }
        return null;
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Capability}.
     *
     * @return
     */
    public static boolean isTypeCapability(String link) {
        return LINK_TYPE_CAPABILITY.equals(Link.getLinkType(link));
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Device}.
     *
     * @return
     */
    public static boolean isTypeDevice(String link) {
        return LINK_TYPE_DEVICE.equals(Link.getLinkType(link));
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Message}.
     *
     * @return
     */
    public static boolean isTypeMessage(String link) {
        return LINK_TYPE_MESSAGE.equals(Link.getLinkType(link));
    }

    /**
     * Returns true, if the {@link Link} points to a SHC.
     *
     * @return
     */
    public static boolean isTypeSHC(String link) {
        return LINK_TYPE_SHC.equals(Link.getLinkType(link));
    }

    /**
     * Returns true, if the {@link Link} points to something unknown.
     *
     * @return
     */
    public static boolean isTypeUnknown(String link) {
        return LINK_TYPE_UNKNOWN.equals(Link.getLinkType(link));
    }
}
