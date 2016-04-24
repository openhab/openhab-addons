package org.openhab.binding.hyperion.internal.protocol;

public interface HyperionStateListener {

    public void stateChanged(String property, Object oldValue, Object newValue);

}
