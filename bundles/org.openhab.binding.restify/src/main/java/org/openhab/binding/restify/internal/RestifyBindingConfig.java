package org.openhab.binding.restify.internal;

import java.io.Serializable;

public record RestifyBindingConfig(boolean enforceAuthentication) implements Serializable {
    public static final RestifyBindingConfig DEFAULT = new RestifyBindingConfig(false);
}
