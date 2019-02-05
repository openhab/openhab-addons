/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mysensors.internal.sensors;

import org.openhab.binding.mysensors.internal.Mergeable;
import org.openhab.binding.mysensors.internal.exception.MergeException;

/**
 * Configuration and parameters of a child from a MySensors node.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public class MySensorsChildConfig implements Mergeable {

    private boolean requestAck;
    private boolean revertState;
    private boolean smartSleep;
    private int expectUpdateTimeout;

    public MySensorsChildConfig() {
        expectUpdateTimeout = -1;
    }

    public boolean getSmartSleep() {
        return smartSleep;
    }

    public void setSmartSleep(boolean smartSleep) {
        this.smartSleep = smartSleep;
    }

    public int getExpectUpdateTimeout() {
        return expectUpdateTimeout;
    }

    public void setExpectUpdateTimeout(int expectUpdateTimeout) {
        this.expectUpdateTimeout = expectUpdateTimeout;
    }

    public boolean getRequestAck() {
        return requestAck;
    }

    public void setRequestAck(boolean requestAck) {
        this.requestAck = requestAck;
    }

    public boolean getRevertState() {
        return revertState;
    }

    public void setRevertState(boolean revertState) {
        this.revertState = revertState;
    }

    @Override
    public void merge(Object o) throws MergeException {
        if (o == null || !(o instanceof MySensorsChildConfig)) {
            throw new MergeException("Invalid object to merge");
        }

        MySensorsChildConfig childConfig = (MySensorsChildConfig) o;

        requestAck |= childConfig.requestAck;
        revertState |= childConfig.revertState;
        smartSleep |= childConfig.smartSleep;

        if (expectUpdateTimeout <= 0) {
            expectUpdateTimeout = childConfig.expectUpdateTimeout;
        }
    }

    @Override
    public String toString() {
        return "MySensorsChildConfig [requestAck=" + requestAck + ", revertState=" + revertState + ", smartSleep="
                + smartSleep + ", expectUpdateTimeout=" + expectUpdateTimeout + "]";
    }

}
