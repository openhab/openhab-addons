/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gruenbecksoftener.handler;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gruenbecksoftener.data.SoftenerEditData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerInputData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerXmlResponse;
import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;

/**
 * The interface for reading out data from the softener.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public interface ResponseFunction {

    /**
     * Retrieves the consumer for reading out specific data from the softener.
     *
     * @param responseConsumer The function which converts a {@link String} to an {@link SoftenerXmlResponse}
     * @param response The {@link Consumer} which will be called for every response.
     * @return The {@link Consumer} which starts the actual read out.
     */
    public BiConsumer<SoftenerConfiguration, Stream<SoftenerInputData>> getResponseFunction(
            Function<String, SoftenerXmlResponse> responseConsumer, Consumer<SoftenerXmlResponse> response);

    /**
     * Edits a parameter from t he softener device.
     *
     * @param config The configuration
     * @param edit The data which shall be edited.
     * @param responseConsumer The {@link Function} that converts the {@link String} result to an
     *            {@link SoftenerXmlResponse}.
     * @return The result.
     * @throws IOException
     */
    public SoftenerXmlResponse editParameter(SoftenerConfiguration config, SoftenerEditData edit,
            Function<String, SoftenerXmlResponse> responseConsumer) throws IOException;
}
