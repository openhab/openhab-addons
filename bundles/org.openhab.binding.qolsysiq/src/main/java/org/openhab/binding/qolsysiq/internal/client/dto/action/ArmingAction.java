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
package org.openhab.binding.qolsysiq.internal.client.dto.action;

/**
 * An {@link ActionType.ARMING} type of {@link ArmingAction} message sent to the panel
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ArmingAction extends Action {
    public ArmingActionType armingType;
    public Integer partitionId;
    public String usercode;

    public ArmingAction(ArmingActionType armingType, Integer partitionId) {
        this(armingType, "", partitionId, null);
    }

    public ArmingAction(ArmingActionType armingType, Integer partitionId, String usercode) {
        this(armingType, "", partitionId, usercode);
    }

    public ArmingAction(ArmingActionType armingType, String token, Integer partitionId) {
        this(armingType, token, partitionId, null);
    }

    public ArmingAction(ArmingActionType armingType, String token, Integer partitionId, String usercode) {
        super(ActionType.ARMING, token);
        this.armingType = armingType;
        this.partitionId = partitionId;
        this.usercode = usercode;
    }
}
