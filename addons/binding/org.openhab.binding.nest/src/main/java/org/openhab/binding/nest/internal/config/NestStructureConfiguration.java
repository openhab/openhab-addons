/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.config;

/**
 * The configuration for structures.
 *
 * @author Wouter Born - Add device configuration to allow file based configuration
 */
public class NestStructureConfiguration {
    public static final String STRUCTURE_ID = "structureId";
    /** Structure ID which can be retrieved with the Nest API. */
    public String structureId;
}
