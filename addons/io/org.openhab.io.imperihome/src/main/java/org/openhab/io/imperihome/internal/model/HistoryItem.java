/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model;

import java.util.Date;

/**
 * History item data object.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class HistoryItem {

    private long date;
    private Number value;

    public HistoryItem(Date date, Number value) {
        this(date.getTime(), value);
    }

    public HistoryItem(long date, Number value) {
        this.date = date;
        this.value = value;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value = value;
    }

}
