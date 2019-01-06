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
 * The {@link ResponseFunction} which just reads some dummy data.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class MockResponseFunction implements ResponseFunction {

    @Override
    public BiConsumer<SoftenerConfiguration, Stream<SoftenerInputData>> getResponseFunction(
            Function<String, SoftenerXmlResponse> responseConsumer, Consumer<SoftenerXmlResponse> response) {
        return (config, inputData) -> {
            String responseBody = "<data><code>ok</code><D_Y_5>1234</D_Y_5><D_Y_10_1>abcd</D_Y_10_1></data>";
            response.accept(responseConsumer.apply(responseBody));
        };
    }

    @Override
    public SoftenerXmlResponse editParameter(SoftenerConfiguration config, SoftenerEditData edit,
            Function<String, SoftenerXmlResponse> responseConsumer) throws IOException {
        // TODO Auto-generated method stub
        return new SoftenerXmlResponse();
    }

}
