/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.items;

import org.openhab.binding.bosesoundtouch.types.OperationModeType;

/**
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public class ContentItem {
    private OperationModeType operationMode;
    private String location;
    private String sourceAccount;
    private String itemName;
    private boolean isPresetable;

    private boolean isEqual(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContentItem) {
            ContentItem other = (ContentItem) obj;
            if (other.operationMode != this.operationMode) {
                return false;
            }
            if (other.isPresetable != this.isPresetable) {
                return false;
            }
            if (!isEqual(other.location, this.location)) {
                return false;
            }
            if (!isEqual(other.sourceAccount, this.sourceAccount)) {
                return false;
            }
            if (!isEqual(other.itemName, this.itemName)) {
                return false;
            }
            return true;
        }
        return super.equals(obj);
    }

    public OperationModeType getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(OperationModeType operationMode) {
        this.operationMode = operationMode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
