/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.items;

/**
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
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

    public RemoteKey getKey() {
        if (getPos() == 1) {
            return RemoteKey.PRESET_1;
        } else if (getPos() == 2) {
            return RemoteKey.PRESET_2;
        } else if (getPos() == 3) {
            return RemoteKey.PRESET_3;
        } else if (getPos() == 4) {
            return RemoteKey.PRESET_4;
        } else if (getPos() == 5) {
            return RemoteKey.PRESET_5;
        } else if (getPos() == 6) {
            return RemoteKey.PRESET_6;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("PRESET_");
        buffer.append(getPos());
        return buffer.toString();
    }

}
