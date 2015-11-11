/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.karaf.internal;

/**
 * This is a java bean to be used for the JSON structure on the REST API
 *
 * @author Kai Kreuzer
 *
 */
public class Addon {

    public final static String PREFIX = "openhab-";

    public String type;
    public String id;
    public String label;
    public String version;
    public boolean installed;
}
