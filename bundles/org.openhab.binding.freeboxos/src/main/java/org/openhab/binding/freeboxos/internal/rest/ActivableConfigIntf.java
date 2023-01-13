package org.openhab.binding.freeboxos.internal.rest;

public interface ActivableConfigIntf {
    public boolean isEnabled();

    public void setEnabled(boolean enabled);
}
