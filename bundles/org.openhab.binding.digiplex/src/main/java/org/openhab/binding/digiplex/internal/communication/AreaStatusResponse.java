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
package org.openhab.binding.digiplex.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response for {@link AreaStatusRequest}
 *
 * @author Robert Michalak - Initial contribution
 */
@NonNullByDefault
public class AreaStatusResponse extends AbstractResponse {

    public final int areaNo;
    public final @Nullable AreaStatus status;
    public final boolean zoneInMemory;
    public final boolean trouble;
    public final boolean ready;
    public final boolean inProgramming;
    public final boolean alarm;
    public final boolean strobe;

    private AreaStatusResponse(int areaNo, AreaStatus status, boolean zoneInMemory, boolean trouble, boolean ready,
            boolean inProgramming, boolean alarm, boolean strobe) {
        this.areaNo = areaNo;
        this.status = status;
        this.zoneInMemory = zoneInMemory;
        this.trouble = trouble;
        this.ready = ready;
        this.inProgramming = inProgramming;
        this.alarm = alarm;
        this.strobe = strobe;
    }

    private AreaStatusResponse(int areaNo) {
        super(false);
        this.areaNo = areaNo;
        this.status = null;
        this.zoneInMemory = false;
        this.trouble = false;
        this.ready = false;
        this.inProgramming = false;
        this.alarm = false;
        this.strobe = false;
    }

    /**
     * Builds a response for a given areaNo. Indicates that request failed.
     */
    public static AreaStatusResponse failure(int areaNo) {
        return new AreaStatusResponse(areaNo);
    }

    /**
     * Builds a response for a given parameters. Indicates that request was successful.
     */
    public static AreaStatusResponse success(int areaNo, AreaStatus status, boolean zoneInMemory, boolean trouble,
            boolean ready, boolean inProgramming, boolean alarm, boolean strobe) {
        return new AreaStatusResponse(areaNo, status, zoneInMemory, trouble, ready, inProgramming, alarm, strobe);
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleAreaStatusResponse(this);
    }
}
