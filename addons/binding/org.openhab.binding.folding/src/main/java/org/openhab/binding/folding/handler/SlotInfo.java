/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folding.handler;

import java.util.Map;

/**
 * Slot information entity
 *
 * This class specifies the format of the Json-compatible data received from
 * the Folding client process.
 *
 * @author Marius Bjoernstad
 */
public class SlotInfo {

    public String id, status, description, reason;
    public Map<String, String> options;
    boolean idle;

}
