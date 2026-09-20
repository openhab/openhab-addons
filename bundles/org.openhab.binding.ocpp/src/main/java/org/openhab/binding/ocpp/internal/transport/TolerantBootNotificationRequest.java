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
 * A {@link BootNotificationRequest} that is accepted whatever its vendor and model say.
 *
 * The library refuses a BootNotification whose vendor or model is absent or longer than the CiString20 the
 * OCPP 1.6 schema allows, and answers a CALLERROR instead of a BootNotificationConfirmation, which leaves the
 * charger unable to come online. Both happen in the field. The binding identifies a charger by the charge
 * point id in its URL, not by these fields, and only reports them as Thing properties, so neither is worth
 * refusing a charger over.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("deprecation")
public class TolerantBootNotificationRequest extends BootNotificationRequest {

    @Override
    public boolean validate() {
        return true;
    }
}
