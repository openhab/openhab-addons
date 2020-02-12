/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 */

package org.openhab.binding.lightwaverf.internal.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */
public class FeatureStatus {

@SerializedName("featureId")
@Expose
private String featureId;
@SerializedName("value")
@Expose
private Integer value;

public String getFeatureId() {
return featureId;
}

public void setFeatureId(String featureId) {
this.featureId = featureId;
}

public Integer getValue() {
return value;
}

public void setValue(Integer value) {
this.value = value;
}
}
