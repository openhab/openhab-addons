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

/**
 * Response for arm, quick arm and disarm requests
 *
 * @author Robert Michalak - Initial contribution
 */
@NonNullByDefault
public class AreaArmDisarmResponse extends AbstractResponse {

    public final int areaNo;
    public final ArmDisarmType type;

    private AreaArmDisarmResponse(int areaNo, ArmDisarmType type, boolean success) {
        super(success);
        this.areaNo = areaNo;
        this.type = type;
    }

    /**
     * Builds a response for a given areaNo and type. Indicates that request failed.
     */
    public static AreaArmDisarmResponse failure(int areaNo, ArmDisarmType type) {
        return new AreaArmDisarmResponse(areaNo, type, false);
    }

    /**
     * Builds a response for a given areaNo and type. Indicates that request was successful.
     */
    public static AreaArmDisarmResponse success(int areaNo, ArmDisarmType type) {
        return new AreaArmDisarmResponse(areaNo, type, true);
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleArmDisarmAreaResponse(this);
    }
}
