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
package org.openhab.binding.sensibo.internal.dto.settimer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.sensibo.internal.dto.AbstractRequest;
import org.openhab.binding.sensibo.internal.dto.poddetails.AcStateDTO;

/**
 * All classes in the ..binding.sensibo.dto are data transfer classes used by the GSON mapper. This class reflects a
 * part of a request/response data structure.
 *
 * @author Arne Seime - Initial contribution.
 */
@NonNullByDefault
public class SetTimerRequest extends AbstractRequest {
    public final transient String podId; // Transient fields are ignored by gson
    public final AcStateDTO acState;
    public final int minutesFromNow;

    public SetTimerRequest(String podId, int minutesFromNow, AcStateDTO acState) {
        this.podId = podId;
        this.acState = acState;
        this.minutesFromNow = minutesFromNow;
    }

    @Override
    public String getRequestUrl() {
        return String.format("/v1/pods/%s/timer/", podId);
    }

    @Override
    public String getMethod() {
        return HttpMethod.PUT.asString();
    }
}
