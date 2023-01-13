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
package org.openhab.binding.freeboxos.internal.api.wifi.ap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.WifiApState;

/**
 * The {@link WifiApStatus} is the Java class used to hold information regarding the current status of the access point
 *
 * https://dev.freebox.fr/sdk/os/switch/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiApStatus {
    private WifiApState state = WifiApState.UNKNOWN;
    private int channelWidth;
    private int primaryChannel;
    private int secondaryChannel;
    private int dfsCacRemainingTime;
    private boolean dfsDisabled;

    public WifiApState getState() {
        return state;
    }

    public int getChannelWidth() {
        return channelWidth;
    }

    public int getPrimaryChannel() {
        return primaryChannel;
    }

    public int getSecondaryChannel() {
        return secondaryChannel;
    }

    public int getDfsCacRemainingTime() {
        return dfsCacRemainingTime;
    }

    public boolean isDfsDisabled() {
        return dfsDisabled;
    }

}
