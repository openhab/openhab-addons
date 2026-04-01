/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.group;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linkplay.internal.client.http.LinkPlayHTTPClient;
import org.openhab.binding.linkplay.internal.client.http.dto.Slave;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * A participant in a LinkPlay group.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface LinkPlayGroupParticipant {
    void addedToOrUpdatedGroup(LinkPlayGroupParticipant leader, List<Slave> slaves);

    void removedFromGroup(LinkPlayGroupParticipant leader);

    void groupParticipantsUpdated(Collection<LinkPlayGroupParticipant> participants);

    void groupProxyUpdateState(String channelId, State state);

    String getGroupParticipantLabel();

    String getIpAddress();

    ThingUID getThingUID();

    LinkPlayHTTPClient getApiClient();

    CompletableFuture<@Nullable Void> playNotification(String url);
}
