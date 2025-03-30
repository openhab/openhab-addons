/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast.internal.solcast.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;

/**
 * The {@link SolcastBridgeMock} is mocking Solcast Bridge Handler
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastBridgeMock extends SolcastBridgeHandler {
    public SolcastBridgeMock(Bridge b, TimeZoneProvider tzp) {
        super(b, tzp);
    }

    public void updateConfig(Configuration config) {
        super.updateConfiguration(config);
    }
}
