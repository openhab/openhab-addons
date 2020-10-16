/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mail.action;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ISendMailActions} interface defines rule actions for sending mail
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface ISendMailActions {
    Boolean sendMail(@Nullable String recipient, @Nullable String subject, @Nullable String text,
            @Nullable List<String> urlStringList);

    Boolean sendHtmlMail(@Nullable String recipient, @Nullable String subject, @Nullable String html,
            @Nullable List<String> urlStringList);
}
