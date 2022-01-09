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
package org.openhab.binding.digiplex.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response for {@link ZoneLabelRequest}
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class ZoneLabelResponse extends AbstractResponse {

    public final int zoneNo;
    public final @Nullable String zoneName;

    private ZoneLabelResponse(int zoneNo, String zoneName) {
        super(true);
        this.zoneNo = zoneNo;
        this.zoneName = zoneName;
    }

    private ZoneLabelResponse(int zoneNo) {
        super(false);
        this.zoneNo = zoneNo;
        this.zoneName = null;
    }

    /**
     * Builds a response for a given zoneNo. Indicates that request failed.
     */
    public static ZoneLabelResponse failure(int zoneNo) {
        return new ZoneLabelResponse(zoneNo);
    }

    /**
     * Builds a response for a given zoneNo. Indicates that request was successful.
     */
    public static ZoneLabelResponse success(int zoneNo, String zoneName) {
        return new ZoneLabelResponse(zoneNo, zoneName);
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleZoneLabelResponse(this);
    }
}
