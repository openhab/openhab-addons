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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * @author B. van Wetten - Initial contribution
 */
@XStreamAlias("domain_objects")
public class DomainObjects {

    @XStreamAlias("gateway")
    private GatewayInfo gatewayInfo;

    @XStreamImplicit(itemFieldName = "appliance", keyFieldName = "id")
    private Appliances appliances = new Appliances();

    @XStreamImplicit(itemFieldName = "location", keyFieldName = "id")
    private Locations locations = new Locations();

    @SuppressWarnings("unused")
    @XStreamImplicit(itemFieldName = "module", keyFieldName = "id")
    private Modules modules = new Modules();

    public GatewayInfo getGatewayInfo() {
        return gatewayInfo;
    }

    public Appliances getAppliances() {
        return appliances;
    }

    public Locations getLocations() {
        return locations;
    }

    public Appliances mergeAppliances(Appliances updatedAppliances) {
        if (updatedAppliances != null) {
            this.appliances.merge(updatedAppliances);
        }

        return this.appliances;
    }

    public Locations mergeLocations(Locations updatedLocations) {
        if (updatedLocations != null) {
            this.locations.merge(updatedLocations);
        }

        return this.locations;
    }
}
