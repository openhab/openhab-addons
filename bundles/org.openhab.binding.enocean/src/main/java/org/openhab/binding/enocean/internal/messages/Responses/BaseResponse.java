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
package org.openhab.binding.enocean.internal.messages.Responses;

import org.openhab.binding.enocean.internal.Helper;
import org.openhab.binding.enocean.internal.messages.Response;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class BaseResponse extends Response {

    public BaseResponse(Response response) {
        super(response.getPayload().length + response.getOptionalPayload().length, 0,
                Helper.concatAll(response.getPayload(), response.getOptionalPayload()));
    }
}
