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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * base class for a response-object from the API
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class BaseFroniusResponse {
    @SerializedName("Head")
    private Head head;

    public Head getHead() {
        if (head == null) {
            head = new Head();
        }
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }
}
