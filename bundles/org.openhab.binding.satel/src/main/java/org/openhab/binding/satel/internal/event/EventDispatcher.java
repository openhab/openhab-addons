/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.satel.internal.event;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows distributing incoming event to all registered listeners. Listeners
 * must implement {@link SatelEventListener} interface.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class EventDispatcher {

    private final Logger logger = LoggerFactory.getLogger(EventDispatcher.class);

    private final Set<SatelEventListener> eventListeners = new CopyOnWriteArraySet<>();

    @SuppressWarnings("unchecked")
    private final Map<Class<? extends SatelEvent>, Method> eventHandlers = Stream
            .of(SatelEventListener.class.getDeclaredMethods())
            .filter(m -> m.getParameterCount() == 1 && SatelEvent.class.isAssignableFrom(m.getParameterTypes()[0]))
            .collect(Collectors.toMap(m -> (Class<SatelEvent>) m.getParameterTypes()[0], m -> m));

    /**
     * Add a listener for Satel events.
     *
     * @param eventListener the event listener to add.
     */
    public void addEventListener(SatelEventListener eventListener) {
        this.eventListeners.add(eventListener);
    }

    /**
     * Remove a listener for Satel events.
     *
     * @param eventListener the event listener to remove.
     */
    public void removeEventListener(SatelEventListener eventListener) {
        this.eventListeners.remove(eventListener);
    }

    /**
     * Dispatch incoming event to all listeners.
     *
     * @param event the event to distribute.
     */
    public void dispatchEvent(SatelEvent event) {
        final Method m = eventHandlers.get(event.getClass());
        if (m == null) {
            logger.warn("Missing event handler for event {}. Event discarded.", event.getClass().getName());
        } else {
            logger.debug("Distributing event: {}", event);
            eventListeners.forEach(listener -> {
                logger.trace("Distributing to {}", listener);
                try {
                    m.invoke(listener, event);
                } catch (ReflectiveOperationException e) {
                    logger.warn("Unable to distribute {} to {}", event.getClass().getName(), listener, e);
                }
            });
        }
    }
}
