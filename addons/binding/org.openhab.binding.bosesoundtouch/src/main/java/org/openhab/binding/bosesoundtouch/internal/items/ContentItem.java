package org.openhab.binding.bosesoundtouch.internal.items;

public class ContentItem {
    public static enum Source {
        STANDBY,
        INTERNET_RADIO,
        BLUETOOTH,
        STORED_MUSIC,
        SPOTIFY,
        PANDORA,
        DEEZER,
        SIRIUSXM,
        UNKNOWN
    }; // TODO incomplete!

    public Source source;
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
            if (other.source != this.source) {
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
}
