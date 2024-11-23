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
package org.openhab.binding.qolsysiq.internal.client.dto.action;

/**
 * An {@link ActionType#INFO} type of {@link InfoAction} message sent to the panel
 *
 * @author Dan Cunningham - Initial contribution
 */
public class InfoAction extends Action {
    public InfoActionType infoType;

    public InfoAction(InfoActionType infoType) {
        this(infoType, "");
    }

    public InfoAction(InfoActionType infoType, String token) {
        super(ActionType.INFO, token);
        this.infoType = infoType;
    }
}
