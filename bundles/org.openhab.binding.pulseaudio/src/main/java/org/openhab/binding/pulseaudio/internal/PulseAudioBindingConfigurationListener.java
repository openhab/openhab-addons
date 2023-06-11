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
package org.openhab.binding.pulseaudio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for listening to configuration change
 *
 * @author Gwendal Roulleau - Initial contribution
 *
 */
@NonNullByDefault
public interface PulseAudioBindingConfigurationListener {

    public void bindingConfigurationChanged();
}
