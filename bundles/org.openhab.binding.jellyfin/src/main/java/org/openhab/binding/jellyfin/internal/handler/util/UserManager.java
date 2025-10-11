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
package org.openhab.binding.jellyfin.internal.handler.util;

import java.util.List;
import java.util.Set;
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
 * @author Patrik Gfeller - Extracted from ServerHandler for better maintainability
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

        // Local predicates for user filtering - following Single Responsibility Principle
        Predicate<UserDto> isUserEnabled = user -> {
            var policy = user.getPolicy();
            if (policy == null) {
                return true; // No policy means no restrictions
            }
            var isDisabled = policy.getIsDisabled();
            return !Boolean.TRUE.equals(isDisabled);
        };

        Predicate<UserDto> isUserVisible = user -> {
            var policy = user.getPolicy();
            if (policy == null) {
                return true; // No policy means no restrictions
            }
            var isHidden = policy.getIsHidden();
            return !Boolean.TRUE.equals(isHidden);
        };

        // Debug logging for all users
        logAllUsers(users);

        // Filter enabled and visible users using separated predicates
        var enabledVisibleUsers = users.stream().filter(isUserEnabled).filter(isUserVisible).toList();

        var currentUserIds = enabledVisibleUsers.stream().map(user -> user.getId().toString()).toList();

        logger.info("  Enabled and visible users: {}", enabledVisibleUsers.size());

        // Log detailed info for included users
        logIncludedUsers(enabledVisibleUsers);

        // Detect changes compared to previous user list
        var previousUserIdSet = Set.copyOf(previousUserIds);
        var newUserIdSet = Set.copyOf(currentUserIds);

        var addedUserIds = newUserIdSet.stream().filter(id -> !previousUserIdSet.contains(id)).toList();

        var removedUserIds = previousUserIdSet.stream().filter(id -> !newUserIdSet.contains(id)).toList();

        // Log changes
        logUserChanges(users, addedUserIds, removedUserIds);

        return new UserChangeResult(currentUserIds, addedUserIds, removedUserIds, enabledVisibleUsers);
    }

    /**
     * Logs debug information for all users.
     *
     * @param users The list of all users
     */
    private void logAllUsers(List<UserDto> users) {
        users.forEach(user -> {
            var policy = user.getPolicy();
            var isDisabled = policy != null ? policy.getIsDisabled() : null;
            var isHidden = policy != null ? policy.getIsHidden() : null;

            logger.debug("  User: {} (ID: {}, Disabled: {}, Hidden: {})", user.getName(), user.getId(), isDisabled,
                    isHidden);
        });
    }

    /**
     * Logs detailed information for users that were included after filtering.
     *
     * @param enabledVisibleUsers The list of enabled and visible users
     */
    private void logIncludedUsers(List<UserDto> enabledVisibleUsers) {
        enabledVisibleUsers.forEach(user -> {
            logger.debug("    User included: {} (ID: {})", user.getName(), user.getId());
            if (user.getLastLoginDate() != null) {
                logger.debug("      Last login: {}", user.getLastLoginDate());
            }
            if (user.getLastActivityDate() != null) {
                logger.debug("      Last activity: {}", user.getLastActivityDate());
            }
        });
    }

    /**
     * Logs user additions and removals.
     *
     * @param allUsers The complete list of users for name lookup
     * @param addedUserIds The list of added user IDs
     * @param removedUserIds The list of removed user IDs
     */
    private void logUserChanges(List<UserDto> allUsers, List<String> addedUserIds, List<String> removedUserIds) {
        // Log added users with their names
        addedUserIds.forEach(addedUserId -> {
            var userName = allUsers.stream().filter(user -> addedUserId.equals(user.getId().toString()))
                    .map(UserDto::getName).findFirst().orElse("Unknown");
            logger.info("User added: {} (ID: {})", userName, addedUserId);
        });

        // Log removed users with their IDs
        removedUserIds.forEach(removedUserId -> logger.info("User removed: {}", removedUserId));
    }
}
