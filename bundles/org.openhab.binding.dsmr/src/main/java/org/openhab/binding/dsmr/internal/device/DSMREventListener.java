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
package org.openhab.binding.dsmr.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorErrorEvent;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;

/**
 * Interface for classes handling DSMR connector events.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - renamed classes/methods
 */
@NonNullByDefault
public interface DSMREventListener {
    /**
     * Callback for DSMRPortEvent events
     *
     * @param connectorErrorEvent {@link DSMRConnectorErrorEvent} that has occurred
     */
    public void handleErrorEvent(DSMRConnectorErrorEvent connectorErrorEvent);

    /**
     * Callback for received P1 telegrams
     *
     * @param telegram the received P1 telegram
     */
    public void handleTelegramReceived(P1Telegram telegram);
}
