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
import org.openhab.core.library.types.HSBType;

/**
 * A listener used to notify panels when they change color.
 *
 * @author Jørgen Austvik - Initial contribution
 */

@NonNullByDefault
public interface NanoleafPanelColorChangeListener {

    /**
     * This method is called after a panel changes its color
     *
     * @param newColor the new color of the panel
     */
    void onPanelChangedColor(HSBType newColor);
}
