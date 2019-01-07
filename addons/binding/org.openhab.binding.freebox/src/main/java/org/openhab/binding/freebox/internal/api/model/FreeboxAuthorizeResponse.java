/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
