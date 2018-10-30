/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.protocol;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snapcast.internal.data.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract protocol handler
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractController<T extends Identifiable, L> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SnapcastController controller;
    private final Map<String, T> thingState = new HashMap<>();
    private final Map<@Nullable String, @Nullable List<L>> listeners = new HashMap<>();

    /**
     * @param controller The main snapcast controller
     */
    protected AbstractController(SnapcastController controller) {
        this.controller = controller;
    }

    /**
     * Returns the main snapcast controller
     *
     * @return the snapcast controller
     */
    protected SnapcastController getController() {
        return controller;
    }

    /**
     * Returns the cached state of a thing
     *
     * @param id The internal snapcast-id (not the UID) of a thing.
     * @return The cached data structure or {@code null} if nothing is found.
     */
    public @Nullable T getThingState(@Nullable String id) {
        synchronized (thingState) {
            return thingState.get(id);
        }
    }

    /**
     * Returns all cached states.
     *
     * @return The cached data structures
     */
    public Collection<T> listThingState() {
        synchronized (thingState) {
            return thingState.values();
        }
    }

    /**
     * Update the state of a thing in the cache.
     *
     * @param newState the data structure with the new state
     */
    protected void updateThingState(@Nullable T newState) {
        if (newState != null) {
            final String key = newState.getId();
            synchronized (thingState) {
                final @Nullable T savedState = getThingState(key);
                if (savedState == null) {
                    thingState.put(key, newState);
                } else {
                    mergeThingState(savedState, newState);
                }
            }
        }
    }

    /**
     * Merge a new state into an origin state.
     * It is not necessary that the new state provide all attributes. The non-existent attributes are retained.
     *
     * @param origin the origin state
     * @param update the new state
     */
    protected <O extends Identifiable> void mergeThingState(O origin, O update) {
        if (origin.equals(update)) {
            return;
        }

        try {
            BeanInfo bean = Introspector.getBeanInfo(origin.getClass());
            synchronized (thingState) {
                for (PropertyDescriptor pd : bean.getPropertyDescriptors()) {
                    String name = pd.getName();
                    if (name.equals("class") || name.equals("id")) {
                        continue;
                    }
                    Method getMethod = pd.getReadMethod();
                    Method setMethod = pd.getWriteMethod();
                    if (getMethod != null && setMethod != null) {
                        Object value = getMethod.invoke(update);
                        if (value != null) {
                            setMethod.invoke(origin, value);
                        }
                    }
                }
            }
        } catch (IntrospectionException | ReflectiveOperationException | IllegalArgumentException e) {
            logger.error("can't merge ThingState", e);
        }
    }

    /**
     * Clears the entire cache.
     */
    void resetThingState() {
        synchronized (thingState) {
            thingState.clear();
        }
    }

    /**
     * Add a listener to receive status updates.
     *
     * @param id       The internal snapcast-id (not the UID) or {@code null} to receive all status updates
     * @param listener The update handler
     */
    public void addListener(@Nullable String id, L listener) {
        synchronized (listeners) {
            List<L> list = listeners.get(id);
            if (list == null) {
                list = new ArrayList<>();
                listeners.put(id, list);
            }
            list.add(listener);
        }
    }

    /**
     * Remove a listener
     *
     * @param The      internal snapcast-id (not the UID) or {@code null}
     * @param listener The update handler
     * @see #addListener(String, Object)
     */
    public void removeListener(@Nullable String id, L listener) {
        synchronized (listeners) {
            List<L> list = listeners.get(id);
            if (list != null) {
                list.remove(listener);
                if (list.isEmpty()) {
                    listeners.remove(id);
                }
            }
        }
    }

    /**
     * Invoke a function for each listener
     *
     * @param id internal snapcast-id (not the UID)
     * @param fn the consumer
     */
    protected void eachListener(@Nullable String id, Consumer<L> fn) {
        List<L> list1;
        List<L> list2;
        synchronized (this.listeners) {
            list1 = (id != null ? this.listeners.get(id) : null);
            list2 = this.listeners.get(null);
        }
        Set<L> listeners = new HashSet<>();
        if (list1 != null) {
            listeners.addAll(list1);
        }
        if (list2 != null) {
            listeners.addAll(list2);
        }
        for (L l : listeners) {
            fn.accept(l);
        }
    }

}
