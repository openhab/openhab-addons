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
package org.openhab.binding.sncf.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception for errors when using the SNCF API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SncfException extends Exception {
    private static final long serialVersionUID = -6215621577081394328L;

    public SncfException(String label) {
        super(label);
    }

    public SncfException(Throwable e) {
        super(e);
    }

    public SncfException(@Nullable String message, @Nullable Throwable e) {
        super(message, e);
    }
}
