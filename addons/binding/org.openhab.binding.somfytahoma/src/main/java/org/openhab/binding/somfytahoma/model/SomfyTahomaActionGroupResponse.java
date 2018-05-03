/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.model;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaActionGroupResponse} holds information about response
 * to getting action groups command.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaActionGroupResponse {
    private ArrayList<SomfyTahomaActionGroup> actionGroups;

    public ArrayList<SomfyTahomaActionGroup> getActionGroups() {
        return actionGroups;
    }
}
