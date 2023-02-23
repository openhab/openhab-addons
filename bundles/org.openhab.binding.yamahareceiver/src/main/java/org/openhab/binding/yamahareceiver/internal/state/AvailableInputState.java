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
package org.openhab.binding.yamahareceiver.internal.state;

import java.util.Map;
import java.util.TreeMap;

/**
 * List of AVR input channel names with <Input ID, Input Name>
 *
 * @author David Graeff - Initial contribution
 */
public class AvailableInputState {

    // List of inputs with <Input ID, Input Name>
    public Map<String, String> availableInputs = new TreeMap<>();
}
