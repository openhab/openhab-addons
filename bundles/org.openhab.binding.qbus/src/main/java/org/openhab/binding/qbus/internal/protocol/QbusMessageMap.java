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
package org.openhab.binding.qbus.internal.protocol;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class {@link QbusMessageMap} used as output from gson for cmd or event feedback from Qbus where the
 * data part is a simple json string. Extends {@link QbusMessageBase}.
 * <p>
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
class QbusMessageMap extends QbusMessageBase {

    private Map<String, String> outputs = new HashMap<>();

    Map<String, String> getData() {
        return this.outputs;
    }

    void setOutputs(Map<String, String> outputs) {
        this.outputs = outputs;
    }
}
