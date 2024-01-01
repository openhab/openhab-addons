/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.saicismart.internal.exceptions;

import net.heberling.ismart.asn1.v3_0.MP_DispatcherBody;

/**
 * @author Doug Culnane
 */
public class ChargingStatusAPIException extends Exception {

    public ChargingStatusAPIException(MP_DispatcherBody body) {
        super("[" + body.getResult() + "] " + new String(body.getErrorMessage()));
    }
}
