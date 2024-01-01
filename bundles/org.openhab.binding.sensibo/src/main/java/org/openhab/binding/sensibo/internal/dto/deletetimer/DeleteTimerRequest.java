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
package org.openhab.binding.sensibo.internal.dto.deletetimer;

import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.sensibo.internal.dto.AbstractRequest;

/**
 * All classes in the ..binding.sensibo.dto are data transfer classes used by the GSON mapper. This class reflects a
 * part of a request/response data structure.
 *
 * @author Arne Seime - Initial contribution.
 */
public class DeleteTimerRequest extends AbstractRequest {
    public final transient String podId; // Transient fields are ignored by gson

    public DeleteTimerRequest(String podId) {
        this.podId = podId;
    }

    @Override
    public String getRequestUrl() {
        return String.format("/v1/pods/%s/timer/", podId);
    }

    @Override
    public String getMethod() {
        return HttpMethod.DELETE.asString();
    }
}
