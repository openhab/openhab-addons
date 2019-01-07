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
 * The {@link JsonWebSiteCookie} encapsulate the GSON data of register cookie array
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonWebSiteCookie {
    public JsonWebSiteCookie(String name, String value) {
        Name = name;
        Value = value;
    }

    @Nullable
    public String Value;
    @Nullable
    public String Name;
}
