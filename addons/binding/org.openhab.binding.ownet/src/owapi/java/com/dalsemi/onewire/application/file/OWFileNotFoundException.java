/*---------------------------------------------------------------------------
 * Copyright (C) 2002 Dallas Semiconductor Corporation, All Rights Reserved.
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
package com.dalsemi.onewire.application.file;

/**
 * <P>Signals that an attempt to open the file on a 1-Wire device denoted.</P>
 *
 * <P> This exception will be thrown by the {@link OWFileInputStream} and
 * {@link OWFileOutputStream} constructors when a file with the specified
 * pathname does not exist on the 1-Wire memory device.</P>
 */
public class OWFileNotFoundException extends java.io.IOException
{

   /**
    * Constructs a <code>FileNotFoundException</code> with
    * <code>null</code> as its error detail message.
    */
   public OWFileNotFoundException()
   {
      super();
   }

   /**
    * Constructs a <code>FileNotFoundException</code> with the
    * specified detail message.
    *
    * @param   s   the detail message.
    */
   public OWFileNotFoundException(String s)
   {
      super(s);
   }
}