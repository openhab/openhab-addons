/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity.link;

import org.openhab.binding.innogysmarthome.internal.client.entity.Message;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;

import com.google.api.client.util.Key;

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

    @Key("value")
    private String value;

    public Link(String value) {
        this.value = value;
    }

    public Link() {
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the Type of the {@link Link}.
     *
     * @return {@link #LINK_TYPE_CAPABILITY},{@link #LINK_TYPE_DEVICE}, {@link #LINK_TYPE_MESSAGE},
     *         {@link #LINK_TYPE_SHC} or {@link #LINK_TYPE_UNKNOWN}
     */
    public String getLinkType() {
        String linkValue = getValue();
        if (linkValue.startsWith(LINK_TYPE_CAPABILITY)) {
            return LINK_TYPE_CAPABILITY;
        } else if (linkValue.startsWith(LINK_TYPE_DEVICE)) {
            return LINK_TYPE_DEVICE;
        } else if (linkValue.startsWith(LINK_TYPE_MESSAGE)) {
            return LINK_TYPE_MESSAGE;
        } else if (linkValue.startsWith(LINK_TYPE_SHC)) {
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
    public String getId() {
        if (this.value != null) {
            String linkType = getLinkType();
            if (linkType != null && !linkType.equals(Link.LINK_TYPE_UNKNOWN) && !linkType.equals(Link.LINK_TYPE_SHC)) {
                return this.value.replace(linkType, "");
            }
        }
        return null;
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Capability}.
     *
     * @return
     */
    public boolean isTypeCapability() {
        return getLinkType().equals(LINK_TYPE_CAPABILITY);
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Device}.
     *
     * @return
     */
    public boolean isTypeDevice() {
        return getLinkType().equals(LINK_TYPE_DEVICE);
    }

    /**
     * Returns true, if the {@link Link} points to a {@link Message}.
     *
     * @return
     */
    public boolean isTypeMessage() {
        return getLinkType().equals(LINK_TYPE_MESSAGE);
    }

    /**
     * Returns true, if the {@link Link} points to a SHC.
     *
     * @return
     */
    public boolean isTypeSHC() {
        return getLinkType().equals(LINK_TYPE_SHC);
    }

    /**
     * Returns true, if the {@link Link} points to something unknown.
     *
     * @return
     */
    public boolean isTypeUnknown() {
        return getLinkType().equals(LINK_TYPE_UNKNOWN);
    }
}
