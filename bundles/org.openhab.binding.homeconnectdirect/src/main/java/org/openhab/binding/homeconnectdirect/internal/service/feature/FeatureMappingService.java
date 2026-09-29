/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.service.feature;

import java.io.InputStream;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnectdirect.internal.common.xml.exception.ParseException;
import org.openhab.binding.homeconnectdirect.internal.service.feature.model.FeatureMapping;
import org.openhab.binding.homeconnectdirect.internal.service.feature.xml.converter.FeatureMappingConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Service for managing feature mappings.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class FeatureMappingService {

    private final FeatureMapping featureMapping;

    public FeatureMappingService(Path featureMappingFile) throws ParseException {
        featureMapping = readFeatureMappingFile(featureMappingFile);
    }

    public FeatureMappingService(InputStream inputStream) throws ParseException {
        featureMapping = readFeatureMappingFile(inputStream);
    }

    public FeatureMapping getFeatureMapping() {
        return featureMapping;
    }

    private FeatureMapping readFeatureMappingFile(Path featureMappingFile) throws ParseException {
        var xstream = createXStream();
        try {
            return (FeatureMapping) xstream.fromXML(featureMappingFile.toFile());
        } catch (XStreamException e) {
            throw new ParseException("Could not deserialize XML '%s'".formatted(featureMappingFile), e);
        }
    }

    private FeatureMapping readFeatureMappingFile(InputStream inputStream) throws ParseException {
        var xstream = createXStream();
        try {
            return (FeatureMapping) xstream.fromXML(inputStream);
        } catch (XStreamException e) {
            throw new ParseException("Could not deserialize XML input stream", e);
        }
    }

    private XStream createXStream() {
        var xstream = new XStream(new StaxDriver());
        xstream.allowTypesByWildcard(new String[] { FeatureMappingService.class.getPackageName() + ".**" });
        xstream.setClassLoader(getClass().getClassLoader());
        xstream.ignoreUnknownElements();
        xstream.alias("featureMappingFile", FeatureMapping.class);
        xstream.registerConverter(new FeatureMappingConverter());
        return xstream;
    }
}
