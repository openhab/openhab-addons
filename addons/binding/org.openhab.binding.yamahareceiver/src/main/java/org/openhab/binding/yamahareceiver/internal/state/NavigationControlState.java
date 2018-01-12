/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.state;

import org.apache.commons.lang.StringUtils;
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
            if (StringUtils.isNotEmpty(item)) {
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

    public void invalidate() {
        this.menuName = "N/A";
        this.maxLine = 0;
        this.currentLine = 0;
        this.menuLayer = 0;
    }
}
