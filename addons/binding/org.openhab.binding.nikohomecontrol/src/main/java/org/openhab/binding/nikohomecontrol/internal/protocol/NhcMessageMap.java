/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Class {@link NhcMessageMap} used as output from gson for cmd or event feedback from Niko Home Control where the
 * data part is a simple json string. Extends {@link NhcMessageBase}.
 * <p>
 * Example: <code>{"cmd":"executeactions", "data":{"error":0}}</code>
 *
 * @author Mark Herwege - Initial Contribution
 */
class NhcMessageMap extends NhcMessageBase {

    private Map<String, String> data = new HashMap<>();

    Map<String, String> getData() {
        return this.data;
    }

    void setData(Map<String, String> data) {
        this.data = data;
    }
}
