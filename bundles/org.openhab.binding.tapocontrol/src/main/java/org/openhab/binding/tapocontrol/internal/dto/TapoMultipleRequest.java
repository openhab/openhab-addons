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
package org.openhab.binding.tapocontrol.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * {@TapoMultiRequest} holds multi-request-data sent to device
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Christian Wild - Changed SubRequest top MultiRequest
 */
@NonNullByDefault
public class TapoMultipleRequest {
    @Expose
    private List<TapoRequest> requests = new ArrayList<>();

    public TapoMultipleRequest() {
    }

    public void addRequest(TapoRequest request) {
        requests.add(request);
    }

    /***********************************************
     * RETURN VALUES
     **********************************************/

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        return new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }
}
