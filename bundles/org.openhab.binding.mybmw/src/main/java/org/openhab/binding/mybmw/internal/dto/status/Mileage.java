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
package org.openhab.binding.mybmw.internal.dto.status;

/**
 * The {@link Mileage} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Mileage {
    public int mileage;// ": 31537,
    public String units;// ": "km",
    public String formattedMileage;// ": "31537"
}
