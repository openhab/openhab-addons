/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class ReverseLink {
    private final NodeId nodeId;
    private final Set<GroupId> groupIds;

    public ReverseLink(
            final NodeId nodeId,
            final Set<GroupId> groupIds
    ) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(groupIds);

        // Make a defensive copy.
        final Set<GroupId> groupIdsCopy = Collections.unmodifiableSet(new HashSet<>(groupIds));

        if (groupIdsCopy.isEmpty()) {
            throw new IllegalArgumentException("A reverse link must have at least one group id");
        }

        if (groupIdsCopy.contains(null)) {
            throw new IllegalArgumentException("Null group ids are not allowed.");
        }

        this.nodeId = nodeId;
        this.groupIds = groupIdsCopy;
    }

    public NodeId getNodeId() {
        return this.nodeId;
    }

    public Set<GroupId> getGroupIds() {
        return this.groupIds;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReverseLink that = (ReverseLink) o;
        return this.nodeId.equals(that.nodeId) &&
                this.groupIds.equals(that.groupIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nodeId, this.groupIds);
    }
}
