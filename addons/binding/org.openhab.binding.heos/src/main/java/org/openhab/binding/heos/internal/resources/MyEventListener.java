/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.resources;

/**
 * The {@link MyEventListener } is an Event Listener for
 * String messages
 *
 * @author Johannes Einig - Initial contribution
 */

import java.util.ArrayList;
import java.util.HashMap;

public class MyEventListener {

    protected ArrayList<HeosEventListener> listenerList = new ArrayList<HeosEventListener>();

    public void addListener(HeosEventListener listener) {

        listenerList.add(listener);

    }

    public void removeListener(HeosEventListener listener) {

        listenerList.remove(listener);
    }

    public void fireStateEvent(String pid, String event, String command) {

        for (int i = 0; i < listenerList.size(); i++) {

            listenerList.get(i).playerStateChangeEvent(pid, event, command);

        }
    }

    public void fireMediaEvent(String pid, HashMap<String, String> info) {

        for (int i = 0; i < listenerList.size(); i++) {

            listenerList.get(i).playerMediaChangeEvent(pid, info);

        }
    }

    public void fireBridgeEvent(String event, String result, String command) {

        for (int i = 0; i < listenerList.size(); i++) {

            listenerList.get(i).bridgeChangeEvent(event, result, command);

        }
    }

}
