/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
