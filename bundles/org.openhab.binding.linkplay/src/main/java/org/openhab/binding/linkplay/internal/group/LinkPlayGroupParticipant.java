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
    /**
     * Called when this participant has been added to a group or when the group membership has been updated.
     * 
     * @param leader the group leader participant
     * @param slaves the list of slave devices in the group
     */
    void addedToOrUpdatedGroup(LinkPlayGroupParticipant leader, List<Slave> slaves);

    /**
     * Called when this participant has been removed from a group.
     * 
     * @param leader the group leader participant from which this participant was removed
     */
    void removedFromGroup(LinkPlayGroupParticipant leader);

    /**
     * Called when the list of participants in the group has been updated.
     * 
     * @param participants the collection of all participants in the group
     */
    void groupParticipantsUpdated(Collection<LinkPlayGroupParticipant> participants);

    /**
     * Updates the state of a channel through the group proxy mechanism.
     * 
     * @param channelId the identifier of the channel to update
     * @param state the new state to set for the channel
     */
    void groupProxyUpdateState(String channelId, State state);

    /**
     * Gets the human-readable label for this group participant.
     * 
     * @return the label for this participant
     */
    String getGroupParticipantLabel();

    /**
     * Gets the IP address of this participant's LinkPlay device.
     * 
     * @return the IP address as a string
     */
    String getIpAddress();

    /**
     * Gets the unique identifier for this participant's Thing.
     * 
     * @return the Thing UID
     */
    ThingUID getThingUID();

    /**
     * Gets the HTTP client used to communicate with this participant's LinkPlay device.
     * 
     * @return the LinkPlay HTTP client
     */
    LinkPlayHTTPClient getApiClient();

    /**
     * Plays a notification sound from the specified URL on this participant's device.
     * 
     * @param url the URL of the notification sound to play
     * @return a CompletableFuture that completes when the notification has been played
     */
    CompletableFuture<@Nullable Void> playNotification(String url);
}
