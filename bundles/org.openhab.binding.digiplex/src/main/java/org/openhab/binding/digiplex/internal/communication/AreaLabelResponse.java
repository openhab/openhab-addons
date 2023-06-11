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
 * Response for {@link AreaLabelRequest}
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class AreaLabelResponse extends AbstractResponse {

    public final int areaNo;
    public final @Nullable String areaName;

    private AreaLabelResponse(int areaNo, String areaName) {
        super(true);
        this.areaNo = areaNo;
        this.areaName = areaName;
    }

    private AreaLabelResponse(int areaNo) {
        super(false);
        this.areaNo = areaNo;
        this.areaName = null;
    }

    /**
     * Builds a response for a given areaNo. Indicates that request failed.
     */

    public static AreaLabelResponse failure(int areaNo) {
        return new AreaLabelResponse(areaNo);
    }

    /**
     * Builds a response for a given areaNo and areaName. Indicates that request was successful.
     */
    public static AreaLabelResponse success(int areaNo, String areaName) {
        return new AreaLabelResponse(areaNo, areaName);
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleAreaLabelResponse(this);
    }
}
