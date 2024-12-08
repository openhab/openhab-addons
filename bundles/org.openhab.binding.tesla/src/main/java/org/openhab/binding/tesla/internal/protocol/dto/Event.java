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
package org.openhab.binding.tesla.internal.protocol.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Event} is a datastructure to capture
 * events sent by the Tesla vehicle.
 *
 * @author Karel Goderis - Initial contribution
 */
public class Event {
    @SerializedName("msg_type")
    public String msgType;
    public String value;
    public String tag;
    @SerializedName("error_type")
    public String errorType;
    public int connectionTimeout;
}
