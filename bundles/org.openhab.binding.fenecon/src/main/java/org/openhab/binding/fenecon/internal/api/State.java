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
package org.openhab.binding.fenecon.internal.api;

/**
 * The {@link State} is a small helper class to convert the state value.
 *
 * @author Philipp Schneider - Initial contribution
 */
public record State(String state) {

    public static State get(FeneconResponse response) {
        // {"address":"_sum/State","type":"INTEGER","accessMode":"RO","text":"0:Ok, 1:Info, 2:Warning,
        // 3:Fault","unit":"","value":0}
        int begin = response.text().indexOf(response.value() + ":");
        int end = response.text().indexOf(",", begin);
        return new State(response.text().substring(begin + 2, end));
    }
}
