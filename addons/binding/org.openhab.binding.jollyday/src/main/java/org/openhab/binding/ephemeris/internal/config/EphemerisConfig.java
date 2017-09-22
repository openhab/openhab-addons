/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ephemeris.internal.config;

/**
 * Configuration for a EphemerisHandler.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class EphemerisConfig {
    public String country;
    public String region;
    public String city;
    public Integer offset;
    public String filename;
}
