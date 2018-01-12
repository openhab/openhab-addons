/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.type;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;

/**
 * Extends the ConfigDescriptionProvider to manually add a ConfigDescription.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface HomematicConfigDescriptionProvider extends ConfigDescriptionProvider {

    /**
     * Adds the ConfigDescription to this provider.
     */
    public void addConfigDescription(ConfigDescription configDescription);

}
