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
 */
package org.openhab.binding.paradoxalarm.internal.communication;

/**
 * The {@link Response}. The response which is returned after receiving data from socket.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class Response implements IResponse {

    private IRequest request;
    private byte[] payload;

    public Response(IRequest request, byte[] content) {
        this.request = request;
        this.payload = content;
    }

    @Override
    public RequestType getType() {
        return request.getType();
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public IRequest getRequest() {
        return request;
    }
}
