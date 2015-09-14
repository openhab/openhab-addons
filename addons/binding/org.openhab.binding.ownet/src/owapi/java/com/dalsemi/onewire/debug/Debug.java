
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Dallas Semiconductor Corporation, All Rights Reserved.
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
package com.dalsemi.onewire.debug;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.utils.Convert;
import java.io.PrintStream;

/**
 * <p>This class is intended to help both developers of the 1-Wire API for
 * Java and developers using the 1-Wire API for Java to have a standard
 * method for printing debug messages.  Applications that want to see debug messages
 * should call  the <code>setDebugMode(boolean)</code> method.
 * Classes that want to print information under debugging
 * circumstances should call the <code>debug(String)</code>
 * method.</p>
 *
 * <p>Debug printing is turned off by default.</p>
 *
 * @version    1.00, 1 Sep 2003
 * @author     KA, SH
 */
public class Debug
{
	private static boolean DEBUG = false;
	private static PrintStream out = System.out;

   /**
    * Static constructor.  Checks system properties to see if debugging
    * is enabled by default.  Also, will redirect debug output to a log
    * file if specified.
    */
	static
	{
	   String enable = OneWireAccessProvider.getProperty("onewire.debug");
	   if(enable!=null && enable.toLowerCase().equals("true"))
	      DEBUG = true;
	   else
	      DEBUG = false;

	   if(DEBUG)
	   {
	      String logFile = OneWireAccessProvider.getProperty("onewire.debug.logfile");
	      if(logFile!=null)
	      {
   	      try
   	      {
   	         out = new PrintStream(new java.io.FileOutputStream(logFile), true);
   	      }
   	      catch(Exception e)
   	      {
   	         out = System.out;
   	         debug("Error in Debug Static Constructor", e);
   	      }
   	   }
	   }
	}

   /**
    * Sets the debug printing mode for this application.
    *
    * @param <code>true</code> to see debug messages, <code>false</code>
    *        to suppress them
    */
	public static final void setDebugMode(boolean onoff)
	{
		DEBUG = onoff;
	}

   /**
    * Gets the debug printing mode for this application.
    *
    * @return <code>true</code> indicates debug messages are on,
    *         <code>false</code> supresses them.
    */
	public static final boolean getDebugMode()
	{
		return DEBUG;
	}

	/**
	 * Sets the output stream for printing the debug info.
	 *
	 * @param out the output stream for printing the debug info.
	 */
	public static final void setPrintStream(PrintStream outStream)
	{
      out = outStream;
	}

   /**
    * Prints the specified <code>java.lang.String</code> object
    * if debug mode is enabled.  This method calls <code>PrintStream.println(String)</code>,
    * and pre-pends the <code>String</code> ">> " to the message, so
    * taht if a program were to call (when debug mode was enabled):
    * <code><pre>
    *     com.dalsemi.onewire.debug.Debug.debug("Some notification...");
    * </pre></code>
    * the resulting output would look like:
    * <code><pre>
    *     >> Some notification...
    * </pre></code>
    *
    * @param x the message to print out if in debug mode
    */
   public static final void debug(String x)
   {
      if (DEBUG)
         out.println(">> " + x);
   }

   /**
    * Prints the specified array of bytes with a given label
    * if debug mode is enabled.  This method calls
    * <code>PrintStream.println(String)</code>,
    * and pre-pends the <code>String</code> ">> " to the message, so
    * taht if a program were to call (when debug mode was enabled):
    * <code><pre>
    *     com.dalsemi.onewire.debug.Debug.debug("Some notification...", myBytes);
    * </pre></code>
    * the resulting output would look like:
    * <code><pre>
    *     >> my label
    *     >>   FF F1 F2 F3 F4 F5 F6 FF
    * </pre></code>
    *
    * @param lbl the message to print out above the array
    * @param bytes the byte array to print out
    */
	public static final void debug(String lbl, byte[] bytes)
	{
      if (DEBUG)
         debug(lbl, bytes, 0, bytes.length);
   }

   /**
    * Prints the specified array of bytes with a given label
    * if debug mode is enabled.  This method calls
    * <code>PrintStream.println(String)</code>,
    * and pre-pends the <code>String</code> ">> " to the message, so
    * taht if a program were to call (when debug mode was enabled):
    * <code><pre>
    *     com.dalsemi.onewire.debug.Debug.debug("Some notification...", myBytes, 0, 8);
    * </pre></code>
    * the resulting output would look like:
    * <code><pre>
    *     >> my label
    *     >>   FF F1 F2 F3 F4 F5 F6 FF
    * </pre></code>
    *
    * @param lbl the message to print out above the array
    * @param bytes the byte array to print out
    * @param offset the offset to start printing from the array
    * @param length the number of bytes to print from the array
    */
	public static final void debug(String lbl, byte[] bytes, int offset, int length)
	{
      if (DEBUG)
      {
         out.print(">> " + lbl + ", offset=" + offset + ", length=" + length);
         length += offset;
         int inc = 8;
         boolean printHead = true;
         for(int i=offset; i<length; i+=inc)
         {
            if(printHead)
            {
               out.println();
               out.print(">>    ");
            }
            else
            {
               out.print(" : ");
            }
            int len = Math.min(inc, length-i);
            out.print(Convert.toHexString(bytes, i, len, " "));
            printHead = !printHead;
         }
         out.println();
      }
   }

   /**
    * Prints the specified exception with a given label
    * if debug mode is enabled.  This method calls
    * <code>PrintStream.println(String)</code>,
    * and pre-pends the <code>String</code> ">> " to the message, so
    * taht if a program were to call (when debug mode was enabled):
    * <code><pre>
    *     com.dalsemi.onewire.debug.Debug.debug("Some notification...", exception);
    * </pre></code>
    * the resulting output would look like:
    * <code><pre>
    *     >> my label
    *     >>   OneWireIOException: Device Not Present
    * </pre></code>
    *
    * @param lbl the message to print out above the array
    * @param bytes the byte array to print out
    * @param offset the offset to start printing from the array
    * @param length the number of bytes to print from the array
    */
   public static final void debug(String lbl, Throwable t)
   {
      if (DEBUG)
      {
         out.println(">> " + lbl);
         out.println(">>    " + t.getMessage());
         t.printStackTrace(out);
      }
   }

   /**
    * Prints out an exception stack trace for debugging purposes.
    * This is useful to figure out which functions are calling
    * a particular function at runtime.
    *
    */
   public static final void stackTrace()
   {
      if (DEBUG)
      {
         try
         {
            throw new Exception("DEBUG STACK TRACE");
         }
         catch(Exception e)
         {
            e.printStackTrace(out);
         }
      }
   }

}