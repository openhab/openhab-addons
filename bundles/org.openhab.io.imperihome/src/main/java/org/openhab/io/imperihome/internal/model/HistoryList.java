/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
        this(new LinkedList<>());
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
