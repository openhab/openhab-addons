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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The {@link LocationsArray} class is an object model class that mirrors the XML
 * structure provided by the Plugwise Home Automation controller for the
 * collection of Plugwise locations/zones.
 * 
 * @author L. Siepel - Initial contribution
 */

@XStreamAlias("locations")
public class LocationsArray {

    @XStreamImplicit
    public Location[] items;
}
