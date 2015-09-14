/* This file is part of NanoXML.
 *
 * $Revision: 2 $
 * $Date: 3/25/04 6:24p $
 * $Name: RELEASE_1_6_8 $
 *
 * Copyright (C) 2000 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 */


package nanoxml.sax;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Locale;
import org.xml.sax.helpers.AttributeListImpl;
import org.xml.sax.Parser;
import org.xml.sax.DocumentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import nanoxml.XMLElement;
import nanoxml.XMLParseException;


/**
 * This is the SAX adapter for NanoXML. Note that this adapter is provided
 * to make NanoXML "buzzword compliant". If you're not stuck with SAX
 * compatibility, you should use the basic API (nanoxml.NanoXML) which is
 * much more programmer-friendly as it doesn't require the cumbersome use
 * of event handlers and has more powerful attribute-handling methods, but
 * that is just IMHO. If you really want to use the SAX API, I would like you
 * to point to the currently available native SAX parsers.
 * <P>
 * Here are some important notes:
 * <UL><LI>The parser is non-validating.
 *     <LI>The DTD is fully ignored, including <CODE>&lt;!ENTITY...&gt;</CODE>.
 *     <LI>SAXParser is reentrant.
 *     <LI>There is support for a document locator.
 *     <LI>There is no support for mixed content (elements containing both
 *         subelements and CDATA elements)
 *     <LI>There are no ignorableWhitespace() events
 *     <LI>Attribute types are always reported as CDATA
 * </UL>
 * <P>
 * $Revision: 2 $<BR>
 * $Date: 3/25/04 6:24p $<P>
 *
 * @see nanoxml.sax.SAXLocator
 * @see nanoxml.XMLElement
 *
 * @author Marc De Scheemaecker
 *         &lt;<A HREF="mailto:Marc.DeScheemaecker@advalvas.be"
 *         >Marc.DeScheemaecker@advalvas.be</A>&gt;
 * @version 1.6
 */ 
public class SAXParser
   implements Parser
{

   /**
    * The associated document handler.
    */
   private DocumentHandler documentHandler;


   /**
    * The associated error handler.
    */
   private ErrorHandler errorHandler;


   /**
    * Initializes the SAX parser adapter.
    */
   public SAXParser()
   {
      this.documentHandler = new HandlerBase();
      this.errorHandler = new HandlerBase();
   }


   /**
    * Sets the locale. Only locales using the language english are accepted.
    *
    * @exception org.xml.sax.SAXException
    *    if <CODE>locale</CODE> is <CODE>null</CODE> or the associated
    *    language is not english.
    */
   public void setLocale(Locale locale)
      throws SAXException
   {
      if ((locale == null) || (! locale.getLanguage().equals("en")))
         {
            throw new SAXException("NanoXML/SAX doesn't support locale: "
                                   + locale);
         }
   }


   /**
    * Sets the entity resolver. As the DTD is ignored, this resolver is never
    * called.
    */
   public void setEntityResolver(EntityResolver resolver)
   {
      // nothing to do
   }


   /**
    * Sets the DTD handler. As the DTD is ignored, this handler is never
    * called.
    */
   public void setDTDHandler(DTDHandler handler)
   {
      // nothing to do
   }


   /**
    * Allows an application to register a document event handler.
    */
   public void setDocumentHandler(DocumentHandler handler)
   {
      this.documentHandler = handler;
   }


   /**
    * Allows an applicaiton to register an error event handler.
    */
   public void setErrorHandler(ErrorHandler handler)
   {
      this.errorHandler = handler;
   }


   /**
    * Handles a subtree of the parsed XML data structure.
    *
    * @exception org.xml.sax.SAXException
    *     if one of the handlers throw such exception
    */
   private void handleSubTree(XMLElement element,
                              SAXLocator locator)
      throws SAXException
   {
      AttributeListImpl attrList = new AttributeListImpl();
      locator.setLineNr(element.getLineNr());
      Enumeration l_enum = element.enumeratePropertyNames();

      while (l_enum.hasMoreElements())
         {
            String key = (String)(l_enum.nextElement());
            String value = element.getProperty(key);
            attrList.addAttribute(key, "CDATA", value);
         }
      
      this.documentHandler.startElement(element.getTagName(), attrList);

      if (element.getContents() == null)
         {
            l_enum = element.enumerateChildren();
            
            while (l_enum.hasMoreElements())
               {
                  this.handleSubTree((XMLElement)(l_enum.nextElement()),
                                     locator);
               }
         }
      else
         {
            char[] chars = element.getContents().toCharArray();
            this.documentHandler.characters(chars, 0, chars.length);
         }

      locator.setLineNr(-1);
      this.documentHandler.endElement(element.getTagName());
   }


   /**
    * Creates the top XML element.
    * Override this method if you need a different parsing behaviour.<P>
    * The default behaviour is:
    * <UL><LI>Case insensitive tag and attribute names, names converted to
    *         uppercase
    *     <LI>The only initial entities are amp, lt, gt, apos and quot.
    *     <LI>Skip formatting whitespace in PCDATA elements.
    * </UL>
    */
   protected XMLElement createTopElement()
   {
      return new XMLElement();
   }


   /**
    * Parses an XML document.
    *
    * @exception org.xml.sax.SAXException
    *    if one of the handlers throws such exception
    * @exception java.io.IOException
    *    if an I/O exception occured while trying to read the document
    */
   public void parse(InputSource source)
      throws SAXException,
             IOException
   {
      XMLElement topElement = this.createTopElement();
      Reader reader = source.getCharacterStream();
      SAXLocator locator = new SAXLocator(source.getSystemId());
      this.documentHandler.setDocumentLocator(locator);
      

      if (reader == null)
         {
            InputStream stream = source.getByteStream();
            String encoding = source.getEncoding();
            
            if (stream == null)
               {
                  String systemId = source.getSystemId();
                  
                  if (systemId == null)
                     {
                        SAXParseException saxException
                           = new SAXParseException("Invalid input source",
                                                   locator);
                        this.errorHandler.fatalError(saxException);
                        return;
                     }
                  
                  try
                     {
                        URL url = new URL(systemId);
                        stream = url.openStream();
                     }
                  catch (MalformedURLException exception)
                     {
                        try
                           {
                              stream = new FileInputStream(systemId);
                           }
                        catch (FileNotFoundException exception2)
                           {
                              SAXParseException saxException
                                 = new SAXParseException(null, locator,
                                                         exception2);
                              this.errorHandler.fatalError(saxException);
                              return;
                           }
                        catch (SecurityException exception2)
                           {
                              SAXParseException saxException
                                 = new SAXParseException(null, locator,
                                                         exception2);
                              this.errorHandler.fatalError(saxException);
                              return;
                           }
                     }
               }

            if (encoding == null)
               {
                  reader = new InputStreamReader(stream);
               }
            else
               {
                  try
                     {
                        reader = new InputStreamReader(stream, encoding);
                     }
                  catch (UnsupportedEncodingException exception)
                     {
                        SAXParseException saxException
                           = new SAXParseException(null, locator, exception);
                        this.errorHandler.fatalError(saxException);
                        return;
                     }
               }
         }

      try
         {
            topElement.parseFromReader(reader);
         }
      catch (XMLParseException exception)
         {
            locator.setLineNr(exception.getLineNr());
            SAXParseException saxException
               = new SAXParseException(null, locator, exception);
            this.errorHandler.fatalError(saxException);
            this.documentHandler.endDocument();
            return;
         }

      locator.setLineNr(topElement.getLineNr());
      this.documentHandler.startDocument();
      this.handleSubTree(topElement, locator);
      this.documentHandler.endDocument();
   }


   /**
    * Parses an XML document from a system identifier (URI).
    *
    * @exception org.xml.sax.SAXException
    *    if one of the handlers throws such exception
    * @exception java.io.IOException
    *    if an I/O exception occured while trying to read the document
    */
   public void parse(String systemId)
      throws SAXException,
             IOException
   {
      this.parse(new InputSource(systemId));
   }


}
