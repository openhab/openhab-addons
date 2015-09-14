
/*---------------------------------------------------------------------------
 * Copyright (C) 1999-2001 Dallas Semiconductor Corporation, All Rights Reserved.
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

import org.xml.sax.DocumentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.utils.OWPath;
import com.dalsemi.onewire.application.tag.TaggedDevice;
import java.util.Vector;
import java.util.Stack;
import java.util.EmptyStackException;


/**
 * SAX parser handler that handles XML 1-wire tags.
 */
class TAGHandler
   implements ErrorHandler, DocumentHandler
{

   /**
    * Method setDocumentLocator
    *
    *
    * @param locator
    *
    */
   public void setDocumentLocator(Locator locator)
   {
   }

   /**
    * Method startDocument
    *
    *
    * @throws SAXException
    *
    */
   public void startDocument()
      throws SAXException
   {

      // Instantiate deviceList and clusterStack
      deviceList    = new Vector();
      clusterStack  = new Stack();   // keep track of clusters
      branchStack   = new Stack();   // keep track of current branches
      branchVector  = new Vector();  // keep track of every branch
      branchVectors = new Vector();  // keep a vector of cloned branchStacks
                                     // to use in making the OWPaths Vector
      branchPaths   = new Vector();  // keep track of OWPaths
   }

   /**
    * Method endDocument
    *
    *
    * @throws SAXException
    *
    */
   public void endDocument()
      throws SAXException
   {

      // Iterate through deviceList and make all the 
      // OWPaths from the TaggedDevice's vector of Branches.
      TaggedDevice device;
      OWPath       branchPath;
      Vector       singleBranchVector;

      for (int i = 0; i < deviceList.size(); i++)
      {
         device = (TaggedDevice) deviceList.elementAt(i);

         device.setOWPath(adapter, device.getBranches());
      }

      // Now, iterate through branchVectors and make all the 
      // OWPaths for the Vector of OWPaths
      
      for (int i = 0; i < branchVectors.size(); i++)
      {
         singleBranchVector = (Vector) branchVectors.elementAt(i);
         branchPath = new OWPath(adapter);
         for (int j = 0; j < singleBranchVector.size(); j++)
         {
            device = (TaggedDevice) singleBranchVector.elementAt(i);

            branchPath.add(device.getDeviceContainer(), device.getChannel());
         }
         branchPaths.addElement(branchPath);
      }      
   }

   /**
    * Method startElement
    *
    *
    * @param name
    * @param atts
    *
    * @throws SAXException
    *
    */
   public void startElement(String name, AttributeList atts)
      throws SAXException
   {
      currentElement = name;   //save current element name

      String attributeAddr = "null";
      String attributeType = "null";
      String className;
      int    i = 0;

      // Parse cluster elements here, keeping track of them with a Stack.
      if (name.toUpperCase().equals("CLUSTER"))
      {
         for (i = 0; i < atts.getLength(); i++)
         {
            if (atts.getName(i).toUpperCase().equals("NAME"))
            {
               clusterStack.push(atts.getValue(i));
            }
         }
      }

      // Parse sensor, actuator, and branch elements here
      if (name.toUpperCase().equals("SENSOR") || name.toUpperCase().equals("ACTUATOR")
              || name.toUpperCase().equals("BRANCH"))
      {
         for (i = 0; i < atts.getLength(); i++)
         {
            String attName = atts.getName(i);

            if (attName.toUpperCase().equals("ADDR"))
            {
               attributeAddr = atts.getValue(i);
            }

            if (attName.toUpperCase().equals("TYPE"))
            {
               attributeType = atts.getValue(i);
            }
         }

         // instantiate the appropriate object based on tag type 
         // (i.e., "Contact", "Switch", etc).  The only exception 
         // is of type "branch"
         if (name.toUpperCase().equals("BRANCH"))
         {
            attributeType = "branch";
            currentDevice = new TaggedDevice();   // instantiates object        
         }
         else
         {

            // first, find tag type to instantiate by CLASS NAME!
            // if the tag has a "." in it, it indicates the package 
            // path was included in the tag type.
            if (attributeType.indexOf(".") > 0)
            {
               className = attributeType;
            }
            else
               className = "com.dalsemi.onewire.application.tag." + attributeType;

            // instantiate the appropriate object based on tag type (i.e., "Contact", "Switch", etc)
            try
            {
               Class genericClass = Class.forName(className);

               currentDevice = (TaggedDevice) genericClass.newInstance();
            }
            catch(Exception e)
            {
               throw new RuntimeException("Can't load 1-Wire Tag Type class ("
                  + className + "): " + e.getMessage());
            }
         }

         // set the members (fields) of the TaggedDevice object
         currentDevice.setDeviceContainer(adapter, attributeAddr);
         currentDevice.setDeviceType(attributeType);
         currentDevice.setClusterName(getClusterStackAsString(clusterStack,
                 "/"));
         currentDevice.setBranches((Vector) branchStack.clone());   // copy branchStack to it's related object in TaggedDevice

         // ** do branch specific work here: **
         if (name.equals("branch"))
         {

            // push the not-quite-finished branch TaggedDevice on the branch stack.
            branchStack.push(currentDevice);

            // put currentDevice in the branch vector that holds all branch objects.
            branchVector.addElement(currentDevice);

            // put currentDevice in deviceList (if it is of type "branch", of course)
            deviceList.addElement(currentDevice);
         }
      }
   }

   /**
    * Method endElement
    *
    *
    * @param name
    *
    * @throws SAXException
    *
    */
   public void endElement(String name)
      throws SAXException
   {
      if (name.toUpperCase().equals("SENSOR") || name.toUpperCase().equals("ACTUATOR"))
      {

         //System.out.println(name + " element finished");
         deviceList.addElement(currentDevice);

         currentDevice = null;
      }

      if (name.toUpperCase().equals("BRANCH"))
      {
         branchVectors.addElement(branchStack.clone()); // adds a snapshot of 
                                                        // the stack to 
                                                        // make OWPaths later
         
         branchStack.pop();

         currentDevice = null;   // !!! not sure if this is needed.
      }

      if (name.toUpperCase().equals("CLUSTER"))
      {
         clusterStack.pop();
      }
   }

   /**
    * Method characters
    *
    *
    * @param ch
    * @param start
    * @param length
    *
    * @throws SAXException
    *
    */
   public void characters(char ch [], int start, int length)
      throws SAXException
   {
      if (currentElement.toUpperCase().equals("LABEL"))
      {
         if (currentDevice == null)
         {

            // This means we have a branch instead of a sensor or actuator
            // so, set label accordingly
            try
            {
               currentDevice = (TaggedDevice) branchStack.peek();

               currentDevice.setLabel(new String(ch, start, length));

               currentDevice = null;
            }
            catch (EmptyStackException ese)
            {

               // don't do anything yet.
            }
         }
         else
         {
            currentDevice.setLabel(new String(ch, start, length));
         }

         //System.out.println("This device's label is: " + currentDevice.label);
      }

      if (currentElement.toUpperCase().equals("CHANNEL"))
      {
         if (currentDevice == null)
         {

            // This means we have a branch instead of a sensor or actuator
            // so, set channel accordingly
            try
            {
               currentDevice = (TaggedDevice) branchStack.peek();

               currentDevice.setChannelFromString(new String(ch, start,
                       length));

               currentDevice = null;
            }
            catch (EmptyStackException ese)
            {

               // don't do anything yet.
            }
         }
         else
         {
            currentDevice.setChannelFromString(new String(ch, start, length));
         }
      }

      if (currentElement.toUpperCase().equals("MAX"))
      {
         currentDevice.max = new String(ch, start, length);

         //System.out.println("This device's max message is: " + currentDevice.max);
      }

      if (currentElement.toUpperCase().equals("MIN"))
      {
         currentDevice.min = new String(ch, start, length);

         //System.out.println("This device's min message is: " + currentDevice.min);
      }

      if (currentElement.toUpperCase().equals("INIT"))
      {
         currentDevice.setInit(new String(ch, start, length));

         //System.out.println("This device's init message is: " + currentDevice.init);
      }
   }

   /**
    * Method ignorableWhitespace
    *
    *
    * @param ch
    * @param start
    * @param length
    *
    * @throws SAXException
    *
    */
   public void ignorableWhitespace(char ch [], int start, int length)
      throws SAXException
   {
   }

   /**
    * Method processingInstruction
    *
    *
    * @param target
    * @param data
    *
    * @throws SAXException
    *
    */
   public void processingInstruction(String target, String data)
      throws SAXException
   {
   }

   /**
    * Method getTaggedDeviceList
    *
    *
    * @return
    *
    */
   public Vector getTaggedDeviceList()
   {
      return deviceList;
   }

   /**
    * Method setAdapter
    *
    *
    * @param adapter
    *
    * @throws com.dalsemi.onewire.OneWireException
    *
    */
   public void setAdapter(DSPortAdapter adapter)
      throws com.dalsemi.onewire.OneWireException
   {
      this.adapter = adapter;
   }

   /**
    * Method fatalError
    *
    *
    * @param exception
    *
    * @throws SAXParseException
    *
    */
   public void fatalError(SAXParseException exception)
      throws SAXParseException
   {
      System.err.println(exception);

      throw exception;
   }

   /**
    * Method error
    *
    *
    * @param exception
    *
    * @throws SAXParseException
    *
    */
   public void error(SAXParseException exception)
      throws SAXParseException
   {
      System.err.println(exception);

      throw exception;
   }

   /**
    * Method warning
    *
    *
    * @param exception
    *
    */
   public void warning(SAXParseException exception)
   {
      System.err.println(exception);
   }

   /**
    * Method getAllBranches
    *
    *
    * @param no parameters
    *
    * @return Vector of all TaggedDevices of type "branch".
    *
    */
   public Vector getAllBranches()
   {

      return branchVector;

   }

   /**
    * Method getAllBranchPaths
    *
    *
    * @param no parameters
    *
    * @return Vector of all possible OWPaths.
    *
    */
   public Vector getAllBranchPaths()
   {

      return branchPaths;

   }

   /**
    * Method getClusterStackAsString
    *
    *
    * @param clusters
    * @param separator
    *
    * @return
    *
    */
   private String getClusterStackAsString(Stack clusters, String separator)
   {
      String returnString = "";

      for (int j = 0; j < clusters.size(); j++)
      {
         returnString = returnString + separator
                        + (String) clusters.elementAt(j);
      }

      return returnString;
   }

   /** Field adapter           */
   private DSPortAdapter adapter;

   /** Field currentElement           */
   private String currentElement;

   /** Field currentDevice           */
   private TaggedDevice currentDevice;

   /** Field deviceList           */
   private Vector deviceList;

   /** Field clusterStack           */
   private Stack clusterStack;

   /** Field branchStack           */
   private Stack branchStack;     // keep a stack of current branches

   /** Field branchVector           */
   private Vector branchVector;   // to hold all branches

   /** Field branchVectors          */
   private Vector branchVectors;  // to hold all branches to eventually
                                  // make OWPaths

   /** Field branchPaths            */
   private Vector branchPaths;    // to hold all OWPaths to 1-Wire devices.

}
