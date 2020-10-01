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
import org.openhab.binding.freebox.internal.api.FreeboxResponse;
import org.openhab.binding.freebox.internal.api.model.WifiConfig.FilterState;

/**
 * The {@link WifiConfigResponse} is the Java class used to map the
 * response of the Wifi global configuration API
 * https://dev.freebox.fr/sdk/os/wifi/# *
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WifiConfigResponse extends FreeboxResponse<WifiConfig> {
    @Override
    public void evaluate() throws FreeboxException {
        super.evaluate();
        if (getResult().getMacFilterState() == FilterState.UNKNOWN) {
            throw new FreeboxException("No Wifi status in response", this);
        }
    }
}
