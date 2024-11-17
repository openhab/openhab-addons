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
package org.openhab.binding.jellyfin.internal.util.user;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
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
        logger.info("Retrieved users list from Jellyfin server:");
        logger.info("  Total users: {}", users.size());

        Predicate<UserDto> isVisible = user -> !user.getPolicy().getIsHidden();
        Predicate<UserDto> isEnabled = user -> !user.getPolicy().getIsDisabled();

        List<UserDto> currentUsers = users.stream().filter(isEnabled).filter(isVisible).toList();

        List<String> currentUserIds = currentUsers.stream().map(u -> u.getId().toString()).toList();
        List<String> addedUserIds = currentUserIds.stream().filter(id -> !previousUserIds.contains(id)).toList();
        List<String> removedUserIds = previousUserIds.stream().filter(id -> !currentUserIds.contains(id)).toList();

        logger.info("  Enabled & visible users: {}", currentUsers.size());
        logger.info("  Added users: {}", addedUserIds);
        logger.info("  Removed users: {}", removedUserIds);

        return new UserChangeResult(currentUserIds, addedUserIds, removedUserIds, currentUsers);
    }
}
