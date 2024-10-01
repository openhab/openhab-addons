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
package org.openhab.binding.pegelonline.internal.dto;

/**
 * {@link Station} DTO for measurement Station
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Station {
    public String uuid; // "47174d8f-1b8e-4599-8a59-b580dd55bc87",
    public long number; // "48900237",
    public String shortname; // "EITZE",
    public String longname; // "EITZE",
    public double km; // 9.56,
    public String agency; // : "WSA VERDEN",
    public double longitude; // 9.27676943537587,
    public double latitude; // 52.90406541008721,
    public Water water;
}
