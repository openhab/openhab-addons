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
package org.openhab.binding.juicenet.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link JuiceNetApiInfo } implements DTO for Info
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApiInfo {
    public String name = "";
    public String address = "";
    public String city = "";
    public String zip = "";
    public String country_code = "";
    public String ip = "";
    public int gascost;
    public int mpg;
    public int ecost;
    public int whpermile;
    public String timeZoneId = "";
    public int amps_wire_rating;
    public int amps_unit_rating;
    public JuiceNetApiCar[] cars = {};
}
