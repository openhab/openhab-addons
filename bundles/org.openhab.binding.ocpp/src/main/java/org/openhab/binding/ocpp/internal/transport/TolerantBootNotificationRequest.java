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
 * The embedded library validates an INBOUND BootNotification against the OCPP CiString20 constraint on
 * {@code chargePointModel} and {@code chargePointVendor} ({@code BootNotificationRequest.validate()} =
 * both non-null and no longer than 20 characters). When a charger exceeds that, or omits a field, the
 * library refuses the whole boot with a {@code CALLERROR} (OccurenceConstraintViolation) — and a
 * charger whose BootNotification is rejected never comes online, so nothing downstream works. A central
 * system must not brick a charger over a string-length technicality, so this override accepts the boot.
 *
 * <p>
 * The fields are still read as-is for the Thing properties: the library deserializes with Gson by field
 * reflection (see {@code JSONCommunicator}), so an over-long or absent value is written straight onto
 * the inherited field without passing through the length-checking setter — which would otherwise throw.
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
