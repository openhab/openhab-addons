/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.touchwand.internal.dto;

/**
 * The {@link TouchWandUnitDataWallController} implements WallController unit
 * property.
 *
 * @author Roie Geron - Initial contribution
 */
public class TouchWandUnitDataWallController extends TouchWandUnitData {

    private CurrStatus currStatus;

    @Override
    public Integer getCurrStatus() {
        if (currStatus != null) {
            return currStatus.getCsc().getKeyAttr();
        } else {
            return 0;
        }
    }

    public void setCurrStatus(CurrStatus currStatus) {
        this.currStatus = currStatus;
    }
}
