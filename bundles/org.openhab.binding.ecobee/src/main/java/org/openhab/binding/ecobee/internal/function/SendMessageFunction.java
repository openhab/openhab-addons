/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * The send message function allows an alert message to be sent to the thermostat. The
 * message properties are same as those of the Alert object.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public final class SendMessageFunction extends AbstractFunction {

    public SendMessageFunction(final @Nullable String text) {
        super("sendMessage");
        if (text == null) {
            throw new IllegalArgumentException("text is required.");
        }
        params.put("text", text);
    }
}
