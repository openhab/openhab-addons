/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.backend.beans;

/**
 * This is a java bean that is used with JAXB to define the backend configurationfor the
 * Cometvisu client.
 *
 * @author Tobias Bräutigam
 * @since 2.0.0
 *
 */
public class ConfigBean {
    public String name = "openhab2";
    public String transport = "sse";
    public String baseURL = "/rest/cv/";
    public ResourcesBean resources;
}
