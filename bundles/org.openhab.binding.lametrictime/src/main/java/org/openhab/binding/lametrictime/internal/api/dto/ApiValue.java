/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.dto;

/**
 * Interface for api value.
 *
 * @author Gregory Moyer - Initial contribution
 */
public interface ApiValue {
    String toRaw();

    static String raw(ApiValue value) {
        if (value == null) {
            return null;
        }

        return value.toRaw();
    }
}
