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
package org.openhab.binding.hive.internal.client.feature;

import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.FeatureAttribute;
import org.openhab.binding.hive.internal.client.Link;
import org.openhab.binding.hive.internal.client.ReverseLink;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class LinksFeature implements Feature {
    private final @Nullable FeatureAttribute<Set<Link>> links;
    private final @Nullable FeatureAttribute<Set<ReverseLink>> reverseLinks;

    private LinksFeature(
            final @Nullable FeatureAttribute<Set<Link>> links,
            final @Nullable FeatureAttribute<Set<ReverseLink>> reverseLinks
    ) {
        this.links = links;
        this.reverseLinks = reverseLinks;
    }

    public @Nullable FeatureAttribute<Set<Link>> getLinks() {
        return this.links;
    }

    public @Nullable FeatureAttribute<Set<ReverseLink>> getReverseLinks() {
        return this.reverseLinks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable FeatureAttribute<Set<Link>> links;
        private @Nullable FeatureAttribute<Set<ReverseLink>> reverseLinks;
        
        public Builder from(final LinksFeature linksFeature) {
            Objects.requireNonNull(linksFeature);
            
            return this.links(linksFeature.getLinks())
                    .reverseLinks(linksFeature.getReverseLinks());
        }
        
        public Builder links(final @Nullable FeatureAttribute<Set<Link>> links) {
            this.links = links;
            
            return this;
        }

        public Builder reverseLinks(final @Nullable FeatureAttribute<Set<ReverseLink>> reverseLinks) {
            this.reverseLinks = reverseLinks;

            return this;
        }

        public LinksFeature build() {
            return new LinksFeature(links, reverseLinks);
        }
    }
}
