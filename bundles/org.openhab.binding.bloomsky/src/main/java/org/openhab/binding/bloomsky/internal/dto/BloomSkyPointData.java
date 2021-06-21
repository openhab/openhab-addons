/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bloomsky.internal.dto;

/**
 * The {@link BloomSkyPointData} is the Java class used to map the JSON response to an BloomSky API request.
 * This refers to inside sensors that were discontinued and no longer support.
 * Holding spot to maintain consistency with JSON response that includes this element.
 *
 * @author dschoepel - Initial contribution
 *
 */
public class BloomSkyPointData {

    @Override
    public String toString() {
        return "Point{}";
    }
}
