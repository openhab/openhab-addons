/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * The {@link JablotronWidgetsResponse} class defines the get widgets
 * response.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronWidgetsResponse {
    private int status;

    @SerializedName("cnt-widgets")
    private int cntWidgets;

    @SerializedName("widget")
    private ArrayList<JablotronWidget> widgets;

    public int getStatus() {
        return status;
    }

    public int getCntWidgets() {
        return cntWidgets;
    }

    public ArrayList<JablotronWidget> getWidgets() {
        return widgets;
    }

    public boolean isOKStatus() {
        return status == 200;
    }
}
