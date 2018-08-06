/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * Represents a list item for the directory.
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public class NeeoDeviceChannelDirectoryListItem {

    /** The item value that will be used when the item is selected */
    private final String itemValue;

    /** The title (label) of the item */
    private final String title;

    /** The optional thumbnail URI to represent the item */
    @Nullable
    private final String thumbNailUri;

    /**
     * Constructs the list item from the attributes
     *
     * @param itemValue a possibly null, possibly empty item value
     * @param title a non-null, non-empty title
     * @param thumbNailUri a possibly null, possibly empty thumbnail URI
     */
    public NeeoDeviceChannelDirectoryListItem(String itemValue, String title, @Nullable String thumbNailUri) {
        NeeoUtil.requireNotEmpty(title, "title cannot be empty");
        this.itemValue = itemValue;
        this.title = title;
        this.thumbNailUri = thumbNailUri;
    }

    /**
     * The item value
     *
     * @return a possibly null, possibly empty item value
     */
    public String getItemValue() {
        return itemValue;
    }

    /**
     * The title for the item
     *
     * @return a non-null, non-empty title
     */
    public String getTitle() {
        return title;
    }

    /**
     * The optional thumbnail URI
     *
     * @return a possibly null, possibly empty thumbnail URI
     */
    @Nullable
    public String getThumbNailUri() {
        return thumbNailUri;
    }

    @Override
    public String toString() {
        return "NeeoDeviceChannelDirectoryListItem [itemValue=" + itemValue + ", title=" + title + ", thumbNailUri="
                + thumbNailUri + "]";
    }
}
