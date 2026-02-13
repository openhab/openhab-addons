package org.openhab.binding.restify.internal;

public record RestifyBindingConfig(boolean enforceAuthentication) {
    public static final RestifyBindingConfig DEFAULT = new RestifyBindingConfig(false);
}
