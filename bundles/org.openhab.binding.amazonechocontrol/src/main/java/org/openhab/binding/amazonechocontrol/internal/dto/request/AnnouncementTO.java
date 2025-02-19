/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.request;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link AnnouncementTO} encapsulates an announcement
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AnnouncementTO {
    public String expireAfter = "PT5S";
    public List<AnnouncementContentTO> content = List.of();
    public AnnouncementTargetTO target = new AnnouncementTargetTO();
    public String customerId;

    @Override
    public @NonNull String toString() {
        return "AnnouncementTO{expireAfter='" + expireAfter + "', content=" + content + ", target=" + target
                + ", customerId='" + customerId + "'}";
    }
}
