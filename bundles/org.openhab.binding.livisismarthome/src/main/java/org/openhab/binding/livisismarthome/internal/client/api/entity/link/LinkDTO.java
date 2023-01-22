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
package org.openhab.binding.livisismarthome.internal.client.api.entity.link;

/**
 * Defines the data structure for a {@link LinkDTO}. This is the basic component used to link different data types in
 * the
 * LIVISI API.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class LinkDTO {

    public static final String LINK_TYPE_CAPABILITY = "/capability/";
    public static final String LINK_TYPE_DEVICE = "/device/";
    public static final String LINK_TYPE_MESSAGE = "/message/";
    public static final String LINK_TYPE_SHC = "/desc/device/SHC.RWE/";
    public static final String LINK_TYPE_UNKNOWN = "unknown";

    /**
     * Returns the Type of the {@link LinkDTO}.
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
     * Returns the id of the {@link LinkDTO} or null, if the link does not have an id or even no value.
     *
     * @return String the id of the link or null
     */
    public static String getId(String link) {
        if (link != null) {
            final String linkType = getLinkType(link);
            if (linkType != null && !LinkDTO.LINK_TYPE_UNKNOWN.equals(linkType)
                    && !LinkDTO.LINK_TYPE_SHC.equals(linkType)) {
                return link.replace(linkType, "");
            }
        }
        return null;
    }

    /**
     * Returns true, if the {@link LinkDTO} points to a
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO}.
     *
     * @return true if the link points to a capability, otherwise false
     */
    public static boolean isTypeCapability(String link) {
        return LINK_TYPE_CAPABILITY.equals(LinkDTO.getLinkType(link));
    }

    /**
     * Returns true, if the {@link LinkDTO} points to a
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO}.
     *
     * @return true if the link points to a device, otherwise false
     */
    public static boolean isTypeDevice(String link) {
        return LINK_TYPE_DEVICE.equals(LinkDTO.getLinkType(link));
    }

    /**
     * Returns true, if the {@link LinkDTO} points to a
     * {@link org.openhab.binding.livisismarthome.internal.client.api.entity.message.MessageDTO}.
     *
     * @return true if the link points to a message, otherwise false
     */
    public static boolean isTypeMessage(String link) {
        return LINK_TYPE_MESSAGE.equals(LinkDTO.getLinkType(link));
    }

    /**
     * Returns true, if the {@link LinkDTO} points to a SHC.
     *
     * @return true if the link points to a SHC bridge device, otherwise false
     */
    public static boolean isTypeSHC(String link) {
        return LINK_TYPE_SHC.equals(LinkDTO.getLinkType(link));
    }

    /**
     * Returns true, if the {@link LinkDTO} points to something unknown.
     *
     * @return true if the link points to something unknown, otherwise false
     */
    public static boolean isTypeUnknown(String link) {
        return LINK_TYPE_UNKNOWN.equals(LinkDTO.getLinkType(link));
    }
}
