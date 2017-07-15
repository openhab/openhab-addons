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
 * The {@link SomfyTahomaExecutionsResponse} holds information about response
 * to getting running executions command (moving devices).
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaExecutionsResponse {

    private ArrayList<SomfyTahomaExecution> executions;

    public ArrayList<SomfyTahomaExecution> getExecutions() {
        return executions;
    }
}
