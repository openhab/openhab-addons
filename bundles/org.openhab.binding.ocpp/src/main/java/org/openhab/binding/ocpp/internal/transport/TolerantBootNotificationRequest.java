/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ocpp.internal.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;

import eu.chargetime.ocpp.model.core.BootNotificationRequest;

/**
 * A {@link BootNotificationRequest} that accepts any payload.
 *
 * <p>
 * The embedded library enforces the OCPP CiString20 constraint on inbound {@code chargePointModel} and
 * {@code chargePointVendor} (non-null, at most 20 characters); a charger that exceeds it or omits a
 * field is refused with a {@code CALLERROR} and never comes online. This override deliberately accepts
 * the boot rather than brick a charger over a string-length technicality.
 *
 * <p>
 * The fields are still read as-is for the Thing properties: the library deserializes with Gson by field
 * reflection, so an over-long or absent value lands on the inherited field without passing through the
 * length-checking setter that would otherwise throw.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class TolerantBootNotificationRequest extends BootNotificationRequest {

    @Override
    public boolean validate() {
        return true;
    }
}
