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
 * A {@link BootNotificationRequest} that accepts an over-length CiString20 model/vendor field.
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
