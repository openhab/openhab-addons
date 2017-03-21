/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract base class for handling input read from a JeeLinkPortReader and converting it to a Reading.
 *
 * @author Volker Bier - Initial contribution
 */
public abstract class JeeLinkReadingConverter<R extends Reading<?>> {
    private final HashMap<String, List<JeeLinkReadingListener<R>>> idListenerMap = new HashMap<>();
    protected final HashMap<JeeLinkReadingListener<R>, String> listenerIdMap = new HashMap<>();

    protected abstract R createReading(String inputLine);

    public abstract boolean convertsTo(Class<?> clazz);

    public R handleInput(String inputLine) {
        R reading = createReading(inputLine);

        if (reading != null) {
            notifyListeners(reading);
        }

        return reading;
    }

    public void addReadingListener(String id, JeeLinkReadingListener<R> listener) {
        synchronized (idListenerMap) {
            List<JeeLinkReadingListener<R>> listeners = idListenerMap.get(id);
            if (listeners == null) {
                listeners = new ArrayList<>();
                idListenerMap.put(id, listeners);
            }
            listeners.add(listener);
            listenerIdMap.put(listener, id);
        }
    }

    public void removeReadingListener(JeeLinkReadingListener<R> listener) {
        if (listener != null) {
            synchronized (idListenerMap) {
                String id = listenerIdMap.remove(listener);
                if (id != null) {
                    List<JeeLinkReadingListener<R>> listeners = idListenerMap.get(id);
                    if (listeners != null) {
                        listeners.remove(listener);

                        if (listeners.size() == 0) {
                            idListenerMap.remove(id);
                        }
                    }
                }
            }
        }
    }

    protected void notifyListeners(R reading) {
        synchronized (idListenerMap) {
            List<JeeLinkReadingListener<R>> listenerL = idListenerMap.get(String.valueOf(reading.getSensorId()));
            if (listenerL != null) {
                for (JeeLinkReadingListener<R> listener : listenerL) {
                    listener.handleReading(reading);
                }
            }

            listenerL = idListenerMap.get(JeeLinkReadingListener.ALL);
            if (listenerL != null) {
                for (JeeLinkReadingListener<R> listener : listenerL) {
                    listener.handleReading(reading);
                }
            }
        }
    }
}
