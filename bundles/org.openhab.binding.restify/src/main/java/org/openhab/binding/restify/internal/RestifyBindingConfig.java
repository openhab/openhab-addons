package org.openhab.binding.restify.internal;

import java.io.Serializable;

import org.eclipse.jdt.annotation.Nullable;

public record RestifyBindingConfig(boolean enforceAuthentication, @Nullable String defaultBasic,
        @Nullable String defaultBearer) implements Serializable {
    public static final RestifyBindingConfig DEFAULT = new RestifyBindingConfig(false, null, null);
}
