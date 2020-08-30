package org.openhab.binding.modbus.helioseasycontrols.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class HeliosException extends Exception {

    private static final long serialVersionUID = -7256846679824295950L;

    public HeliosException(String msg) {
        super(msg);
    }
}
