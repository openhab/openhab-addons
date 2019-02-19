/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sensibosky.internal.http;

import java.util.List;

import org.openhab.binding.sensibosky.internal.model.AcStateRead;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SensiboAcStateResponse} class represents the response of the ac state.
 *
 * @author Robert Kaczmarczyk - Initial contribution
 */
public class SensiboAcStateResponse {
    @SerializedName("status")
    public String status;
    @SerializedName("moreResults")
    public boolean moreResults;
    @SerializedName("result")
    public List<AcStateRead> result;
}
