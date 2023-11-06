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
package org.openhab.binding.lcn.internal.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Default checked exception.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnException extends Exception {
    private static final long serialVersionUID = -4341882774124288028L;

    public LcnException() {
        super();
    }

    public LcnException(String message) {
        super(message);
    }

    public LcnException(Exception e) {
        super(e);
    }
}
