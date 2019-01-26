/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.ui.cometvisu.internal.backend.beans;

/**
 * This is a java bean that is used with JAXB to define the backend configurationfor the
 * Cometvisu client.
 *
 * @author Tobias Bräutigam - Initial contribution
 *
 */
public class ConfigBean {
    public String name = "openhab2";
    public String transport = "sse";
    public String baseURL = "/rest/cv/";
    public ResourcesBean resources;
}
