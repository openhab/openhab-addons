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
package org.openhab.persistence.influxdb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Throw to indicate an unnexpected condition that should not have happened (a bug)
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class UnnexpectedConditionException extends RuntimeException {
    private static final long serialVersionUID = 1128380327167959556L;

    public UnnexpectedConditionException(String message) {
        super(message);
    }

    public UnnexpectedConditionException(String message, Throwable cause) {
        super(message, cause);
    }
}
