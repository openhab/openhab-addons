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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.FreeboxResponse;

/**
 * The {@link PhoneStatusResponse} is the Java class used to map the
 * response of the phone API
 *
 * @author Laurent Garnier - Initial contribution
 */
// This API is undocumented but working
// It is extracted from the freeboxos-java library
// https://github.com/MatMaul/freeboxos-java/blob/master/src/org/matmaul/freeboxos/phone/PhoneManager.java#L17
@NonNullByDefault
public class PhoneStatusResponse extends FreeboxResponse<List<PhoneStatus>> {
    @Override
    public void evaluate() throws FreeboxException {
        super.evaluate();
        if (getResult().size() == 0) {
            throw new FreeboxException("No phone status in response", this);
        }
    }
}
