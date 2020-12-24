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
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The {@link Module} class is an object model class that
 * mirrors the XML structure provided by the Plugwise Home Automation
 * controller for a Plugwise module.
 * It implements the {@link PlugwiseComparableDate} interface and
 * extends the abstract class {@link PlugwiseBaseModel}.
 * 
 * @author B. van Wetten - Initial contribution
 */
@XStreamAlias("module")
public class Module extends PlugwiseBaseModel implements PlugwiseComparableDate<Module> {

    @XStreamImplicit(itemFieldName = "service", keyFieldName = "id")
    private Services services;

    @XStreamAlias("vendor_name")
    private String vendorName;

    @XStreamAlias("vendor_model")
    private String vendorModel;

    @XStreamAlias("hardware_version")
    private String hardwareVersion;

    @XStreamAlias("firmware_version")
    private String firmwareVersion;

    public String getVendorName() {
        return vendorName;
    }

    public String getVendorModel() {
        return vendorModel;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    public int compareDateWith(Module hasModifiedDate) {
        return this.getModifiedDate().compareTo(hasModifiedDate.getModifiedDate());
    }

    @Override
    public boolean isNewerThan(Module hasModifiedDate) {
        return compareDateWith(hasModifiedDate) > 0;
    }

    @Override
    public boolean isOlderThan(Module hasModifiedDate) {
        return compareDateWith(hasModifiedDate) < 0;
    }
}
