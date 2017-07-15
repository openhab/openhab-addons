/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.model;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaActionGroup} holds information about a action
 * group and current actions.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaActionGroup {
    private String oid;
    private String label;
    private ArrayList<SomfyTahomaAction> actions;

    public String getOid() {
        return oid;
    }

    public String getLabel() {
        return label;
    }

    public ArrayList<SomfyTahomaAction> getActions() {
        return actions;
    }
}
