/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ikeatradfri.internal;

import com.google.gson.JsonElement;

/**
 * The {@link IkeaTradfriObserveListener} is notified by the bridge thing handler
 * with updated data from the Tradfri gateway.
 *
 * @author Daniel Sundberg - Initial contribution
 */
public interface IkeaTradfriObserveListener {
    public void onDataUpdate(JsonElement data);
}
