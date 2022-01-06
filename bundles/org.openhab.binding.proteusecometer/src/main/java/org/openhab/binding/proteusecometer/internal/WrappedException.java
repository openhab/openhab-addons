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
package org.openhab.binding.proteusecometer.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Allows you to transform an {@link Exception} to {@link RuntimeException} to circumvent checked exception
 * issues.
 *
 * @author Matthias Herrmann - Initial contribution
 */
@NonNullByDefault
public class WrappedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public WrappedException(final Exception wrapped) {
        super(wrapped);
    }
}
