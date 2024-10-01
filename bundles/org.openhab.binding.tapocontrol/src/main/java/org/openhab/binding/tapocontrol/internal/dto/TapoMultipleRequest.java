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
package org.openhab.binding.tapocontrol.internal.dto;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

import io.reactivex.annotations.Nullable;

/**
 * Holds multi-request-data sent to device
 *
 * @author Christian Wild - Changed SubRequest top MultiRequest
 */
@NonNullByDefault
public record TapoMultipleRequest(@Expose String method, @Expose @Nullable Object params,
        @Expose long requestTimeMils) implements TapoBaseRequestInterface {

    public record SubRequest(@Expose List<TapoRequest> requests) {
    }

    public TapoMultipleRequest(TapoRequest... requests) {
        this(DEVICE_CMD_MULTIPLE_REQ, new SubRequest(Arrays.asList(requests)), System.currentTimeMillis());
    }

    public TapoMultipleRequest(List<TapoRequest> requests) {
        this(DEVICE_CMD_MULTIPLE_REQ, new SubRequest(requests), System.currentTimeMillis());
    }

    /***********************************************
     * RETURN VALUES
     **********************************************/

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
