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
package org.openhab.binding.lgthinq.lgservices.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link MonitoringBinaryProtocol}
 *
 * @author Nemer Daud - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NonNullByDefault
public class MonitoringBinaryProtocol {
    @JsonProperty("startByte")
    public int startByte;
    @JsonProperty("length")
    public int length;
    @JsonProperty("value")
    public String fieldName = "";
}
