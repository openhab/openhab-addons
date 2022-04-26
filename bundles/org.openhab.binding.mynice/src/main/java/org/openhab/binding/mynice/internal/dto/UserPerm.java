package org.openhab.binding.mynice.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum UserPerm {
    // TODO : créer un converter pour désérializer les enums en majuscules
    wait,
    user,
    admin;
}
