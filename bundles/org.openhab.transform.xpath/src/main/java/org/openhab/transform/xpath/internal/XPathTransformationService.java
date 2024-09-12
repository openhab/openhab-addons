/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.transform.xpath.internal;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * <p>
 * The implementation of {@link TransformationService} which transforms the input by XPath Expressions.
 *
 * @author Thomas.Eichstaedt-Engelen - Initial contribution
 */
@NonNullByDefault
@Component(property = { "openhab.transform=XPATH" })
public class XPathTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(XPathTransformationService.class);

    @Override
    public @Nullable String transform(String xpathExpression, String source) throws TransformationException {
        if (xpathExpression == null || source == null) {
            throw new TransformationException("the given parameters 'xpath' and 'source' must not be null");
        }

        logger.debug("about to transform '{}' by the function '{}'", source, xpathExpression);

        StringReader stringReader = null;

        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
            domFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            domFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            domFactory.setXIncludeAware(false);
            domFactory.setExpandEntityReferences(false);
            domFactory.setNamespaceAware(true);
            domFactory.setValidating(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();

            stringReader = new StringReader(source);
            InputSource inputSource = new InputSource(stringReader);
            inputSource.setEncoding("UTF-8");

            Document doc = builder.parse(inputSource);

            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile(xpathExpression);

            String transformationResult = (String) expr.evaluate(doc, XPathConstants.STRING);

            logger.debug("transformation resulted in '{}'", transformationResult);

            return transformationResult;
        } catch (Exception e) {
            throw new TransformationException("transformation throws exceptions", e);
        } finally {
            if (stringReader != null) {
                stringReader.close();
            }
        }
    }
}
