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
package org.openhab.binding.telegram.bot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Provides the actions for the Telegram API.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface ITelegramActions {

    public boolean sendTelegramAnswer(@Nullable Long chatId, @Nullable String replyId, @Nullable String message);

    public boolean sendTelegramAnswer(@Nullable String replyId, @Nullable String message);

    public boolean sendTelegram(@Nullable Long chatId, @Nullable String message);

    public boolean sendTelegram(@Nullable String message);

    public boolean sendTelegramQuery(@Nullable Long chatId, @Nullable String message, @Nullable String replyId,
            @Nullable String... buttons);

    public boolean sendTelegramQuery(@Nullable String message, @Nullable String replyId, @Nullable String... buttons);

    public boolean sendTelegram(@Nullable Long chatId, @Nullable String message, @Nullable Object... args);

    public boolean sendTelegram(@Nullable String message, @Nullable Object... args);

    public boolean sendTelegramPhoto(@Nullable Long chatId, @Nullable String photoURL, @Nullable String caption,
            @Nullable String username, @Nullable String password);

    public boolean sendTelegramPhoto(@Nullable String photoURL, @Nullable String caption, @Nullable String username,
            @Nullable String password);
}
