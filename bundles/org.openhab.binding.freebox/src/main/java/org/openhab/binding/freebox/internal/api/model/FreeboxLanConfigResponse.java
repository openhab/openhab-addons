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
 * The {@link FreeboxLanConfigResponse} is the Java class used to map the
 * response of the LAN configuration API
 * https://dev.freebox.fr/sdk/os/lan/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxLanConfigResponse extends FreeboxResponse<FreeboxLanConfig> {
    @Override
    public void evaluate() throws FreeboxException {
        super.evaluate();
        if (getResult() == null) {
            throw new FreeboxException("Missing result data in LAN configuration API response", this);
        }
    }
}
