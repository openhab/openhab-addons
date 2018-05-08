/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airvisualnode.internal.config;

/**
 * Configuration for AirVisual Node.
 *
 * @author Victor Antonovich - Initial contribution
 */
public class AirVisualNodeConfig {

    public static final String ADDRESS = "address";

    public String address;

    public String username;

    public String password;

    public String share;

    public long refresh;
}
