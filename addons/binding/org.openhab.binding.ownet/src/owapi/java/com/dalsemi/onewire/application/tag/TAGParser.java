
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000,2001 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package com.dalsemi.onewire.application.tag;

import org.xml.sax.SAXException;
import java.io.IOException;
import org.xml.sax.InputSource;
import java.util.Vector;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import java.io.InputStream;


/**
 * The tag parser parses tagging information.
 */
public class TAGParser
{

   /**
    * Construct the tag parser.
    *
    * @param adapter What port adapter will serve the devices created.
    */
   public TAGParser(DSPortAdapter adapter)
   {
      parser  = XML.createSAXParser();
      handler = new TAGHandler();

      try
      {
         handler.setAdapter(adapter);
      }
      catch (Exception e)
      {
         System.out.println(e);
      }

      parser.setDocumentHandler(handler);
      parser.setErrorHandler(handler);
   }

   /**
    * Returns the vector of TaggedDevice objects described in the TAG file.
    *
    * @param in The XML document to parse.
    *
    * @return Vector of TaggedDevice objects.
    * @throws SAXException If a parse error occurs parsing <var>in</var>.
    * @throws IOException If an I/O error occurs while reading <var>in</var>.
    */
   public Vector parse(InputStream in)
      throws SAXException, IOException
   {
      InputSource insource = new InputSource(in);

      parser.parse(insource);

      Vector v = handler.getTaggedDeviceList();

      return v;
   }

   /**
    * Returns the vector of Branch TaggedDevice objects described in the TAG file.
    * The XML should already be parsed before calling this method.
    *
    * @param in The XML document to parse.
    *
    * @return Vector of Branch TaggedDevice objects.
    */
   public Vector getBranches()
   {
 
      Vector v = handler.getAllBranches();

      return v;
   }

   /**
    * Returns the vector of OWPath objects discovered through parsing 
    * the XML file.  The XML file should already be parsed before calling 
    * this method.
    *
    * @param no parameters.
    *
    * @return Vector of OWPath objects.
    */
   public Vector getOWPaths()
   {

      Vector v = handler.getAllBranchPaths();

      return v;
   }


   /** Field parser           */
   private SAXParser parser;

   /** Field handler           */
   private TAGHandler handler;
}
