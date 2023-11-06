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
package org.openhab.binding.ecobee.internal.function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.enums.AckType;

/**
 * The acknowledge function allows an alert to be acknowledged.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public final class AcknowledgeFunction extends AbstractFunction {

    public AcknowledgeFunction(@Nullable String thermostatIdentifier, @Nullable String ackRef,
            @Nullable AckType ackType, @Nullable Boolean remindMeLater) {
        super("acknowledge");

        if (thermostatIdentifier == null || ackRef == null || ackType == null) {
            throw new IllegalArgumentException("thermostatIdentifier, ackRef and ackType are required.");
        }
        params.put("thermostatIdentifier", thermostatIdentifier);
        params.put("ackRef", ackRef);
        params.put("ackType", ackType);
        if (remindMeLater != null) {
            params.put("remindMeLater", remindMeLater);
        }
    }
}
