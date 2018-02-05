/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication;

/**
 * Response for arm, quick arm and disarm requests
 *
 * @author Robert Michalak - Initial contribution
 */
public class AreaArmDisarmResponse extends AbstractResponse {

    private int areaNo;
    private ArmDisarmType type;

    private AreaArmDisarmResponse(int areaNo, ArmDisarmType type, boolean success) {
        super(success);
        this.areaNo = areaNo;
        this.type = type;
    }

    public static AreaArmDisarmResponse failure(int areaNo, ArmDisarmType type) {
        return new AreaArmDisarmResponse(areaNo, type, false);
    }

    public static AreaArmDisarmResponse success(int areaNo, ArmDisarmType type) {
        return new AreaArmDisarmResponse(areaNo, type, true);
    }

    public int getAreaNo() {
        return areaNo;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleArmDisarmAreaResponse(this);

    }

}
