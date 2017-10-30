/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.io.azureiothub;

import com.google.gson.Gson;

/**
 * A single datapoint that is pushed into azure Iot Hub
 *
 * @author Niko Tanghe
 */

public class AzureDatapoint {
    public String deviceId;
    public String value;

    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
