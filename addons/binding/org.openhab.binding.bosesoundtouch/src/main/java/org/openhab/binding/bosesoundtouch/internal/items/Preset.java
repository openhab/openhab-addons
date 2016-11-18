package org.openhab.binding.bosesoundtouch.internal.items;

public class Preset {
    private int pos;
    private ContentItem contentItem;

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public ContentItem getContentItem() {
        return contentItem;
    }

    public void setContentItem(ContentItem contentItem) {
        this.contentItem = contentItem;
    }

    public boolean posIsValid() {
        return (getPos() >= 1 && getPos() <= 6);
    }

}
