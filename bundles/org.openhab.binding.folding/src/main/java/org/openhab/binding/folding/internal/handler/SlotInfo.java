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
package org.openhab.binding.folding.internal.handler;

import java.util.Map;

/**
 * Slot information entity
 *
 * This class specifies the format of the Json-compatible data received from
 * the Folding client process.
 *
 * @author Marius Bjoernstad - Initial contribution
 */
public class SlotInfo {

    public String id, status, description, reason;
    public Map<String, String> options;
    boolean idle;
}
