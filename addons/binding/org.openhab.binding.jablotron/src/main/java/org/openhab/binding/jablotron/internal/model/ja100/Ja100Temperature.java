/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model.ja100;

import com.google.gson.annotations.SerializedName;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The {@link Ja100StatusResponse} class defines the JA100 temperature sensor
 * object.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class Ja100Temperature {
    private String stateName;
    private String value;
    private long ts;

    public String getStateName() {
        return stateName;
    }

    public String getValue() {
        return value;
    }

    public long getTs() {
        return ts;
    }
}
