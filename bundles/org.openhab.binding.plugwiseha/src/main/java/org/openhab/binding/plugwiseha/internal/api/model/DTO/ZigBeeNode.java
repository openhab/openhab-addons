/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.plugwiseha.internal.api.model.DTO;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The {@link ZigBeeNode} class is an object model class that
 * mirrors the XML structure provided by the Plugwise Home Automation
 * controller for a Plugwise ZigBeeNode.
 * It extends the abstract class {@link PlugwiseBaseModel}.
 * 
 * @author B. van Wetten - Initial contribution
 */
@XStreamAlias("ZigBeeNode")
public class ZigBeeNode extends PlugwiseBaseModel {

    private String type;
    private String reachable;

    @XStreamAlias("power_source")
    private String powerSource;

    @XStreamAlias("mac_address")
    private String macAddress;

    public String getType() {
        return type;
    }

    public String getReachable() {
        return reachable;
    }

    public String getPowerSource() {
        return powerSource;
    }

    public String getMacAddress() {
        return macAddress;
    }
}
