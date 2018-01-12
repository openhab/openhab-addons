/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
