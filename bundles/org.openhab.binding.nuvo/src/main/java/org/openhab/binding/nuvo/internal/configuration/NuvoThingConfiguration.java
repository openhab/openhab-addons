/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nuvo.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NuvoThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class NuvoThingConfiguration {

    public @Nullable String serialPort;
    public @Nullable String host;
    public @Nullable Integer port;
    public @Nullable Integer numZones;
    public boolean clockSync;
    public String favoriteLabels = "";
    public Integer nuvoNetSrc1 = 0;
    public Integer nuvoNetSrc2 = 0;
    public Integer nuvoNetSrc3 = 0;
    public Integer nuvoNetSrc4 = 0;
    public Integer nuvoNetSrc5 = 0;
    public Integer nuvoNetSrc6 = 0;
    public String favoritesSrc1 = "";
    public String favoritesSrc2 = "";
    public String favoritesSrc3 = "";
    public String favoritesSrc4 = "";
    public String favoritesSrc5 = "";
    public String favoritesSrc6 = "";
    public String favPrefix1 = "";
    public String favPrefix2 = "";
    public String favPrefix3 = "";
    public String favPrefix4 = "";
    public String favPrefix5 = "";
    public String favPrefix6 = "";
    public String menuXmlSrc1 = "";
    public String menuXmlSrc2 = "";
    public String menuXmlSrc3 = "";
    public String menuXmlSrc4 = "";
    public String menuXmlSrc5 = "";
    public String menuXmlSrc6 = "";
}
