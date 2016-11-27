/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.config;

/**
 * The {@link AbstractNetatmoThingConfiguration} is the base class for configuration
 * information for all netatmo devices
 *
 * @author Ing. Peter Weiss - Initial contribution
 */
public abstract class AbstractNetatmoThingConfiguration {

    public abstract String getId();

    public abstract String getParentId();

}
