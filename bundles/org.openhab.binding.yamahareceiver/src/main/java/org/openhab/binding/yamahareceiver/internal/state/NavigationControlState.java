/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.state;

import org.openhab.binding.yamahareceiver.internal.protocol.xml.InputWithNavigationControlXML;

/**
 * The current state of the navigation
 *
 * @author David Graeff - Initial contribution
 */
public class NavigationControlState implements Invalidateable {
    public String menuName = null;
    public int menuLayer = -1;
    public int currentLine = 0;
    public int maxLine = -1;
    public String items[] = new String[InputWithNavigationControlXML.MAX_PER_PAGE];

    public String getCurrentItemName() {
        if (currentLine < 1 || currentLine > items.length) {
            return "";
        }
        return items[currentLine - 1];
    }

    public String getAllItemLabels() {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (item != null && !item.isEmpty()) {
                sb.append(item);
                sb.append(',');
            }
        }
        return sb.toString();
    }

    public void clearItems() {
        for (int i = 0; i < items.length; ++i) {
            items[i] = null;
        }
    }

    @Override
    public void invalidate() {
        this.menuName = "N/A";
        this.maxLine = 0;
        this.currentLine = 0;
        this.menuLayer = 0;
    }
}
