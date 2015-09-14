/* This file is part of NanoXML.
 *
 * $Revision: 1 $
 * $Date: 4/17/02 6:09p $
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


import org.xml.sax.Locator;


/**
 * A SAXLocator allows applications to associate a SAX event with a document
 * location.
 * <P>
 * Note that the results returned by such locator is valid only during the
 * scope of each document handler method: the application will receive
 * unpredictable results if it attempts to use the locator at any other time.
 * <P>
 * NanoXML only supports line numbers and system IDs, hence
 * <CODE>getColumnNumber()</CODE> always returns <CODE>-1</CODE> and
 * <CODE>getPublicId()</CODE> always returns <CODE>null</CODE>.
 * <P>
 * $Revision: 1 $<BR>
 * $Date: 4/17/02 6:09p $<P>
 *
 * @see nanoxml.sax.SAXParser
 * @see nanoxml.XMLElement
 *
 * @author Marc De Scheemaecker
 *         &lt;<A HREF="mailto:Marc.DeScheemaecker@advalvas.be"
 *         >Marc.DeScheemaecker@advalvas.be</A>&gt;
 * @version 1.6
 */
class SAXLocator
   implements Locator
{

   /**
    * The associated system ID. This ID is only set if the application
    * calls the parse method with a system ID.
    *
    * @see org.xml.sax.Parser#parse(java.lang.String)
    */
   private String systemId;


   /**
    * The current line number.
    */
   private int lineNr;


   /**
    * Creates a new locator.
    *
    * @param systemId  the associated system ID
    */
   SAXLocator(String systemId)
   {
      this.systemId = systemId;
      this.lineNr = -1;
   }


   /**
    * Sets the current line number.
    *
    * @param lineNr  the new line number
    */
   void setLineNr(int lineNr)
   {
      this.lineNr = lineNr;
   }


   /**
    * Returns the public identifier for the current document event.
    * As there is no support for public identifiers in NanoXML, this method
    * simply returns <CODE>null</CODE>.
    */
   public String getPublicId()
   {
      return null;
   }


   /**
    * Returns the system identifier for the current document event.
    * This ID is only set if the application calls the parse method with a
    * system ID.
    *
    * @see org.xml.sax.Parser#parse(java.lang.String)
    */
   public String getSystemId()
   {
      return this.systemId;
   }


   /**
    * Returns the line number associated with the current document event.
    * As NanoXML reports the line number at the <I>beginning</I> of the
    * element (which make more sense IMHO), this number might not be what
    * you expect. For this reason, endElement() and endDocument() events
    * are never associated with a line number.
    */
   public int getLineNumber()
   {
      return this.lineNr;
   }


   /**
    * Returns the column number where the current event ends. As NanoXML has
    * no support for columns, this method simply returns -1.
    */
   public int getColumnNumber()
   {
      return -1;
   }

}
