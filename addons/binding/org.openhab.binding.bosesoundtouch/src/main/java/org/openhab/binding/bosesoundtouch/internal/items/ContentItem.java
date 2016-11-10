package org.openhab.binding.bosesoundtouch.internal.items;

import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.openhab.binding.bosesoundtouch.types.RadioStationType;

public class ContentItem {
    public OperationModeType operationMode;
    public RadioStationType radioStation;
    public String location;
    public String sourceAccount;
    public String itemName;

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
            if (other.radioStation != this.radioStation) {
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
}
