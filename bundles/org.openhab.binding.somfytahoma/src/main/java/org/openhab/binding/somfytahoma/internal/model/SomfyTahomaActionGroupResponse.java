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
package org.openhab.binding.somfytahoma.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaActionGroupResponse} holds information about response
 * to getting action groups command.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaActionGroupResponse {
    private ArrayList<SomfyTahomaActionGroup> actionGroups = new ArrayList<>();

    public ArrayList<SomfyTahomaActionGroup> getActionGroups() {
        return actionGroups;
    }
}
