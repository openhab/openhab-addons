/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;

/**
 * Extends the ConfigDescriptionProvider to manually add a ConfigDescription.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public interface NeeoConfigDescriptionProvider extends ConfigDescriptionProvider {

    /**
     * Adds the ConfigDescription to this provider.
     *
     * @param configDescription the config description
     */
    public void addConfigDescription(ConfigDescription configDescription);

}
