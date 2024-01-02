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
package org.openhab.binding.opengarage.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The OpenGarageConfiguration class contains fields mapping thing configuration parameters.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class OpenGarageConfiguration {
    public String hostname = "";
    public int port = 80;
    public String password = "opendoor";
    public int refresh = 10;

    public String doorOpeningState = "OPENING";
    public String doorOpenState = "OPEN";
    public String doorClosedState = "CLOSED";
    public String doorClosingState = "CLOSING";
    public int doorTransitionTimeSeconds = 17;
}
