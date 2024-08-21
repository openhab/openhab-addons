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
package org.openhab.binding.sonos.internal;

import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains the resource meta data within a browse response result
 * {@code "<r:resMD>..</r:resMD>"}. This is used for SONOS favorites.
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public class SonosResourceMetaData implements Serializable {

    private static final long serialVersionUID = 7438424501599637712L;

    String id;
    String parentId;
    String title;
    String upnpClass;
    String desc;

    public SonosResourceMetaData(String id, String parentId, String title, String upnpClass, String desc) {
        super();
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.upnpClass = upnpClass;
        this.desc = desc;
    }

    /**
     * The parent id for the resource meta data
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * The parent id for the resource meta data
     *
     * @return
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * title from the resource meta data
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * The upnp class for the resource meta data. This can be different from the
     * parent meta data class and should be used to match the play type over the
     * parent value.
     *
     * @return
     */
    public String getUpnpClass() {
        return upnpClass;
    }

    /**
     * The desc text for the resource meta data. This contains the service login
     * id for streaming accounts (pandora, spotify, etc..)
     *
     * @return
     */
    public String getDesc() {
        return desc;
    }
}
