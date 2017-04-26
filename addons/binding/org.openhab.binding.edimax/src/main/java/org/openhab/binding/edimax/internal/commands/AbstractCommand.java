/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import org.openhab.binding.edimax.internal.ConnectionInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Base class for commands.
 *
 * @author Heinz
 *
 * @param <T>
 */
public abstract class AbstractCommand<T extends Object> {

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
        StringBuffer command = new StringBuffer();
        command.append(XML_HEADER);
        recurseSubList(list, command);
        return command.toString();
    }

    protected void recurseSubList(List<String> list, StringBuffer command) {
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
     * @return
     */
    protected List<String> getPath() {
        ArrayList<String> list = new ArrayList<>();
        list.add("SMARTPLUG");
        return list;
    }

    /**
     * Returns XPath expression to load response for a get request.
     *
     * @return
     */
    protected String getXPathString() {
        List<String> list = getPath();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            sb.append("/" + list.get(i));
        }
        sb.append("/text()");
        return sb.toString();
    }

    /**
     * Format usually used for dates.
     */
    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddkkmmss");

    /**
     * Extract return types from response.
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
                    return (T) format.parse(result);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (tempClass == Boolean.class) {
                if ("ON".equals(result)) {
                    return (T) Boolean.TRUE;
                } else if ("OFF".equals(result)) {
                    return (T) Boolean.FALSE;
                }
            } else if (tempClass == Integer.class) {
                return (T) new Integer(result);
            } else if (tempClass == BigDecimal.class) {
                return (T) new BigDecimal(result);
            } else if (tempClass == String.class) {
                return (T) result;
            }
        }

        throw new RuntimeException("Type unknown " + tType);
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
     * The value to be set when it's a set command.
     */
    protected T setValue;

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
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Returns true if it is a SET command and false for GET.
     *
     * @return
     */
    protected boolean isSet() {
        return (setValue != null);
    }

    /**
     * Do the command.
     *
     * @param ci
     * @return
     * @throws IOException
     */
    public T executeCommand(ConnectionInformation ci) throws IOException {
        String lastPart = "smartplug.cgi";
        String response = executePost(ci.getUrl(), ci.getPort(), lastPart, getCommandString(), ci.getUsername(),
                ci.getPassword());

        return getResultValue(response);
    }

    private String executePost(String targetURL, int targetPort, String targetURlPost, String urlParameters,
            String username, String password) throws IOException {
        String complete = targetURL + ":" + targetPort + "/" + targetURlPost;

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
        StringBuffer res = new StringBuffer();
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
            StringBuffer sb = new StringBuffer();
            sb.append(createStartTag(aName));
            sb.append(marshal(setValue));
            sb.append(createEndTag(aName));
            return sb.toString();
        } else {
            return "<" + aName + "/>";
        }
    }

    /**
     * HELPER.
     */
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF8\"?>\r\n";

    /**
     * XML Helper.
     */
    protected static final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

    /**
     * Extract the innermost information of the response as string.
     *
     * @param document
     * @param xpathExpression
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    protected String extractValueFromXML(String document, String xpathExpression)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        ByteArrayInputStream tempIS = new ByteArrayInputStream(document.getBytes());
        Document dDoc = builder.parse(tempIS);

        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.evaluate(xpathExpression, dDoc, XPathConstants.NODE);
        return node.getNodeValue();
    }

}