/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.listener;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.openwebnet.internal.AutomationState;
import org.openhab.binding.openwebnet.internal.LightState;

/**
 *
 * @author Antoine Laydier
 *
 */
public interface ThingStatusListener extends EventListener {

    void onStatusChange(@NonNull LightState state);

    void onStatusChange(@NonNull AutomationState state);

}
