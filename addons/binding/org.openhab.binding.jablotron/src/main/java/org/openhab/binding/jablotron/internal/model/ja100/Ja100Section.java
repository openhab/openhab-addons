/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.model.ja100;

/**
 * The {@link Ja100StatusResponse} class defines the JA100 section status
 * object.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class Ja100Section {
    private int stav;
    private String stateName;
    private String nazev;
    private String time;
    private long active;

    public long getStav() {
        return stav;
    }

    public String getStateName() {
        return stateName;
    }

    public String getNazev() {
        return nazev;
    }

    public String getTime() {
        return time;
    }

    public long getActive() {
        return active;
    }
}
