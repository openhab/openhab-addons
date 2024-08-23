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
package org.openhab.binding.jablotron.internal.model.ja100f;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link JablotronGetThermoDevicesResponse} class defines the response object for the
 * getThermometers call
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronGetThermoDevicesResponse {

    JablotronGetThermoDevicesData data = new JablotronGetThermoDevicesData();

    public JablotronGetThermoDevicesData getData() {
        return data;
    }
}
