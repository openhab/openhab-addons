/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.haywardomnilogic.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.haywardomnilogic.internal.hayward.HaywardTypeToRequest;

/**
 * The {@link HaywardListener} is notified when a light status has changed or a light has been removed or added.
 *
 * @author Matt Myers - Initial Contribution
 *
 */
@NonNullByDefault
public interface HaywardListener {

    void handleHaywardTelemetry(HaywardTypeToRequest type, String systemID, String channelID, String data);

    void onDeviceDiscovered(HaywardTypeToRequest type, Integer systemID, String label, String bowID, String bowName,
            String property1, String property2, String property3, String property4);

}
