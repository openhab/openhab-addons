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
package org.openhab.binding.freebox.internal.api.model;

import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.RelativePath;

/**
 * The {@link SambaConfigResponse} is the Java class used to map the
 * response of the Samba configuration API
 * https://dev.freebox.fr/sdk/os/network_share/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@RelativePath(relativeUrl = "netshare/samba/", retryAuth = true)
public class SambaConfigResponse extends FreeboxResponse<SambaConfig> {
    @Override
    public void evaluate() throws FreeboxException {
        super.evaluate();
        if (getResult() == null) {
            throw new FreeboxException("Missing result data in Samba configuration API response", this);
        }
        if (getResult().isFileShareEnabled() == null) {
            throw new FreeboxException("No file sharing status in response", this);
        }
        if (getResult().isPrintShareEnabled() == null) {
            throw new FreeboxException("No printer sharing status in response", this);
        }
    }
}
