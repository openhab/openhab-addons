/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model;

import java.util.LinkedList;
import java.util.List;

/**
 * History list data object.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class HistoryList {

    private List<HistoryItem> values;

    public HistoryList() {
        this(new LinkedList<HistoryItem>());
    }

    public HistoryList(List<HistoryItem> resultItems) {
        this.values = resultItems;
    }

    public List<HistoryItem> getValues() {
        return values;
    }

    public void setValues(List<HistoryItem> values) {
        this.values = values;
    }

}
