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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class Link {
    private final NodeId nodeId;
    private final GroupId groupId;

    public Link(
            final NodeId nodeId,
            final GroupId groupId
    ) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(groupId);

        this.nodeId = nodeId;
        this.groupId = groupId;
    }

    public NodeId getNodeId() {
        return this.nodeId;
    }

    public GroupId getGroupId() {
        return this.groupId;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Link link = (Link) o;
        return this.nodeId.equals(link.nodeId) &&
                this.groupId.equals(link.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nodeId, this.groupId);
    }
}
