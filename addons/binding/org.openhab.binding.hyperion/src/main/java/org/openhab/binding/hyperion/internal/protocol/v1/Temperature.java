/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
