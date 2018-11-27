/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model;

import java.util.List;

/**
 * Represents a Gardena recurrence.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Recurrence {

    private String type;
    private List<String> weekdays;

    /**
     * Returns the type of the recurrence.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns a list of weekdays.
     */
    public List<String> getWeekdays() {
        return weekdays;
    }
}
