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
package org.openhab.binding.amazonechocontrol.internal.dto.push;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PushMessageTO} is used to handle activity messages
 *
 * @author Jan N. Klug - Initial contribution
 */
public class PushMessageTO {

    public DirectiveTO directive = new DirectiveTO();

    @Override
    public @NonNull String toString() {
        return "MessageTO{directive=" + directive + "}";
    }

    public static class DirectiveTO {
        public HeaderTO header = new HeaderTO();
        public PayloadTO payload = new PayloadTO();

        @Override
        public @NonNull String toString() {
            return "DirectiveTO{header=" + header + ", payload=" + payload + "}";
        }
    }

    public static class HeaderTO {
        public String namespace;
        @SerializedName("name")
        public String directiveName;
        public String messageId;

        @Override
        public @NonNull String toString() {
            return "HeaderTO{namespace='" + namespace + "', directiveName='" + directiveName + "', messageId='"
                    + messageId + "'}";
        }
    }

    public static class PayloadTO {
        public List<RenderingUpdateTO> renderingUpdates = List.of();

        @Override
        public @NonNull String toString() {
            return "PayloadTO{renderingUpdates=" + renderingUpdates + "}";
        }
    }

    public static class RenderingUpdateTO {
        public String route;
        public String resourceId;
        public String resourceMetadata;

        @Override
        public @NonNull String toString() {
            return "RenderingUpdateTO{route='" + route + "', resourceId='" + resourceId + "', resourceMetadata='"
                    + resourceMetadata + "'}";
        }
    }
}
