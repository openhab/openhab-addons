/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.powermax.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.message.PowermaxMessageConstants.PowermaxSysEvent;
import org.openhab.binding.powermax.internal.state.PowermaxState;

/**
 * A class for PANEL message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxPanelMessage extends PowermaxBaseMessage {

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxPanelMessage(byte[] message) {
        super(message);
    }

    @Override
    protected @Nullable PowermaxState handleMessageInternal(@Nullable PowermaxCommManager commManager) {
        if (commManager == null) {
            return null;
        }

        PowermaxState updatedState = commManager.createNewState();

        byte[] message = getRawData();
        int msgCnt = message[2] & 0x000000FF;

        debug("Event count", msgCnt);

        for (int i = 1; i <= msgCnt; i++) {
            byte eventZone = message[2 + 2 * i];
            byte logEvent = message[3 + 2 * i];
            int eventType = logEvent & 0x0000007F;
            PowermaxSysEvent sysEvent = PowermaxMessageConstants.getSystemEvent(eventType);
            String logEventStr = sysEvent.toString();
            String logUserStr = commManager.getPanelSettings().getZoneOrUserName(eventZone & 0x000000FF);

            debug("Event " + i + " zone code", eventZone, logUserStr);
            debug("Event " + i + " event code", eventType, logEventStr);

            if (sysEvent.isAlarm() || sysEvent.isSilentAlarm() || sysEvent.isAlert() || sysEvent.isPanic()
                    || sysEvent.isTrouble()) {
                updatedState.addActiveAlert(eventZone, eventType);
            }

            if (sysEvent.isAlarm() || (sysEvent.isPanic() && !commManager.getPanelSettings().isSilentPanic())) {
                updatedState.ringing.setValue(true);
                updatedState.ringingSince.setValue(System.currentTimeMillis());
            }

            if (sysEvent.isCancel() || sysEvent.isGeneralRestore() || sysEvent.isReset()) {
                updatedState.ringing.setValue(false);
            }

            if (sysEvent.isRestore()) {
                updatedState.clearActiveAlert(eventZone, sysEvent.getRestoreFor());
            }

            if (sysEvent.isGeneralRestore() || sysEvent.isReset()) {
                updatedState.clearAllActiveAlerts();
            }

            if (sysEvent.isReset()) {
                updatedState.clearAllActiveAlerts();
                updatedState.downloadSetupRequired.setValue(true);
            }
        }

        return updatedState;
    }
}
