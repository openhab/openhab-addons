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
package org.openhab.binding.mielecloud.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper class for testing websites. Allows for easy access to the document contents.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class Website {
    private String content;

    protected Website(String content) {
        this.content = content;
    }

    /**
     * Gets the part of the content representing the element that surrounds the given text.
     */
    private String getElementSurrounding(String text) {
        int index = content.indexOf(text);
        if (index == -1) {
            throw new IllegalStateException("Could not find \"" + text + "\" in \"" + content + "\"");
        }

        int elementBegin = content.lastIndexOf('<', index);
        if (elementBegin == -1) {
            throw new IllegalStateException("\"" + text + "\" is not contained in \"" + content + "\"");
        }

        int elementEnd = content.indexOf('>', index);
        if (elementEnd == -1) {
            throw new IllegalStateException("Malformatted HTML content: " + content);
        }

        return content.substring(elementBegin, elementEnd + 1);
    }

    /**
     * Gets the value of an attribute from an element.
     */
    private String getAttributeFromElement(String element, String attribute) {
        int valueStart = element.indexOf(attribute + "=\"");
        if (valueStart == -1) {
            throw new IllegalStateException("Element \"" + element + "\" has no " + attribute);
        }

        int valueEnd = element.indexOf('\"', valueStart + attribute.length() + 2);
        if (valueEnd == -1) {
            throw new IllegalStateException("Malformatted HTML content in element: " + element);
        }

        return element.substring(valueStart + attribute.length() + 2, valueEnd);
    }

    /**
     * Gets the value of the input field with the given name.
     *
     * @param inputName Name of the input field.
     * @return The value of the input field.
     */
    public String getValueOfInput(String inputName) {
        return getAttributeFromElement(getElementSurrounding("name=\"" + inputName + "\""), "value");
    }

    /**
     * Gets the value of the href attribute of the link with the given title text.
     */
    public String getTargetOfLink(String linkTitle) {
        return getAttributeFromElement(getElementSurrounding(linkTitle), "href");
    }

    /**
     * Checks whether the given raw text is contained in the raw website code.
     */
    public boolean contains(String expectedContent) {
        return this.content.contains(expectedContent);
    }

    /**
     * Gets the value of the action attribute of the first form found in the website body.
     */
    public String getFormAction() {
        int formActionStart = content.indexOf("<form action=\"");
        if (formActionStart == -1) {
            throw new IllegalStateException("Could not find a form in \"" + content + "\"");
        }

        int formActionEnd = content.indexOf('\"', formActionStart + 15);
        if (formActionEnd == -1) {
            throw new IllegalStateException("Malformatted HTML content in form: " + content);
        }

        return content.substring(formActionStart + 14, formActionEnd);
    }

    public String getContent() {
        return content;
    }
}
