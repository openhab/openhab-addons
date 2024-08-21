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
package org.openhab.binding.mynice.internal.xml.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Interface {
    @XStreamAlias("Zone")
    public String zone;
    @XStreamAlias("DST")
    public String dst;
    @XStreamAlias("VersionHW")
    public String versionHW;
    @XStreamAlias("VersionFW")
    public String versionFW;
    @XStreamAlias("Manuf")
    public String manuf;
    @XStreamAlias("Prod")
    public String prod;
    @XStreamAlias("SerialNr")
    public String serialNr;
}
