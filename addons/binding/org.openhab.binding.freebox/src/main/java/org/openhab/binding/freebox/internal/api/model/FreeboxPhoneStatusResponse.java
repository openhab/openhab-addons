/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

import java.util.List;

import org.openhab.binding.freebox.internal.api.FreeboxException;

/**
 * The {@link FreeboxPhoneStatusResponse} is the Java class used to map the
 * response of the phone API
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxPhoneStatusResponse extends FreeboxResponse<List<FreeboxPhoneStatus>> {
    @Override
    public void evaluate() throws FreeboxException {
        super.evaluate();
        if (getResult() == null || getResult().size() == 0) {
            throw new FreeboxException("No phone status in response", this);
        }
    }
}
