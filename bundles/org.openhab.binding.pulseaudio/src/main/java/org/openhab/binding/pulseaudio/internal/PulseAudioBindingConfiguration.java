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
package org.openhab.binding.pulseaudio.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains the binding configuration
 *
 * @author Gwendal Roulleau - Initial contribution
 *
 */
@NonNullByDefault
public class PulseAudioBindingConfiguration {

    public boolean sink = true;

    public boolean source = true;

    public boolean sinkInput = false;

    public boolean sourceOutput = false;

    private Set<PulseAudioBindingConfigurationListener> listeners = new HashSet<>();

    public void addPulseAudioBindingConfigurationListener(PulseAudioBindingConfigurationListener listener) {
        listeners.add(listener);
    }

    public void removePulseAudioBindingConfigurationListener(PulseAudioBindingConfigurationListener listener) {
        listeners.remove(listener);
    }

    public void update(PulseAudioBindingConfiguration newConfiguration) {
        sink = newConfiguration.sink;
        source = newConfiguration.source;
        sinkInput = newConfiguration.sinkInput;
        sourceOutput = newConfiguration.sourceOutput;

        listeners.forEach(PulseAudioBindingConfigurationListener::bindingConfigurationChanged);
    }
}
