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
package org.openhab.binding.mynice.internal.xml.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Device {
    public enum DeviceType {
        SECTIONAL,
        UP_AND_OVER,
        SLIDING,
        BARRIER,
        SWING;
    }

    @XStreamAsAttribute
    public String id;

    @XStreamAlias("Type")
    public DeviceType type;

    @XStreamAlias("Manuf")
    public String manuf;

    @XStreamAlias("Prod")
    public String prod;

    @XStreamAlias("Desc")
    public String desc;

    @XStreamAlias("VersionHW")
    public String versionHW;

    @XStreamAlias("VersionFW")
    public String versionFW;

    @XStreamAlias("SerialNr")
    public String serialNr;

    @XStreamAlias("Properties")
    public Properties properties;
}
