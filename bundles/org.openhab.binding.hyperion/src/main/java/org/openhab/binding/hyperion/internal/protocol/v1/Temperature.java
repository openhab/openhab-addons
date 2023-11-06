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
package org.openhab.binding.hyperion.internal.protocol.v1;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Temperature} is a POJO for temperature correction on the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class Temperature {

    @SerializedName("correctionValues")
    private List<Integer> correctionValues = null;

    @SerializedName("id")
    private String id;

    public List<Integer> getCorrectionValues() {
        return correctionValues;
    }

    public void setCorrectionValues(List<Integer> correctionValues) {
        this.correctionValues = correctionValues;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
