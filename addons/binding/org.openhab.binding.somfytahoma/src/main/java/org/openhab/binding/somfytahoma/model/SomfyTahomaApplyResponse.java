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
 * The {@link SomfyTahomaApplyResponse} holds information about
 * response to sending a command to a device.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaApplyResponse {

    private String execId;

    public String getExecId() {
        return execId;
    }
}
