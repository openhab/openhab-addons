package org.openhab.binding.bosesoundtouch.internal.items;

import org.openhab.binding.bosesoundtouch.types.OperationModeType;

public class ContentItem {
    public OperationModeType operationMode;
    public String location;
    public String sourceAccount;
    public String itemName;
    public boolean isPresetable;

    private boolean se(String s1, String s2) {
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
            if (!se(other.location, this.location)) {
                return false;
            }
            if (!se(other.sourceAccount, this.sourceAccount)) {
                return false;
            }
            if (!se(other.itemName, this.itemName)) {
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
