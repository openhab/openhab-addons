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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * Receives the decoded RSCP response of each poll cycle, on the bridge's scheduler thread; the response
 * is only valid for the duration of the call.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface E3DCDataListener {

    void onData(RscpValue response);
}
