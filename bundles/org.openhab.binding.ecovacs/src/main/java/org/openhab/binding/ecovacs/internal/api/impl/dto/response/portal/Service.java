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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public class Service {

    @SerializedName("jmq")
    private final String jmq;

    @SerializedName("mqs")
    private final String mqs;

    public Service(String jmq, String mqs) {
        this.jmq = jmq;
        this.mqs = mqs;
    }

    public String getJmq() {
        return jmq;
    }

    public String getMqs() {
        return mqs;
    }
}
