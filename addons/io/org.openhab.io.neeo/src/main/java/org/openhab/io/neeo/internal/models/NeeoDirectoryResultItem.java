/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.models;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * Represents a directory list item. This class is simply used for serialization of the result back to the brain.
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public class NeeoDirectoryResultItem {
    /** The title (label) of the item */
    private final String title;

    /** The URI to the thumbnail representing the item */
    @Nullable
    private final String thumbnailUri;

    /** The browse identifier (reflected back in a request if this item is a container) */
    @Nullable
    private final String browseIdentifier;

    /** The action identifier (reflected back in a request if this is a leaf) */
    @Nullable
    private final String actionIdentifier;

    /** Whether the item is queueable (no explanation posted by NEEO) */
    private final boolean isQueueable;

    /**
     * Creates the directory from the given items
     *
     * @param title a non-null, non-empty title
     * @param thumbnailUri a possibly null, possibly empty thumbnail URI
     * @param browseIdentifier a possibly null, possibly empty browse identifier
     * @param actionIdentifier a possibly null, possibly empty action identifier
     * @param isQueueable true/false if queueable
     * @throws IllegalArgumentException if both browseIdentifier and actionIdentifier are null or empty
     */
    public NeeoDirectoryResultItem(String title, @Nullable String thumbnailUri, @Nullable String browseIdentifier,
            @Nullable String actionIdentifier, boolean isQueueable) {
        NeeoUtil.requireNotEmpty(title, "title cannot be empty");

        if (StringUtils.isEmpty(browseIdentifier) && StringUtils.isEmpty(actionIdentifier)) {
            throw new IllegalArgumentException("Either browserIdentifier or actionIdentifier must be specified");
        }

        this.title = title;
        this.thumbnailUri = thumbnailUri;
        this.browseIdentifier = browseIdentifier;
        this.actionIdentifier = actionIdentifier;
        this.isQueueable = isQueueable;
    }

    /**
     * The title (label) of the item
     *
     * @return the non-null, non-empty title
     */
    public String getTitle() {
        return title;
    }

    /**
     * The thumbnail URI representing the item
     *
     * @return a possibly null, possibly empty thumbnail URI
     */
    @Nullable
    public String getThumbnailUri() {
        return thumbnailUri;
    }

    /**
     * The browse identifier
     *
     * @return a possibly null, possibly empty browse identifier
     */
    @Nullable
    public String getBrowseIdentifier() {
        return browseIdentifier;
    }

    /**
     * The action identifier
     *
     * @return a possibly null, possibly empty action identifier
     */
    @Nullable
    public String getActionIdentifier() {
        return actionIdentifier;
    }

    /**
     * Whether the item is queueable or not
     *
     * @return true if queueable, false otherwise
     */
    public boolean isQueueable() {
        return isQueueable;
    }

    @Override
    public String toString() {
        return "NeeoDiscoveryListResultItem [title=" + title + ", thumbnailUri=" + thumbnailUri + ", browseIdentifier="
                + browseIdentifier + ", actionIdentifier=" + actionIdentifier + ", isQueueable=" + isQueueable + "]";
    }

}