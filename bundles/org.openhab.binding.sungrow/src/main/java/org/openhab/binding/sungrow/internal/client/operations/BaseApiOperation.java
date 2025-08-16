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
package org.openhab.binding.sungrow.internal.client.operations;

import org.openhab.binding.sungrow.internal.client.APIOperation;
import org.openhab.binding.sungrow.internal.client.dto.BaseRequest;

/**
 * @author Christian Kemper - Initial contribution
 */
abstract class BaseApiOperation<REQ extends BaseRequest, RES> implements APIOperation<REQ, RES> {

    private final String path;
    private REQ request;
    private RES response;

    protected BaseApiOperation(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public REQ getRequest() {
        return request;
    }

    public void setRequest(REQ request) {
        this.request = request;
    }

    @Override
    public void setResponse(RES response) {
        this.response = response;
    }

    @Override
    public RES getResponse() {
        return response;
    }
}
