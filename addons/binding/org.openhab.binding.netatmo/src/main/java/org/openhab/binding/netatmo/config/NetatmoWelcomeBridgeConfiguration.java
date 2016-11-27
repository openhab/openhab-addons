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
 * The {@link NetatmoWelcomeBridgeConfiguration} is responsible for holding
 * configuration informations needed to access Netatmo API
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 */
public class NetatmoWelcomeBridgeConfiguration extends NetatmoBridgeConfiguration {
    public Integer welcomeEventThings;
    public Integer welcomeUnknownPersonThings;
}
