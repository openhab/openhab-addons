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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSSceneRelayValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSceneRelayValue extends WSResourceValue {

    public final int delayTime;
    public final boolean relayValue;

    public WSSceneRelayValue(int resourceID, int delayTime, boolean relayValue) {
        super(resourceID);
        this.delayTime = delayTime;
        this.relayValue = relayValue;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, value=%b, delayTime=%d]", super.resourceID, relayValue, delayTime);
    }
}
