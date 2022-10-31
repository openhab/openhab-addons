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
 * {@link JuiceNetApiCar } implements DTO for Car API call
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApiCar {
    public int car_id;
    public String description = "";
    public int battery_size_wh;
    public int battery_range_m;
    public int charging_rate_w;
    public String model_id = "";
}
