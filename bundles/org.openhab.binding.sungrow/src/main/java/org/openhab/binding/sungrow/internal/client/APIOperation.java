/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.client;

import org.openhab.binding.sungrow.internal.client.dto.BaseRequest;

/**
 * @author Christian Kemper - Initial contribution
 */
public interface APIOperation<REQ extends BaseRequest, RES> {

    String getPath();

    default Method getMethod() {
        return Method.POST;
    }

    REQ getRequest();

    void setResponse(RES response);

    RES getResponse();

    enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }
}
