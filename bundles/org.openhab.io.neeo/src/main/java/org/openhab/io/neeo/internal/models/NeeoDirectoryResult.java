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
package org.openhab.io.neeo.internal.models;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the result of a directory list request. This class is simply used for serialization of the result back to
 * the brain.
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public class NeeoDirectoryResult {
    /** The title of the directory */
    private final String title;

    /** The amount of items to show (the limit) */
    private final int limit;

    /** The offset index position of the first item in items */
    private final int offset;

    /** The total number of items overally */
    private final int totalMatchingItems;

    /** The browse identifier (not used for lists) */
    @Nullable
    private final String browseIdentifier;

    /** The subset of items in this result (index 0 = offset position in total) */
    private final NeeoDirectoryResultItem[] items;

    /** The meta data describing the current, previous and next positions */
    @SerializedName("_meta")
    private final NeeoDirectoryResultMeta meta;

    /**
     * Constructs the result given the request and overall directory
     *
     * @param req a non-null {@link NeeoDirectoryRequest}
     * @param directory a non-null {@link NeeoDeviceChannelDirectory}
     */
    public NeeoDirectoryResult(NeeoDirectoryRequest req, NeeoDeviceChannelDirectory directory) {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(directory, "directory cannot be null");

        final NeeoDeviceChannelDirectoryListItem[] listItems = directory.getListItems();

        this.title = directory.getLabel();
        this.limit = Math.min(10, listItems.length);
        this.offset = req.getOffset();
        this.totalMatchingItems = 1;
        this.browseIdentifier = req.getBrowseIdentifier();

        this.items = Arrays.stream(listItems).skip(this.offset).limit(this.limit).map(item -> {
            return new NeeoDirectoryResultItem(item.getTitle(), item.getThumbNailUri(), null, item.getItemValue(),
                    true);
        }).toArray(NeeoDirectoryResultItem[]::new);

        final NeeoDirectoryRequest current = new NeeoDirectoryRequest(this.offset, this.limit, this.browseIdentifier);

        final NeeoDirectoryRequest previous = this.offset > 0
                ? new NeeoDirectoryRequest(Math.max(0, this.offset - this.limit), Math.min(this.offset, this.limit),
                        this.browseIdentifier)
                : null;

        final int nextOffset = this.offset + this.items.length;
        final NeeoDirectoryRequest next = listItems.length > nextOffset
                ? new NeeoDirectoryRequest(nextOffset, this.limit, this.browseIdentifier)
                : null;

        this.meta = new NeeoDirectoryResultMeta(listItems.length, listItems.length, current, previous, next);
    }

    /**
     * Returns the title
     *
     * @return a non-null title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the result limit
     *
     * @return the result limit (>= 0)
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Returns the offset of the items
     *
     * @return the offset (> = 0)
     */
    public int getOffset() {
        return offset;
    }

    /**
     * The total matching items
     *
     * @return the total items (>=0)
     */
    public int getTotalMatchingItems() {
        return totalMatchingItems;
    }

    /**
     * The browse identifier
     *
     * @return a non-null possibly empty browse identifier
     */
    @Nullable
    public String getBrowseIdentifier() {
        return browseIdentifier;
    }

    /**
     * The items in this result
     *
     * @return a non-null, possibly empty list of items
     */
    public NeeoDirectoryResultItem[] getItems() {
        return items;
    }

    /**
     * The meta data for the request
     *
     * @return a non-null meta data
     */
    public NeeoDirectoryResultMeta getMeta() {
        return meta;
    }

    @Override
    public String toString() {
        return "NeeoDiscoveryListResult [title=" + title + ", limit=" + limit + ", offset=" + offset
                + ", totalMatchingItems=" + totalMatchingItems + ", browseIdentifier=" + browseIdentifier + ", items="
                + Arrays.toString(items) + ", meta=" + meta + "]";
    }
}
