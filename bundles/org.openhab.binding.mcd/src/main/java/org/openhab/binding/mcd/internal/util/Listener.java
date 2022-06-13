package org.openhab.binding.mcd.internal.util;

import java.util.EventListener;

public interface Listener extends EventListener {
    void onEvent();
}
