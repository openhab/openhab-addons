/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.local;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.tellstick.device.TellstickException;

/**
 * {@link TelldusLocalException} is used when there is exception communicating with Telldus local API.
 * This exception extends the Telldus Core exception.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class TelldusLocalException extends TellstickException {

    public TelldusLocalException(Exception source) {
        super(null, 0);
        this.initCause(source);
    }

    private static final long serialVersionUID = 3067179547449454711L;

    @Override
    public @Nullable String getMessage() {
        Throwable throwable = getCause();
        if (throwable != null) {
            String localMessage = throwable.getMessage();
            if (localMessage != null) {
                return localMessage;
            }
        }
        return "";
    }
}
