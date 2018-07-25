/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.api.model;

/**
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class Me {
    private String displayName;
    private String id;
    private String product;

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }
}
