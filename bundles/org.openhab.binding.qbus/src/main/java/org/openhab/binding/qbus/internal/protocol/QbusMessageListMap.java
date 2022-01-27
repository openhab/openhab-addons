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
package org.openhab.binding.qbus.internal.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class {@link QbusMessageListMap} used as output from gson for cmd or event feedback from Qbus where the
 * data part is enclosed by [] and contains a list of json strings. Extends {@link QbusMessageBase}.
 * <p>
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
class QbusMessageListMap extends QbusMessageBase {

    private List<Map<String, String>> outputs = new ArrayList<>();

    List<Map<String, String>> getOutputs() {
        return this.outputs;
    }

    void setOutputs(List<Map<String, String>> outputs) {
        this.outputs = outputs;
    }
}
