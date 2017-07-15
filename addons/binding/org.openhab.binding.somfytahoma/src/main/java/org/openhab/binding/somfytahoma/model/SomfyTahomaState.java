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
 * The {@link SomfyTahomaState} holds information about
 * device state.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaState {

    private Object value;

    public int getType() {
        return type;
    }

    private int type;

    public Object getValue() {
        return value;
    }
}
