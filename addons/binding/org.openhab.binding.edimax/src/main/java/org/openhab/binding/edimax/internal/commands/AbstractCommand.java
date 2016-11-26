/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.internal.commands;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openhab.binding.edimax.internal.UnknownTypeException;
import org.openhab.binding.edimax.internal.configuration.EdimaxConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Base class for commands.
 *
 * @author Falk Harnisch - Initial Contribution
 *
 * @param <T> Return Type of Commands
 */
public abstract class AbstractCommand<T extends Object> {

    /**
     * HELPER.
     */
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF8\"?>\r\n";

    /**
     * XML Helper.
     */
    protected static final DocumentBuilderFactory DOM_FACTORY = DocumentBuilderFactory.newInstance();

    /**
     * The value to be set when it's a set command.
     */
    protected T setValue;

    private Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

    /**
     * Format used for dates.
     */
    // private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddkkmmss");
    private DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddkkmmss");

    /**
     * GET constructor.
     */
    public AbstractCommand() {
        // default
    }

    /**
     * SET constructor.
     *
     * @param newValue
     */
    public AbstractCommand(T newValue) {
        setValue = newValue;
    }

    protected String getCommandString() {
        List<String> list = getPath();
        StringBuilder command = new StringBuilder();
        command.append(XML_HEADER);
        recurseSubList(list, command);
        return command.toString();
    }

    protected void recurseSubList(List<String> list, StringBuilder command) {
        String element = list.get(0);

        if (list.size() == 1) {
            // basket case
            command.append(createLeafTag(element));
            return;
        }

        // usually.
        command.append(createStartTag(element));
        List<String> subList = list.subList(1, list.size());
        recurseSubList(subList, command);
        command.append(createEndTag(element));
    }

    /**
     * Overwrite and add your path entry.
     *
     * @return List of Path entries.
     */
    protected List<String> getPath() {
        List<String> list = new ArrayList<>();
        list.add("SMARTPLUG");
        return list;
    }

    /**
     * Returns XPath expression to load response for a get request.
     *
     * @return Concatenated Path for the get request
     */
    protected String getXPathString() {
        List<String> list = getPath();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append("/" + list.get(i));
        }
        sb.append("/text()");
        return sb.toString();
    }

    /**
     * Unmarshals the response and generate the correct return types from response.
     *
     * @param result
     * @return
     */
    @SuppressWarnings("unchecked")
    protected T unmarshal(String result) {
        Type tType = extractType();

        if (tType instanceof Class) {
            Class<?> tempClass = (Class<?>) tType;
            if (tempClass == Date.class) {
                try {
                    return (T) LocalDate.parse(result, format);
                } catch (DateTimeParseException e) {
                    logger.error("Error while parsing result '{}'.", result, e);
                }
            } else if (tempClass == Boolean.class) {
                if ("ON".equals(result)) {
                    return (T) Boolean.TRUE;
                } else if ("OFF".equals(result)) {
                    return (T) Boolean.FALSE;
                }
            } else if (tempClass == Integer.class) {
                return (T) Integer.valueOf(result);
            } else if (tempClass == BigDecimal.class) {
                return (T) new BigDecimal(result);
            } else if (tempClass == String.class) {
                return (T) result;
            }
        }
        throw new UnknownTypeException("Type unknown " + tType);
    }

    /**
     * This command to XML.
     *
     * @param value
     * @return
     */
    protected String marshal(T value) {
        return value.toString();
    }

    /**
     * Find type.
     */
    protected Type extractType() {
        Type mySuperclass = this.getClass().getGenericSuperclass();
        Type[] arr = ((ParameterizedType) mySuperclass).getActualTypeArguments();
        Type tType = arr[0];
        return tType;
    }

    /**
     * Return the extracted return type.
     *
     * @param aResponse
     * @return
     */
    protected T getResultValue(String aResponse) {
        try {
            String result = extractValueFromXML(aResponse, getXPathString());
            return unmarshal(result);
        } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException ex) {
            logger.error("Error while extracting the result value from response '{}'", aResponse, ex);
        }

        return null;
    }

    /**
     * Returns true if it is a SET command and false for GET.
     *
     * @return
     */
    protected boolean isSet() {
        return setValue != null;
    }

    /**
     * Do the command.
     *
     * @param ci
     * @return
     * @throws IOException
     */
    public T executeCommand(EdimaxConfiguration ci) throws IOException {
        String lastPart = "smartplug.cgi";
        String response = executePost(ci.getUrl(), EdimaxConfiguration.PORT, lastPart, getCommandString(),
                ci.getUsername(), ci.getPassword());

        return getResultValue(response);
    }

    private String executePost(String targetURL, int targetPort, String targetURlPost, String urlParameters,
            String username, String password) throws IOException {
        String complete = String.format("%s:%s/%s", targetURL, targetPort, targetURlPost);

        HttpURLConnection connection = null;
        try {
            // Create connection
            URL url = new URL(complete);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));

            String userpass = username + ":" + password;
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
            connection.setRequestProperty("Authorization", basicAuth);

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(urlParameters.getBytes());
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Create initial XML tag.
     *
     * @param aName
     * @return
     */
    protected String createStartTag(String aName) {
        StringBuilder res = new StringBuilder();
        res.append("<");
        res.append(aName);
        if ("SMARTPLUG".equals(aName)) {
            res.append(" id=\"edimax\"");
        } else if ("CMD".equals(aName)) {
            if (isSet()) {
                res.append(" id=\"setup\"");
            } else {
                res.append(" id=\"get\"");
            }
        }
        res.append(">");

        return res.toString();
    }

    /**
     * Create end tag.
     *
     * @param aName
     * @return
     */
    protected String createEndTag(String aName) {
        return "</" + aName + ">";
    }

    /**
     * Create leaf node.
     *
     * @param aName
     * @return
     */
    protected String createLeafTag(String aName) {
        if (isSet()) {
            StringBuilder sb = new StringBuilder();
            sb.append(createStartTag(aName));
            sb.append(marshal(setValue));
            sb.append(createEndTag(aName));
            return sb.toString();
        } else {
            return "<" + aName + "/>";
        }
    }

    /**
     * Extract the innermost information of the response as string.
     *
     * @param documentContent
     * @param xpathExpression
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    protected String extractValueFromXML(String documentContent, String xpathExpression)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilder builder = DOM_FACTORY.newDocumentBuilder();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(documentContent.getBytes());
        Document document = builder.parse(inputStream);

        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.evaluate(xpathExpression, document, XPathConstants.NODE);
        return node.getNodeValue();
    }

}
