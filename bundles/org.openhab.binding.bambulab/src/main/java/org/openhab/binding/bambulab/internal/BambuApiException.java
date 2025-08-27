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
package org.openhab.binding.bambulab.internal;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import pl.grzeslowski.jbambuapi.mqtt.PrinterClientConfig;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class BambuApiException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public static String findSerial(@Nullable PrinterClientConfig localConfig) {
        return localConfig != null ? localConfig.serial() : "?";
    }

    public BambuApiException(String serial, String message) {
        super("[%s] %s".formatted(serial, message));
    }

    public BambuApiException(String serial, String message, Exception e) {
        super("[%s] %s".formatted(serial, message), e);
    }
}
