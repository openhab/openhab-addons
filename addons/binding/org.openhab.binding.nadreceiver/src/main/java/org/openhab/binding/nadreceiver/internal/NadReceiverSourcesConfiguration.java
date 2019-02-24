/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nadreceiver.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.types.StateOption;

/**
 * The {@link NadReceiverSourcesConfiguration} is responsible for collecting configuration values of NAD receiver
 * sources
 *
 * @author Marc Chételat - Initial contribution
 */
public class NadReceiverSourcesConfiguration {
    private Integer maxSources = null;
    private Map<String, NadReceiverSourceConfiguration> configurations = null;

    public NadReceiverSourcesConfiguration(Integer maxSources) {
        if (maxSources == null) {
            throw new IllegalArgumentException("maxSources cannot be null");
        }

        this.maxSources = maxSources;
        this.configurations = new HashMap<String, NadReceiverSourceConfiguration>();
    }

    public void addOrUpdateSourceName(String number, String sourceName) {
        if (configurations.size() > 0 && configurations.get(number) != null) {
            NadReceiverSourceConfiguration config = configurations.get(number);
            config.setName(sourceName);
            configurations.put(number, config);
        } else {
            configurations.put(number, new NadReceiverSourceConfiguration(number, sourceName));
        }
    }

    public void addOrUpdateSourceState(String number, boolean state) {
        if (configurations.size() > 0 && configurations.get(number) != null) {
            NadReceiverSourceConfiguration config = configurations.get(number);
            config.setEnabled(Boolean.valueOf(state));
            configurations.put(number, config);
        } else {
            configurations.put(number, new NadReceiverSourceConfiguration(number, Boolean.valueOf(state)));
        }
    }

    public List<StateOption> getStateOptions() {
        if (!isComplete()) {
            new IllegalStateException("Not all source configurations loaded");
        }
        List<StateOption> options = new ArrayList<StateOption>();
        for (NadReceiverSourceConfiguration config : configurations.values()) {
            if (config.getEnabled()) {
                options.add(new StateOption(config.getNumber(), config.getName()));
            }
        }
        return options;
    }

    public boolean isComplete() {
        if (configurations.values().size() < maxSources.intValue()) {
            return false;
        }
        // If at this stage it means that we already have collected at least one data for
        // all sources, but perhaps something is missing
        for (NadReceiverSourceConfiguration config : configurations.values()) {
            if (!config.isComplete()) {
                return false;
            }
        }
        // If at this stage, everything mandatory collected
        return true;
    }

}
