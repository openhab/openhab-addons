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
package org.openhab.binding.icloud.internal.handler.dto.json.response;

/**
 * Serializable class to parse json response received from the Apple server.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ICloudServerContextTimezone {
    private int currentOffset;

    private int previousOffset;

    private long previousTransition;

    private String tzCurrentName;

    private String tzName;

    public int getCurrentOffset() {
        return this.currentOffset;
    }

    public int getPreviousOffset() {
        return this.previousOffset;
    }

    public long getPreviousTransition() {
        return this.previousTransition;
    }

    public String getTzCurrentName() {
        return this.tzCurrentName;
    }

    public String getTzName() {
        return this.tzName;
    }
}
