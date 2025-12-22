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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linkplay.internal.client.http.dto.Slave;
import org.openhab.binding.linkplay.internal.client.http.dto.SlaveListResponse;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing LinkPlay groups.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(service = LinkPlayGroupService.class, scope = ServiceScope.SINGLETON)
public class LinkPlayGroupService {

    private final Logger logger = LoggerFactory.getLogger(LinkPlayGroupService.class);
    private final Map<ThingUID, List<Slave>> groups = new ConcurrentHashMap<>();
    private final Map<ThingUID, Map<String, State>> groupStateCache = new ConcurrentHashMap<>();
    private final Map<ThingUID, LinkPlayGroupParticipant> participants = new ConcurrentHashMap<>();

    @Deactivate
    public void deactivate() {
        logger.debug("LinkPlayGroupService deactivated");
        groups.clear();
        participants.clear();
    }

    /**
     * Register a potential group participant (a player)
     * 
     * @param participant
     */
    public void registerParticipant(LinkPlayGroupParticipant participant) {
        participants.put(participant.getThingUID(), participant);
        notifyAllParticipantsUpdated();
    }

    /**
     * Unregister a participant
     * 
     * @param participant
     */
    public void unregisterParticipant(LinkPlayGroupParticipant participant) {
        ThingUID uid = participant.getThingUID();
        groups.remove(uid);
        participants.remove(uid);
        groupStateCache.remove(uid);
        notifyAllParticipantsUpdated();
    }

    /**
     * Get the leader of a group
     * 
     * @param member
     * @return the leader of the group (which could also be the member)
     */
    public @Nullable LinkPlayGroupParticipant getLeader(LinkPlayGroupParticipant member) {
        // member is the leader
        if (groups.containsKey(member.getThingUID())) {
            return member;
        }
        // member is a slave
        String memberIp = member.getIpAddress();
        for (Map.Entry<ThingUID, List<Slave>> entry : groups.entrySet()) {
            if (entry.getValue().stream().anyMatch(slave -> slave.ip.equals(memberIp))) {
                return participants.get(entry.getKey());
            }
        }
        return null;
    }

    /**
     * Join a member to a group
     * 
     * @param member
     * @param leaderThingUID the ThingUID of the leader
     */
    public void joinGroup(LinkPlayGroupParticipant member, ThingUID leaderThingUID) {
        // first remove the member from any existing group, including their own
        LinkPlayGroupParticipant oldLeader = getLeader(member);
        if (oldLeader != null) {
            unGroup(oldLeader);
        }
        LinkPlayGroupParticipant leader = participants.get(leaderThingUID);
        if (leader != null) {
            try {
                member.getApiClient().multiroomJoinGroupMaster(leader.getIpAddress()).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error joining group: {}", e.getMessage(), e);
            }
            refreshMemberSlaveList(leader);
        }
    }

    /**
     * Remove the member from participating in a group
     * 
     * @param member
     */
    public void unGroup(LinkPlayGroupParticipant member) {
        LinkPlayGroupParticipant leader = getLeader(member);
        try {
            member.getApiClient().multiroomUngroup().get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error ungrouping: {}", e.getMessage(), e);
        }
        if (leader != null) {
            refreshMemberSlaveList(leader);
        }
    }

    /**
     * Adds or moves a member to a group
     * 
     * @param leader
     * @param memberThingUID
     */
    public void addOrMoveMember(LinkPlayGroupParticipant leader, ThingUID memberThingUID) {
        LinkPlayGroupParticipant member = participants.get(memberThingUID);
        if (member != null) {
            addOrMoveMember(leader, member);
            refreshMemberSlaveList(leader);
        }
    }

    /**
     * Adds all registered players to a single group (play everywhere)
     * 
     * @param leader
     */
    public void addAllMembers(LinkPlayGroupParticipant leader) {
        if (!groups.containsKey(leader.getThingUID())) {
            unGroup(leader);
        }
        participants.values().forEach(participant -> {
            if (leader.equals(participant)) {
                return;
            }
            addOrMoveMember(leader, participant.getThingUID());
        });
        refreshMemberSlaveList(leader);
    }

    public void updateGroupState(LinkPlayGroupParticipant leader, String channelId, State state) {
        Map<String, State> cache = groupStateCache.computeIfAbsent(leader.getThingUID(),
                k -> new ConcurrentHashMap<>());
        cache.put(channelId, state);

        // Notify all group members except the leader
        String leaderIp = leader.getIpAddress();
        groups.getOrDefault(leader.getThingUID(), List.of()).stream().filter(slave -> !slave.ip.equals(leaderIp))
                .forEach(slave -> notifyParticipant(slave.ip, p -> p.groupProxyUpdateState(channelId, state)));
    }

    /**
     * Get the list of slaves in a group
     * 
     * @param member
     * @return the list of slaves in the group
     */
    public List<Slave> getGroupListForLeader(LinkPlayGroupParticipant leader) {
        LinkPlayGroupParticipant groupLeader = getLeader(leader);
        if (groupLeader != null) {
            return groups.getOrDefault(groupLeader.getThingUID(), List.of());
        }
        return List.of();
    }

    /**
     * Refresh the list of slaves in a group
     * to be called when we get the UPNP event "Slave"
     * 
     * @param member
     */
    public void refreshMemberSlaveList(LinkPlayGroupParticipant member) {
        try {
            SlaveListResponse slaveList = member.getApiClient().multiroomGetSlaveList().get();
            List<Slave> slaves = slaveList.slaveList == null ? List.of() : slaveList.slaveList;

            if (slaves.isEmpty()) {
                unregisterGroup(member);
                return;
            }

            ThingUID leaderUID = member.getThingUID();
            List<Slave> oldSlaves = groups.put(leaderUID, slaves);

            // Notify participants that were removed from the group
            if (oldSlaves != null) {
                oldSlaves.stream().filter(slave -> slaves.stream().noneMatch(s -> s.ip.equals(slave.ip)))
                        .forEach(slave -> notifyParticipant(slave.ip, p -> p.removedFromGroup(member)));
            }

            // Notify participants that are now in the group and apply cached states
            Map<String, State> cachedStates = groupStateCache.computeIfAbsent(leaderUID,
                    k -> new ConcurrentHashMap<>());
            slaves.forEach(slave -> {
                LinkPlayGroupParticipant participant = findParticipantByIp(slave.ip);
                if (participant != null) {
                    participant.addedToOrUpdatedGroup(member, slaves);
                    cachedStates.forEach((channelId, state) -> participant.groupProxyUpdateState(channelId, state));
                }
            });

            member.addedToOrUpdatedGroup(member, slaves);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error getting slave list: {}", e.getMessage(), e);
        }
    }

    public CompletableFuture<@Nullable Void> playNotification(LinkPlayGroupParticipant member, String url) {
        LinkPlayGroupParticipant leader = getLeader(member);
        if (leader != null && !leader.equals(member)) {
            return leader.playNotification(url);
        }
        return CompletableFuture.failedFuture(new IllegalStateException("Not in a group"));
    }

    public @Nullable Slave findSlaveByUDN(String udn) {
        String formattedUdn = udn.replace("-", "").toUpperCase();
        logger.debug("Finding slave by UDN: {} formatted: {} # of Slaves: {}", udn, formattedUdn,
                groups.values().stream().flatMap(slaves -> slaves.stream()).count());
        return groups.values().stream().flatMap(slaves -> slaves.stream())
                .filter(slave -> slave.uuid != null && formattedUdn.contains(slave.uuid.toUpperCase())).findFirst()
                .orElse(null);
    }

    private void unregisterGroup(LinkPlayGroupParticipant leader) {
        ThingUID leaderUID = leader.getThingUID();
        List<Slave> slaves = groups.remove(leaderUID);
        if (slaves != null) {
            slaves.forEach(slave -> notifyParticipant(slave.ip, p -> p.removedFromGroup(leader)));
            leader.removedFromGroup(leader);
        }
        groupStateCache.remove(leaderUID);
    }

    private void addOrMoveMember(LinkPlayGroupParticipant leader, LinkPlayGroupParticipant member) {
        try {
            String memberIp = member.getIpAddress();
            String leaderIp = leader.getIpAddress();
            List<Slave> slaves = groups.get(leader.getThingUID());
            if (slaves != null) {
                if (slaves.stream().anyMatch(slave -> slave.ip.equals(memberIp))) {
                    leader.getApiClient().multiroomSlaveKickout(memberIp).get();
                    return;
                }
            }
            member.getApiClient().multiroomJoinGroupMaster(leaderIp).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error adding member: {}", e.getMessage(), e);
        }
    }

    /**
     * Find a participant by IP address (used internally for API operations)
     * 
     * @param ipAddress
     * @return the participant with the given IP address, or null if not found
     */
    private @Nullable LinkPlayGroupParticipant findParticipantByIp(String ipAddress) {
        return participants.values().stream().filter(p -> p.getIpAddress().equals(ipAddress)).findFirst().orElse(null);
    }

    /**
     * Find a participant by IP and execute an action if found
     * 
     * @param ipAddress the IP address to find
     * @param action the action to perform on the participant if found
     */
    private void notifyParticipant(String ipAddress, Consumer<LinkPlayGroupParticipant> action) {
        LinkPlayGroupParticipant participant = findParticipantByIp(ipAddress);
        if (participant != null) {
            action.accept(participant);
        }
    }

    /**
     * Notify all participants that the participant list has been updated
     */
    private void notifyAllParticipantsUpdated() {
        participants.values().forEach(p -> p.groupParticipantsUpdated(participants.values()));
    }
}
