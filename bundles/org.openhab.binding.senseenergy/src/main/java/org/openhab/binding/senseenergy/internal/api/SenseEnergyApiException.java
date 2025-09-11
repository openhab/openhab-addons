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
package org.openhab.binding.senseenergy.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link SenseEnergyApiException} exception class for any api exception
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class SenseEnergyApiException extends Exception {
    private static final long serialVersionUID = -7059398508028583720L;
    public final SEVERITY severity;
    @Nullable
    public final Exception e;

    public static enum SEVERITY {
        CONFIG,
        TRANSIENT,
        DATA,
        FATAL
    }

    public SenseEnergyApiException(String message, SEVERITY severity) {
        super(message);
        this.severity = severity;
        this.e = null;
    }

    public SenseEnergyApiException(String message, SEVERITY severity, Exception e) {
        super(message);
        this.severity = severity;
        this.e = e;
    }

    @Override
    public String toString() {
        Exception localE = e;
        return String.format("SenseEnergyApiException{message='%s', severity=%s}",
                (localE == null) ? getMessage() : localE.getMessage(), severity.toString());
    }
}
