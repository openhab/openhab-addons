/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.util.user;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.gen.current.model.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing Jellyfin users, including filtering, tracking changes,
 * and logging user status. Follows Single Responsibility Principle by separating
 * user management concerns from the main handler.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class UserManager {

    private final Logger logger = LoggerFactory.getLogger(UserManager.class);

    /**
     * User change tracking result containing filtered users and detected changes.
     */
    public record UserChangeResult(List<String> currentUserIds, List<String> addedUserIds, List<String> removedUserIds,
            List<UserDto> enabledVisibleUsers) {
    }

    /**
     * Processes a list of users, filters enabled and visible ones, and detects changes
     * compared to the previously tracked user list.
     *
     * @param users The list of users from the server
     * @param previousUserIds The previously tracked user IDs for comparison
     * @return UserChangeResult containing filtered users and detected changes
     */
    public UserChangeResult processUsersList(List<UserDto> users, List<String> previousUserIds) {
        logger.debug("Retrieved users list from Jellyfin server:");
        logger.debug("  Total users: {}", users.size());

        Predicate<UserDto> isVisible = user -> {
            var policy = user.getPolicy();
            return policy != null && !Boolean.TRUE.equals(policy.getIsHidden());
        };
        Predicate<UserDto> isEnabled = user -> {
            var policy = user.getPolicy();
            return policy != null && !Boolean.TRUE.equals(policy.getIsDisabled());
        };

        List<UserDto> currentUsers = users.stream().filter(isEnabled).filter(isVisible).toList();

        List<String> currentUserIds = currentUsers.stream().map(UserDto::getId).filter(java.util.Objects::nonNull)
                .map(id -> id.toString()).toList();
        List<String> addedUserIds = currentUserIds.stream().filter(id -> !previousUserIds.contains(id)).toList();
        List<String> removedUserIds = previousUserIds.stream().filter(id -> !currentUserIds.contains(id)).toList();

        logger.debug("  Enabled & visible users: {}", currentUsers.size());
        if (!addedUserIds.isEmpty()) {
            logger.info("Added users: {}", addedUserIds);
        }
        if (!removedUserIds.isEmpty()) {
            logger.info("Removed users: {}", removedUserIds);
        }

        return new UserChangeResult(currentUserIds, addedUserIds, removedUserIds, currentUsers);
    }
}
