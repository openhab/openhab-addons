/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonActivity} encapsulate the GSON data of the users me response
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonUsersMeResponse {
    @Nullable
    public String countryOfResidence;
    @Nullable
    public String effectiveMarketPlaceId;
    @Nullable
    public String email;
    @Nullable
    public Boolean eulaAcceptance;
    @Nullable
    public String @Nullable [] features;
    @Nullable
    public String fullName;
    @Nullable
    public Boolean hasActiveDopplers;
    @Nullable
    public String id;
    @Nullable
    public String marketPlaceDomainName;
    @Nullable
    public String marketPlaceId;
    @Nullable
    public String marketPlaceLocale;
}
