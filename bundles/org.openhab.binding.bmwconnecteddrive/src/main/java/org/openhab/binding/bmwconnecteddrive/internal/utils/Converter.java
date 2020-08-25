/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import java.time.format.DateTimeFormatter;

/**
 * The {@link Converter} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Converter {
    public final static DateTimeFormatter inputPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public final static DateTimeFormatter outputPattern = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static double round(double value) {
        double scale = Math.pow(10, 1);
        return Math.round(value * scale) / scale;
    }
}
