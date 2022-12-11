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
package org.openhab.binding.nanoleaf.internal.colors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A listener used to notify panels when they change color.
 *
 * @author Jørgen Austvik - Initial contribution
 */

@NonNullByDefault
public interface NanoleafControllerColorChangeListener {

    /**
     * This method is called after any panel changes its color.
     */
    void onPanelChangedColor();
}
