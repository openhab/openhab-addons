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
package org.openhab.io.hueemulation.internal.dto.response;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Hue API response base type
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueResponseSuccessSimple {
    public final String success;

    public HueResponseSuccessSimple(String success) {
        this.success = success;
    }
}
