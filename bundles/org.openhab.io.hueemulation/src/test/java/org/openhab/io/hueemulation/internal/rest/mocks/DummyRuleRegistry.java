/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.rest.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.common.registry.RegistryChangeListener;

/**
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DummyRuleRegistry implements RuleRegistry {
    Map<String, Rule> items = new HashMap<>();
    List<RegistryChangeListener<Rule>> listeners = new ArrayList<>();

    @Override
    public void addRegistryChangeListener(RegistryChangeListener<Rule> listener) {
        listeners.add(listener);
    }

    @Override
    public Collection<Rule> getAll() {
        return items.values();
    }

    @Override
    public Stream<Rule> stream() {
        return items.values().stream();
    }

    @Override
    public @Nullable Rule get(@Nullable String key) {
        return items.get(key);
    }

    @Override
    public void removeRegistryChangeListener(RegistryChangeListener<Rule> listener) {
        listeners.remove(listener);
    }

    @Override
    public Rule add(Rule element) {
        items.put(element.getUID(), element);
        for (RegistryChangeListener<Rule> l : listeners) {
            l.added(element);
        }
        return element;
    }

    @Override
    public @Nullable Rule update(Rule element) {
        Rule put = items.put(element.getUID(), element);
        if (put != null) {
            for (RegistryChangeListener<Rule> l : listeners) {
                l.updated(put, element);
            }
        }
        return put;
    }

    @Override
    public @Nullable Rule remove(String key) {
        Rule put = items.remove(key);
        if (put != null) {
            for (RegistryChangeListener<Rule> l : listeners) {
                l.removed(put);
            }
        }
        return put;
    }

    @Override
    public Collection<Rule> getByTag(@Nullable String tag) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Rule> getByTags(String... tags) {
        return Collections.emptyList();
    }
}
