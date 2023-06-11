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
package org.openhab.binding.sensibo.internal.dto.setacstateproperty;

import org.openhab.binding.sensibo.internal.dto.AbstractRequest;

/**
 * All classes in the ..binding.sensibo.dto are data transfer classes used by the GSON mapper. This class reflects a
 * part of a request/response data structure.
 *
 * @author Arne Seime - Initial contribution.
 */
public class SetAcStatePropertyRequest extends AbstractRequest {
    public transient String podId; // Transient fields are ignored by gson
    public transient String property;
    public Object newValue;

    public SetAcStatePropertyRequest(String podId, String property, Object value) {
        this.podId = podId;
        this.property = property;
        this.newValue = value;
    }

    @Override
    public String getRequestUrl() {
        return String.format("/v2/pods/%s/acStates/%s", podId, property);
    }

    @Override
    public String getMethod() {
        return "PATCH";
    }
}
