/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.xmltv.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link XmlTVConfiguration} class contains fields mapping TV bridge
 * configuration parameters.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class XmlTVConfiguration {
    public String filePath = "";
    public int refresh = 24;
    public String encoding = "UTF8";
}
