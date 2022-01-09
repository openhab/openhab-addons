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
package org.openhab.binding.dbquery.internal.error;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An unexpected error, aka bug
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class UnnexpectedCondition extends RuntimeException {
    private static final long serialVersionUID = -7785815761302340174L;

    public UnnexpectedCondition(String message) {
        super(message);
    }
}
