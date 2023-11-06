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
package org.openhab.binding.vdr.internal.svdrp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SVDRPConnectionException} is thrown when SVDRP Connection cannot be established
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPConnectionException extends SVDRPException {

    private static final long serialVersionUID = 2825596676109860370L;

    public SVDRPConnectionException(@Nullable String message) {
        super(message);
    }

    public SVDRPConnectionException(@Nullable String message, Throwable cause) {
        super(message, cause);
    }
}
