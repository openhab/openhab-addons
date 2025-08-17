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
package org.openhab.binding.exec.internal.handler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Spawns a new thread that consumes all bytes from the specified {@link InputStream}, converts
 * the stream to characters using the specified {@link Charset} and then returns the content
 * as a {@link List} of {@link String}s, one {@link String} per "line" of text.
 *
 * @author Ravi Nadahar - Initial contribution
 */
@NonNullByDefault
public class TextOutputConsumer {

    /**
     * Consumes the specified {@link InputStream} by spawning a new thread and converts in into text using UTF-8. The
     * result is returned as a {@link FutureTask}.
     *
     * @param inputStream the {@link InputStream} to consume.
     * @return The {@link FutureTask} where the task can be cancelled and the results retrieved.
     */
    public FutureTask<List<String>> consume(InputStream inputStream) {
        return consume(inputStream, null, null);
    }

    /**
     * Consumes the specified {@link InputStream} by spawning a new thread and converts in into text using the
     * specified {@link Charset}. The result is returned as a {@link FutureTask}.
     *
     * @param inputStream the {@link InputStream} to consume.
     * @param charset the {@link Charset} to use for byte to character conversion.
     *            will be generated.
     * @return The {@link FutureTask} where the task can be cancelled and the results retrieved.
     */
    public FutureTask<List<String>> consume(InputStream inputStream, @Nullable Charset charset) {
        return consume(inputStream, charset, null);
    }

    /**
     * Consumes the specified {@link InputStream} by spawning a new thread and converts in into text using UTF-8. The
     * result is returned as a {@link FutureTask}.
     *
     * @param inputStream the {@link InputStream} to consume.
     * @param threadName the name of the worker thread that will do the consumption. If {@code null}, a thread name
     *            will be generated.
     * @return The {@link FutureTask} where the task can be cancelled and the results retrieved.
     */
    public FutureTask<List<String>> consume(InputStream inputStream, @Nullable String threadName) {
        return consume(inputStream, null, threadName);
    }

    /**
     * Consumes the specified {@link InputStream} by spawning a new thread and converts in into text using the
     * specified {@link Charset}. The result is returned as a {@link FutureTask}.
     *
     * @param inputStream the {@link InputStream} to consume.
     * @param charset the {@link Charset} to use for byte to character conversion.
     * @param threadName the name of the worker thread that will do the consumption. If {@code null}, a thread name
     *            will be generated.
     * @return The {@link FutureTask} where the task can be cancelled and the results retrieved.
     */
    public FutureTask<List<String>> consume(final InputStream inputStream, @Nullable Charset charset,
            @Nullable String threadName) {
        FutureTask<List<String>> result = new FutureTask<List<String>>(() -> {
            List<String> output = new ArrayList<>();
            try (InputStreamReader isr = new InputStreamReader(inputStream,
                    charset == null ? StandardCharsets.UTF_8 : charset); BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.add(line);
                }
            }
            return output;
        });

        Thread runner;
        if (threadName == null || threadName.isBlank()) {
            runner = new Thread(result);
        } else {
            runner = new Thread(result, threadName);
        }
        runner.start();
        return result;
    }
}
