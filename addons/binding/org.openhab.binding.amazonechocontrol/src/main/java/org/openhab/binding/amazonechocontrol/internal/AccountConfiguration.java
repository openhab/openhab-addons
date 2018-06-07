/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.amazonechocontrol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Account Thing configuration
 *
 * @author Michael Geramb - Initial Contribution
 */
@NonNullByDefault
public class AccountConfiguration {
    @Nullable
    public String email;
    @Nullable
    public String password;
    @Nullable
    public String amazonSite;
    @Nullable
    public Integer pollingIntervalInSeconds;
}
