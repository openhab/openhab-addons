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
package org.openhab.binding.loqed.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LoqedConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedConfiguration {
    public String apiToken = "";
    public int refreshInterval = 60;
    public String lockId = "";
    public String keySecret = "";
    public int localKeyId = -1;
}
