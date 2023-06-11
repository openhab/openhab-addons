/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.transform.xslt.internal;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The implementation of {@link TransformationService} which transforms the input by XSLT.
 *
 * @author Thomas.Eichstaedt-Engelen
 */
@NonNullByDefault
@Component(property = { "openhab.transform=XSLT" })
public class XsltTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(XsltTransformationService.class);

    /**
     * Transforms the input <code>source</code> by XSLT.
     *
     * The method expects the transformation rule to be read from a file which
     * is stored under the 'configurations/transform' folder. To organize the
     * various transformations one should use subfolders.
     *
     * @param filename the name of the file which contains the XSLT transformation rule.
     *            The name may contain subfoldernames as well
     * @param source the input to transform
     */
    @Override
    public @Nullable String transform(String filename, String source) throws TransformationException {
        if (filename == null || source == null) {
            throw new TransformationException("the given parameters 'filename' and 'source' must not be null");
        }

        Source xsl = null;

        try {
            String path = OpenHAB.getConfigFolder() + File.separator + TransformationService.TRANSFORM_FOLDER_NAME
                    + File.separator + filename;
            xsl = new StreamSource(new File(path));
        } catch (Exception e) {
            String message = "opening file '" + filename + "' throws exception";

            logger.error("{}", message, e);
            throw new TransformationException(message, e);
        }

        logger.debug("about to transform '{}' by the function '{}'", source, xsl);

        StringReader xml = new StringReader(source);
        StringWriter out = new StringWriter();

        Transformer transformer;

        try {
            transformer = TransformerFactory.newInstance().newTransformer(xsl);
            transformer.transform(new StreamSource(xml), new StreamResult(out));
        } catch (Exception e) {
            logger.error("transformation throws exception", e);
            throw new TransformationException("transformation throws exception", e);
        }

        logger.debug("transformation resulted in '{}'", out.toString());

        return out.toString();
    }
}
