/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ondilo.internal.dto;

/**
 * The {@link LastMeasure} DTO for representing Ondilo LastMeasures.
 *
 * @author MikeTheTux - Initial contribution
 */
public class LastMeasure {
    /*
     * "data_type": "temperature",
     * "value": 12.5,
     * "value_time": "2020-03-23T16:08:51+0000",
     * "is_valid": true,
     * "exclusion_reason": null
     */
    public String data_type;
    public double value;
    public String value_time;
    public boolean is_valid;
    public String exclusion_reason;
}
