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
package org.openhab.io.hueemulation.internal.automation.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A configuration holder class for the item command handler
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ItemCommandActionConfig {
    public String itemName = "";
    public String command = "";
}
