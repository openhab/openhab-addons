package org.openhab.binding.freeboxos.internal.api;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;

public class MissingPermissionException extends FreeboxException {
    private static final long serialVersionUID = 3965810786699311126L;

    private Permission permission;

    public MissingPermissionException(Permission permission, @NonNull Exception cause, @NonNull String format,
            Object @NonNull... args) {
        super(cause, format, args);
        this.permission = permission;
    }

    public MissingPermissionException(Permission permission, @NonNull String format, Object @NonNull... args) {
        super(format, args);
        this.permission = permission;
    }

    public MissingPermissionException(Permission permission, @NonNull String msg) {
        super(msg);
        this.permission = permission;
    }

    public Permission getPermission() {
        return permission;
    }
}
