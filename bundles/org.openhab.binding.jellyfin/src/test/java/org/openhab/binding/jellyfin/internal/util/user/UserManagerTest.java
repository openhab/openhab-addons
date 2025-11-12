package org.openhab.binding.jellyfin.internal.util.user;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserPolicy;

class UserManagerTest {
    private final UserManager userManager = new UserManager();

    private UserDto createUser(String id, Boolean isDisabled, Boolean isHidden) {
        UserDto user = new UserDto();
        user.setId(java.util.UUID.fromString("00000000-0000-0000-0000-00000000000" + id));

        UserPolicy policy = new UserPolicy();
        policy.setIsDisabled(isDisabled);
        policy.setIsHidden(isHidden);
        user.setPolicy(policy);
        return user;
    }

    @Test
    void testFiltersOutHiddenUsers() {
        UserDto user1 = createUser("1", false, false);
        UserDto user2 = createUser("2", false, true);
        UserDto user3 = createUser("3", false, false);
        UserDto user4 = createUser("4", true, false);
        UserDto user5 = createUser("5", false, false);
        List<UserDto> users = List.of(user1, user2, user3, user4, user5);
        List<String> previousUserIds = List.of("1", "2", "3", "4", "5");
        UserManager.UserChangeResult result = userManager.processUsersList(users, previousUserIds);

        // Only user1, user3, user5 should be present (enabled & not hidden)
        assertEquals(List.of("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000003",
                "00000000-0000-0000-0000-000000000005"), result.currentUserIds());
        assertEquals(
                List.of("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000003",
                        "00000000-0000-0000-0000-000000000005"),
                result.enabledVisibleUsers().stream().map(u -> u.getId().toString()).toList());
        // user2 is hidden, user4 is disabled
        assertFalse(result.currentUserIds().contains("00000000-0000-0000-0000-000000000002"));
        assertFalse(result.currentUserIds().contains("00000000-0000-0000-0000-000000000004"));
    }

    @Test
    void testAddedAndRemovedUserIds() {
        UserDto user1 = createUser("1", false, false); // enabled, visible
        UserDto user2 = createUser("2", false, true); // enabled, hidden (should be filtered out)
        UserDto user3 = createUser("3", false, false); // enabled, visible

        List<UserDto> users = List.of(user1, user2, user3);
        List<String> previousUserIds = List.of("00000000-0000-0000-0000-000000000002",
                "00000000-0000-0000-0000-000000000004");

        UserManager.UserChangeResult result = userManager.processUsersList(users, previousUserIds);
        // Only user1 and user3 should be present (user2 is hidden)
        assertEquals(List.of("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000003"),
                result.currentUserIds());
        assertEquals(List.of("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000003"),
                result.addedUserIds());
        assertEquals(List.of("00000000-0000-0000-0000-000000000002", "00000000-0000-0000-0000-000000000004"),
                result.removedUserIds());
    }
}
