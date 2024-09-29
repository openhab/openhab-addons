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
package org.openhab.binding.boschindego.internal.dto.request;

/**
 * Request for setting a new device state
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class SetStateRequest {

    public static final String STATE_MOW = "mow";

    public static final String STATE_PAUSE = "pause";

    public static final String STATE_RETURN = "returnToDock";

    public String state;
}
