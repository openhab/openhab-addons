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
package org.openhab.binding.freebox.internal.api.model;

import org.openhab.binding.freebox.internal.api.FreeboxException;

/**
 * The {@link FreeboxAuthorizeResponse} is the Java class used to map the
 * response of the request authorization API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxAuthorizeResponse extends FreeboxResponse<FreeboxAuthorizeResult> {
    @Override
    public void evaluate() throws FreeboxException {
        super.evaluate();
        if (getResult() == null) {
            throw new FreeboxException("Missing result data in request authorization API response", this);
        }
        if ((getResult().getAppToken() == null) || getResult().getAppToken().isEmpty()) {
            throw new FreeboxException("No app token in response", this);
        }
        if (getResult().getTrackId() == null) {
            throw new FreeboxException("No track id in response", this);
        }
    }
}
