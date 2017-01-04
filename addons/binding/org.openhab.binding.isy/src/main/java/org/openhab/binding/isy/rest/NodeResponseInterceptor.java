package org.openhab.binding.isy.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class NodeResponseInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext ctx) throws IOException, WebApplicationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document document;
        try {
            document = dbf.newDocumentBuilder().parse(ctx.getInputStream());
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
            try {
                removeNodes(document, xpath.compile("//nodes/root"));
                removeNodes(document, xpath.compile("//nodes/folder"));
                removeNodes(document, xpath.compile("//nodes/group"));
            } catch (XPathExpressionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            TransformerFactory tf = TransformerFactory.newInstance();

            try {
                Transformer t = tf.newTransformer();
                try {
                    ByteArrayOutputStream newOutStream = new ByteArrayOutputStream();
                    t.transform(new DOMSource(document), new StreamResult(newOutStream));
                    ctx.setInputStream(new ByteArrayInputStream(newOutStream.toByteArray()));
                } catch (TransformerException e) {
                    e.printStackTrace();
                }
            } catch (TransformerConfigurationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } catch (SAXException | ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ctx.proceed();
    }

    private void removeNodes(Document document, XPathExpression expression) throws XPathExpressionException {
        Node b13Node;
        b13Node = (Node) expression.evaluate(document, XPathConstants.NODE);
        while (b13Node != null) {
            {
                b13Node.getParentNode().removeChild(b13Node);
                b13Node = (Node) expression.evaluate(document, XPathConstants.NODE);
            }
        }
    }

}
