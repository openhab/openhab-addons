/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.ankersolix.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AnkerSolixConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
public class AnkerSolixConfiguration {

    public int pollInterval = 5000;
    public int maxTries = 3;
    public int writeProtectionDurationSeconds = 15;
    public boolean autoThirdPartyControl = true;
}
