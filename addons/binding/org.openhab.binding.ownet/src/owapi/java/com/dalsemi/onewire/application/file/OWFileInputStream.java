
/*---------------------------------------------------------------------------
 * Copyright (C) 2001 Dallas Semiconductor Corporation, All Rights Reserved.
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

import java.io.IOException;
import java.io.InputStream;
import com.dalsemi.onewire.container.OneWireContainer;

/**
 * A <code>OWFileInputStream</code> obtains input bytes
 * from a file in a 1-Wire Filesystem. What files
 * are available depends on the 1-Wire device.
 *
 * <p> Note that the 1-Wire File system can reside across multiple 1-Wire
 *  devices.  In this case only one of the devices need be supplied to the
 *  constructor.  Each device in a multi-device file system contains
 *  information to reacquire the entire list.
 *
 * <p>  File and directory <b> name </b> limitations
 * <ul>
 * <li> File/directory names limited to 4 characters not including extension
 * <li> File/directory names are not case sensitive and will be automatically
 *      changed to all-CAPS
 * <li> Only files can have extensions
 * <li> Extensions are numberical in the range 0 to 125
 * <li> Extensions 100 to 125 are special purpose and not yet implemented or allowed
 * <li> Files can have the read-only attribute
 * <li> Directories can have the hidden attribute
 * <li> It is recommended to limit directory depth to 10 levels to accomodate
 *      legacy implementations
 * </ul>
 *
 * <H3> Usage </H3>
 * <DL>
 * <DD> <H4> Example </H4>
 * Read from a 1-Wire file on device 'owd':
 * <PRE> <CODE>
 *   // get an input stream to the 1-Wire file
 *   OWFileInputStream instream = new OWFileInputStream(owd, "DEMO.0");
 *
 *   // read some data
 *   byte[] data = new byte[2000];
 *   int len = instream.read(data);
 *
 *   // close the stream to release system resources
 *   instream.close();
 *
 * </CODE> </PRE>
 *
 * @author  DS
 * @version 0.01, 1 June 2001
 * @see     com.dalsemi.onewire.application.file.OWFile
 * @see     com.dalsemi.onewire.application.file.OWFileDescriptor
 * @see     com.dalsemi.onewire.application.file.OWFileOutputStream
 */
public class OWFileInputStream
   extends InputStream
{

   //--------
   //-------- Variables
   //--------

   /**
    * File descriptor.
    */
   private OWFileDescriptor fd;

   //--------
   //-------- Constructors
   //--------

   /**
    * Creates a <code>FileInputStream</code> by
    * opening a connection to an actual file,
    * the file named by the path name <code>name</code>
    * in the Filesystem.  A new <code>OWFileDescriptor</code>
    * object is created to represent this file
    * connection.
    * <p>
    * First, if there is a security
    * manager, its <code>checkRead</code> method
    * is called with the <code>name</code> argument
    * as its argument.
    * <p>
    * If the named file does not exist, is a directory rather than a regular
    * file, or for some other reason cannot be opened for reading then a
    * <code>FileNotFoundException</code> is thrown.
    *
    * @param      owd    OneWireContainer that this Filesystem resides on
    * @param      name   the system-dependent file name.
    * @exception  FileNotFoundException  if the file does not exist,
    *                   is a directory rather than a regular file,
    *                   or for some other reason cannot be opened for
    *                   reading.
    */
   public OWFileInputStream(OneWireContainer owd, String name)
      throws OWFileNotFoundException
   {
      fd = new OWFileDescriptor(owd, name);

      // open the file
      try
      {
         fd.open();
      }
      catch (OWFileNotFoundException e)
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException(e.toString());
      }

      // make sure this is not directory
      if (!fd.isFile())
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException("Not a file");
      }
   }

   /**
    * Creates a <code>FileInputStream</code> by
    * opening a connection to an actual file,
    * the file named by the path name <code>name</code>
    * in the Filesystem.  A new <code>OWFileDescriptor</code>
    * object is created to represent this file
    * connection.
    * <p>
    * First, if there is a security
    * manager, its <code>checkRead</code> method
    * is called with the <code>name</code> argument
    * as its argument.
    * <p>
    * If the named file does not exist, is a directory rather than a regular
    * file, or for some other reason cannot be opened for reading then a
    * <code>FileNotFoundException</code> is thrown.
    *
    * @param      owd    array of OneWireContainers that this Filesystem resides on
    * @param      name   the system-dependent file name.
    * @exception  FileNotFoundException  if the file does not exist,
    *                   is a directory rather than a regular file,
    *                   or for some other reason cannot be opened for
    *                   reading.
    */
   public OWFileInputStream(OneWireContainer[] owd, String name)
      throws OWFileNotFoundException
   {
      fd = new OWFileDescriptor(owd, name);

      // open the file
      try
      {
         fd.open();
      }
      catch (OWFileNotFoundException e)
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException(e.toString());
      }

      // make sure this is not directory
      if (!fd.isFile())
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException("Not a file");
      }
   }

   /**
    * Creates a <code>OWFileInputStream</code> by
    * opening a connection to an actual file,
    * the file named by the <code>File</code>
    * object <code>file</code> in the Filesystem.
    * A new <code>OWFileDescriptor</code> object
    * is created to represent this file connection.
    * <p>
    * If the named file does not exist, is a directory rather than a regular
    * file, or for some other reason cannot be opened for reading then a
    * <code>FileNotFoundException</code> is thrown.
    *
    * @param      file   the file to be opened for reading.
    * @exception  FileNotFoundException  if the file does not exist,
    *                   is a directory rather than a regular file,
    *                   or for some other reason cannot be opened for
    *                   reading.
    * @see        com.dalsemi.onewire.application.file.OWFile#getPath()
    */
   public OWFileInputStream(OWFile file)
      throws OWFileNotFoundException
   {
      // get the file descriptor
      try
      {
         fd = file.getFD();
      }
      catch (IOException e)
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException(e.toString());
      }

      // open the file
      try
      {
         fd.open();
      }
      catch (OWFileNotFoundException e)
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException(e.toString());
      }

      // make sure it is not a directory
      if (!fd.isFile())
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException("Not a file");
      }
   }

   /**
    * Creates a <code>OWFileInputStream</code> by using the file descriptor
    * <code>fdObj</code>, which represents an existing connection to an
    * actual file in the Filesystem.
    * <p>
    * If <code>fdObj</code> is null then a <code>NullPointerException</code>
    * is thrown.
    *
    * @param      fdObj   the file descriptor to be opened for reading.
    */
   public OWFileInputStream(OWFileDescriptor fdObj)
   {
      if (fdObj == null)
         throw new NullPointerException("OWFile provided is null");

      fd = fdObj;
   }

   //--------
   //-------- Read Methods
   //--------

   /**
    * Reads a byte of data from this input stream. This method blocks
    * if no input is yet available.
    *
    * @return     the next byte of data, or <code>-1</code> if the end of the
    *             file is reached.
    * @exception  IOException  if an I/O error occurs.
    */
   public int read()
      throws IOException
   {
      if (fd != null)
         return fd.read();
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Reads up to <code>b.length</code> bytes of data from this input
    * stream into an array of bytes. This method blocks until some input
    * is available.
    *
    * @param      b   the buffer into which the data is read.
    * @return     the total number of bytes read into the buffer, or
    *             <code>-1</code> if there is no more data because the end of
    *             the file has been reached.
    * @exception  IOException  if an I/O error occurs.
    */
   public int read(byte b [])
      throws IOException
   {
      if (fd != null)
         return fd.read(b, 0, b.length);
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Reads up to <code>len</code> bytes of data from this input stream
    * into an array of bytes. This method blocks until some input is
    * available.
    *
    * @param      b     the buffer into which the data is read.
    * @param      off   the start offset of the data.
    * @param      len   the maximum number of bytes read.
    * @return     the total number of bytes read into the buffer, or
    *             <code>-1</code> if there is no more data because the end of
    *             the file has been reached.
    * @exception  IOException  if an I/O error occurs.
    */
   public int read(byte b [], int off, int len)
      throws IOException
   {
      if (fd != null)
         return fd.read(b, off, len);
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Skips over and discards <code>n</code> bytes of data from the
    * input stream. The <code>skip</code> method may, for a variety of
    * reasons, end up skipping over some smaller number of bytes,
    * possibly <code>0</code>. The actual number of bytes skipped is returned.
    *
    * @param      n   the number of bytes to be skipped.
    * @return     the actual number of bytes skipped.
    * @exception  IOException  if an I/O error occurs.
    */
   public long skip(long n)
      throws IOException
   {
      if (fd != null)
         return fd.skip(n);
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Returns the number of bytes that can be read from this file input
    * stream without blocking.
    *
    * @return     the number of bytes that can be read from this file input
    *             stream without blocking.
    * @exception  IOException  if an I/O error occurs.
    */
   public int available()
      throws IOException
   {
      if (fd != null)
         return fd.available();
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Closes this file input stream and releases any system resources
    * associated with the stream.
    *
    * @exception  IOException  if an I/O error occurs.
    */
   public void close()
      throws IOException
   {
      if (fd != null)
         fd.close();
      else
         throw new IOException("1-Wire FileDescriptor is null");

      fd = null;
   }

   /**
    * Returns the <code>OWFileDescriptor</code>
    * object  that represents the connection to
    * the actual file in the Filesystem being
    * used by this <code>FileInputStream</code>.
    *
    * @return     the file descriptor object associated with this stream.
    * @exception  IOException  if an I/O error occurs.
    * @see        com.dalsemi.onewire.application.file.OWFileDescriptor
    */
   public final OWFileDescriptor getFD()
      throws IOException
   {
      if (fd != null)
         return fd;
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Ensures that the <code>close</code> method of this file input stream is
    * called when there are no more references to it.
    *
    * @exception  IOException  if an I/O error occurs.
    * @see        com.dalsemi.onewire.application.file.OWFileInputStream#close()
    */
   public void finalize()
      throws IOException
   {
      if (fd != null)
      {
         fd.close();
      }
   }

   //--------
   //-------- Mark Methods
   //--------

   /**
    * Marks the current position in this input stream. A subsequent call to
    * the <code>reset</code> method repositions this stream at the last marked
    * position so that subsequent reads re-read the same bytes.
    *
    * <p> The <code>readlimit</code> arguments tells this input stream to
    * allow that many bytes to be read before the mark position gets
    * invalidated.
    *
    * <p> The general contract of <code>mark</code> is that, if the method
    * <code>markSupported</code> returns <code>true</code>, the stream somehow
    * remembers all the bytes read after the call to <code>mark</code> and
    * stands ready to supply those same bytes again if and whenever the method
    * <code>reset</code> is called.  However, the stream is not required to
    * remember any data at all if more than <code>readlimit</code> bytes are
    * read from the stream before <code>reset</code> is called.
    *
    * <p> The <code>mark</code> method of <code>InputStream</code> does
    * nothing.
    *
    * @param   readlimit   the maximum limit of bytes that can be read before
    *                      the mark position becomes invalid.
    * @see     java.io.InputStream#reset()
    */
   public void mark(int readlimit)
   {
      if (fd != null)
         fd.mark(readlimit);
   }

   /**
    * Repositions this stream to the position at the time the
    * <code>mark</code> method was last called on this input stream.
    *
    * <p> The general contract of <code>reset</code> is:
    *
    * <p><ul>
    *
    * <li> If the method <code>markSupported</code> returns
    * <code>true</code>, then:
    *
    *     <ul><li> If the method <code>mark</code> has not been called since
    *     the stream was created, or the number of bytes read from the stream
    *     since <code>mark</code> was last called is larger than the argument
    *     to <code>mark</code> at that last call, then an
    *     <code>IOException</code> might be thrown.
    *
    *     <li> If such an <code>IOException</code> is not thrown, then the
    *     stream is reset to a state such that all the bytes read since the
    *     most recent call to <code>mark</code> (or since the start of the
    *     file, if <code>mark</code> has not been called) will be resupplied
    *     to subsequent callers of the <code>read</code> method, followed by
    *     any bytes that otherwise would have been the next input data as of
    *     the time of the call to <code>reset</code>. </ul>
    *
    * <li> If the method <code>markSupported</code> returns
    * <code>false</code>, then:
    *
    *     <ul><li> The call to <code>reset</code> may throw an
    *     <code>IOException</code>.
    *
    *     <li> If an <code>IOException</code> is not thrown, then the stream
    *     is reset to a fixed state that depends on the particular type of the
    *     input stream and how it was created. The bytes that will be supplied
    *     to subsequent callers of the <code>read</code> method depend on the
    *     particular type of the input stream. </ul></ul>
    *
    * <p> The method <code>reset</code> for class <code>InputStream</code>
    * does nothing and always throws an <code>IOException</code>.
    *
    * @exception  IOException  if this stream has not been marked or if the
    *               mark has been invalidated.
    * @see     java.io.InputStream#mark(int)
    * @see     java.io.IOException
    */
   public void reset()
      throws IOException
   {
      if (fd != null)
         fd.reset();
   }

   /**
    * Tests if this input stream supports the <code>mark</code> and
    * <code>reset</code> methods. The <code>markSupported</code> method of
    * <code>InputStream</code> returns <code>false</code>.
    *
    * @return  <code>true</code> if this true type supports the mark and reset
    *          method; <code>false</code> otherwise.
    * @see     java.io.InputStream#mark(int)
    * @see     java.io.InputStream#reset()
    */
   public boolean markSupported()
   {
      return true;
   }
}
