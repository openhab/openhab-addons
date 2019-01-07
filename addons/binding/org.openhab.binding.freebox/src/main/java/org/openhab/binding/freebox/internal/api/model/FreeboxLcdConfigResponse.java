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
 * The {@link FreeboxLcdConfigResponse} is the Java class used to map the
 * response of the LCD configuration API
 * https://dev.freebox.fr/sdk/os/lcd/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxLcdConfigResponse extends FreeboxResponse<FreeboxLcdConfig> {
    @Override
    public void evaluate() throws FreeboxException {
        super.evaluate();
        if (getResult() == null) {
            throw new FreeboxException("Missing result data in LCD configuration API response", this);
        }
    }
}
