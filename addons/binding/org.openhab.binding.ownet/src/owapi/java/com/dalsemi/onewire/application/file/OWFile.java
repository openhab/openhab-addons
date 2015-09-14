
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
import java.io.FileNotFoundException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.container.PagedMemoryBank;

/**
 * An abstract representation of file and directory pathnames on 1-Wire devices.
 *
 * <p> User interfaces and operating systems use system-dependent <em>pathname
 * strings</em> to name files and directories.  This class presents an
 * abstract, system-independent view of hierarchical pathnames.  An
 * <em>abstract pathname</em> has two components:
 *
 * <ol>
 * <li> An optional system-dependent <em>prefix</em> string,<br>
 *      such as a disk-drive specifier, <code>"/"</code> for the UNIX root
 *      directory, or <code>"\\"</code> for a Win32 UNC pathname, and
 * <li> A sequence of zero or more string <em>names</em>.
 * </ol>
 *
 * Each name in an abstract pathname except for the last denotes a directory;
 * the last name may denote either a directory or a file.  The <em>empty</em>
 * abstract pathname has no prefix and an empty name sequence.
 *
 * <p> The conversion of a pathname string to or from an abstract pathname is
 * inherently system-dependent.  When an abstract pathname is converted into a
 * pathname string, each name is separated from the next by a single copy of
 * the default <em>separator character</em>.  The default name-separator
 * character is defined by the system property <code>OWFile.separator</code>, and
 * is made available in the public static fields <code>{@link
 * #separator}</code> and <code>{@link #separatorChar}</code> of this class.
 * When a pathname string is converted into an abstract pathname, the names
 * within it may be separated by the default name-separator character or by any
 * other name-separator character that is supported by the underlying system.
 *
 * <p> A pathname, whether abstract or in string form, may be either
 * <em>absolute</em> or <em>relative</em>.  An absolute pathname is complete in
 * that no other information is required in order to locate the file that it
 * denotes.  A relative pathname, in contrast, must be interpreted in terms of
 * information taken from some other pathname.  By default the classes in the
 * <code>java.io</code> package always resolve relative pathnames against the
 * current user directory.  This directory is named by the system property
 * <code>user.dir</code>, and is typically the directory in which the Java
 * virtual machine was invoked.  The pathname provided to this OWFile
 * however is always <em>absolute</em>.
 *
 * <p> The prefix concept is used to handle root directories on UNIX platforms,
 * and drive specifiers, root directories and UNC pathnames on Win32 platforms,
 * as follows:
 *
 * <ul>
 * <li> For 1-Wire the Filesystem , the prefix of an absolute pathname is always
 * <code>"/"</code>.  The abstract pathname denoting the root directory
 * has the prefix <code>"/"</code> and an empty name sequence.
 *
 * </ul>
 *
 * <p> Instances of the <code>OWFile</code> class are immutable; that is, once
 * created, the abstract pathname represented by a <code>OWFile</code> object
 * will never change.
 *
 * <H3> What is Different on the 1-Wire Filesystem </H3>
 * <p> The methods in the class are the same as in the java.io.File version 1.2
 *     with the following exceptions
 * <p>
 * <p> Methods provided but of <b> limited </b> functionallity
 * <ul>
 * <li>   public long lastModified() - always returns 0
 * <li>   public boolean isAbsolute() - always true
 * <li>   public boolean setLastModified(long time) - does nothing
 * <li>   public boolean setReadOnly() - only for files
 * <li>   public boolean isHidden() - only could be true for directories
 * </ul>
 *
 * <p> Methods <b> not </b> provided or supported:
 * <ul>
 * <li>   public void deleteOnExit()
 * <li>   public String[] list(FilenameFilter filter)
 * <li>   public File[] listFiles(FilenameFilter filter)
 * <li>   public File[] listFiles(FileFilter filter)
 * <li>   public static File createTempFile(String prefix, String suffix, File directory)
 * <li>   public static File createTempFile(String prefix, String suffix)
 * <li>   public URL toURL()
 * </ul>
 *
 * <p> <b> Extra </b> Methods (not usually in 1.2 java.io.File)
 * <ul>
 * <li>   public OWFileDescriptor getFD()
 * <li>   public void close()
 * <li>   public OneWireContainer getOneWireContainer()
 * <li>   public void format()
 * <li>   public int getFreeMemory()
 * <li>   public int[] getPageList()
 * <li>   public PagedMemoryBank getMemoryBankForPage(int)
 * <li>   public int getLocalPage(int)
 * </ul>
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
 * <H3> Tips </H3>
 * <ul>
 * <li> <i> Writes </i> will not be flushed to the 1-Wire device Filesystem
 *      until the <code> OWFile </code> instance is closed with the
 *      <code> close() </code> method or the <code> sync() </code> method
 *      from the OWFileDescriptor
 * <li> The <code> sync() </code> method for flushing the changes to the
 *      Filesystem is preferred since it can be called multiple times if
 *      there is a connection problem
 * <li> New 1-Wire devices Filesystem must first be formatted with the
 *      <code> format() </code> method in order for files or directories to
 *      be added or changed.
 * <li> Multiple 1-Wire devices can be linked into a common Filesystem by
 *      using the constructor that accepts an array of 1-Wire devices.  The
 *      first device in the list is the 'root' device and the rest will be
 *      designated 'satelite's.  Once the <code> format() </code> method
 *      is used to link these devices then only the 'root' need be used
 *      in future constuctors of this class or the 1-Wire file stream classes.
 * <li> Only rewrittable 1-Wire memory devices can be used in multi-device
 *      file systems.  EPROM and write-once devices can only be used in
 *      single device file systems.
 * <li> 1-Wire devices have a limited amount of space.  Use the
 *      <code> getFreeMemory() </code> method to get an estimate of free memory
 *      available.
 * <li> Call the <code> close() </code> method to release system resources
 *      allocated when done with the <code> OWFile </code> instance
 * </ul>
 *
 * <H3> Usage </H3>
 * <DL>
 * <DD> <H4> Example 1</H4>
 * Format the Filesystem of the 1-Wire device 'owd':
 * <PRE> <CODE>
 *   // create a 1-Wire file at root
 *   OWFile owfile = new OWFile(owd, "");
 *
 *   // format Filesystem
 *   owfile.format();
 *
 *   // get 1-Wire File descriptor to flush to device
 *   OWFileDescriptor owfd = owfile.getFD();
 *   // loop until sync is successful
 *   do
 *   {
 *      try
 *      {
 *         owfd.sync();
 *         done = true;
 *      }
 *      catch (SyncFailedException e)
 *      {
 *         // do something
 *         ...
 *         done = false;
 *      }
 *   }
 *   while (!done)
 *
 *   // close the 1-Wire file to release system resources
 *   owfile.close();
 * </CODE> </PRE>
 *
 * <DD> <H4> Example 2</H4>
 * Make a multi-level directory structure on the 1-Wire device 'owd':
 * <PRE> <CODE>
 *   OWFile owfile = new OWFile(owd, "/doc/text/temp");
 *
 *   // make the directories
 *   if (owfile.mkdirs())
 *      System.out.println("Success!");
 *   else
 *      System.out.println("Out of memory or invalid file/directory");
 *
 *   // get 1-Wire File descriptor to flush to device
 *   ...
 * </CODE> </PRE>
 * </DL>
 *
 * <H3> 1-Wire File Structure Format </H3>
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/AppNotes/app114.pdf"> http://pdfserv.maxim-ic.com/arpdf/AppNotes/app114.pdf</A>
 * </DL>
 *
 * @see com.dalsemi.onewire.application.file.OWFileDescriptor
 * @see com.dalsemi.onewire.application.file.OWFileInputStream
 * @see com.dalsemi.onewire.application.file.OWFileOutputStream
 *
 * @author  DS
 * @version 0.01, 1 June 2001
 */
public class OWFile
{

   //--------
   //-------- Static Variables
   //--------

   /** Field separator */
   public static final String separator = "/";

   /** Field separatorChar */
   public static final char separatorChar = '/';

   /** Field pathSeparator */
   public static final String pathSeparator = ":";

   /** Field pathSeparatorChar */
   public static final char pathSeparatorChar = ':';

   //--------
   //-------- Variables
   //--------

   /** Abstract file descriptor containing this file */
   private OWFileDescriptor fd;

   //--------
   //-------- Constructor
   //--------

   /**
    * Creates a new <code>OWFile</code> instance by converting the given
    * pathname string into an abstract pathname.  If the given string is
    * the empty string, then the result is the empty abstract pathname.
    *
    * @param   owd    OneWireContainer that this Filesystem resides on
    * @param   pathname  A pathname string
    * @throws  NullPointerException
    *          If the <code>pathname</code> argument is <code>null</code>
    */
   public OWFile(OneWireContainer owd, String pathname)
   {
      fd          = new OWFileDescriptor(owd, pathname);
   }

   /**
    * Creates a new <code>OWFile</code> instance by converting the given
    * pathname string into an abstract pathname.  If the given string is
    * the empty string, then the result is the empty abstract pathname.
    *
    * @param   owd  ordered array of OneWireContainers that this Filesystem
    *               resides on
    * @param   pathname  A pathname string
    * @throws  NullPointerException
    *          If the <code>pathname</code> argument is <code>null</code>

        Change the OWFileDescriptor to accept only an array of containers
        Change the local ref to be an array
        Create a single array in constructors with single passed owc

    */
   public OWFile(OneWireContainer[] owd, String pathname)
   {
      fd       = new OWFileDescriptor(owd, pathname);
   }

   /* Note: The two-argument File constructors do not interpret an empty
      parent abstract pathname as the current user directory.  An empty parent
      instead causes the child to be resolved against the system-dependent
      directory defined by the FileSystem.getDefaultParent method.  On Unix
      this default is "/", while on Win32 it is "\\".  This is required for
      compatibility with the original behavior of this class. */

   /**
    * Creates a new <code>OWFile</code> instance from a parent pathname string
    * and a child pathname string.
    *
    * <p> If <code>parent</code> is <code>null</code> then the new
    * <code>OWFile</code> instance is created as if by invoking the
    * single-argument <code>OWFile</code> constructor on the given
    * <code>child</code> pathname string.
    *
    * <p> Otherwise the <code>parent</code> pathname string is taken to denote
    * a directory, and the <code>child</code> pathname string is taken to
    * denote either a directory or a file.  If the <code>child</code> pathname
    * string is absolute then it is converted into a relative pathname in a
    * system-dependent way.  If <code>parent</code> is the empty string then
    * the new <code>OWFile</code> instance is created by converting
    * <code>child</code> into an abstract pathname and resolving the result
    * against a system-dependent default directory.  Otherwise each pathname
    * string is converted into an abstract pathname and the child abstract
    * pathname is resolved against the parent.
    *
    * @param   owd    OneWireContainer that this Filesystem resides on
    * @param   parent  The parent pathname string
    * @param   child   The child pathname string
    * @throws  NullPointerException
    *          If <code>child</code> is <code>null</code>
    */
   public OWFile(OneWireContainer owd, String parent, String child)
   {
      if (child == null)
         throw new NullPointerException("child is null");

      fd = new OWFileDescriptor(owd, parent + child);
   }

   /**
    * Creates a new <code>OWFile</code> instance from a parent abstract
    * pathname and a child pathname string.
    *
    * <p> If <code>parent</code> is <code>null</code> then the new
    * <code>OWFile</code> instance is created as if by invoking the
    * single-argument <code>OWFile</code> constructor on the given
    * <code>child</code> pathname string.
    *
    * <p> Otherwise the <code>parent</code> abstract pathname is taken to
    * denote a directory, and the <code>child</code> pathname string is taken
    * to denote either a directory or a file.  If the <code>child</code>
    * pathname string is absolute then it is converted into a relative
    * pathname in a system-dependent way.  If <code>parent</code> is the empty
    * abstract pathname then the new <code>OWFile</code> instance is created by
    * converting <code>child</code> into an abstract pathname and resolving
    * the result against a system-dependent default directory.  Otherwise each
    * pathname string is converted into an abstract pathname and the child
    * abstract pathname is resolved against the parent.
    *
    * @param   owd    OneWireContainer that this Filesystem resides on
    * @param   parent  The parent abstract pathname
    * @param   child   The child pathname string
    * @throws  NullPointerException
    *          If <code>child</code> is <code>null</code>
    */
   public OWFile(OWFile parent, String child)
   {
      if (child == null)
         throw new NullPointerException("child is null");

      String new_path;

      if (parent.getAbsolutePath().endsWith("/"))
         new_path = parent.getAbsolutePath() + child;
      else
         new_path = parent.getAbsolutePath() + separator + child;

      fd  = new OWFileDescriptor(parent.getOneWireContainers(), new_path);
   }

   //--------
   //-------- Path 'get' Methods
   //--------

   /**
    * Returns the name of the file or directory denoted by this abstract
    * pathname.  This is just the last name in the pathname's name
    * sequence.  If the pathname's name sequence is empty, then the empty
    * string is returned.
    *
    * @return  The name of the file or directory denoted by this abstract
    *          pathname, or the empty string if this pathname's name sequence
    *          is empty
    */
   public String getName()
   {
      return fd.getName();
   }

   /**
    * Returns the pathname string of this abstract pathname's parent, or
    * <code>null</code> if this pathname does not name a parent directory.
    *
    * <p> The <em>parent</em> of an abstract pathname consists of the
    * pathname's prefix, if any, and each name in the pathname's name
    * sequence except for the last.  If the name sequence is empty then
    * the pathname does not name a parent directory.
    *
    * @return  The pathname string of the parent directory named by this
    *          abstract pathname, or <code>null</code> if this pathname
    *          does not name a parent
    */
   public String getParent()
   {
      return fd.getParent();
   }

   /**
    * Returns the abstract pathname of this abstract pathname's parent,
    * or <code>null</code> if this pathname does not name a parent
    * directory.
    *
    * <p> The <em>parent</em> of an abstract pathname consists of the
    * pathname's prefix, if any, and each name in the pathname's name
    * sequence except for the last.  If the name sequence is empty then
    * the pathname does not name a parent directory.
    *
    * @return  The abstract pathname of the parent directory named by this
    *          abstract pathname, or <code>null</code> if this pathname
    *          does not name a parent
    */
   public OWFile getParentFile()
   {
      return new OWFile(fd.getOneWireContainers(), fd.getParent());
   }

   /**
    * Converts this abstract pathname into a pathname string.  The resulting
    * string uses the {@link #separator default name-separator character} to
    * separate the names in the name sequence.
    *
    * @return  The string form of this abstract pathname
    */
   public String getPath()
   {
      return fd.getPath();
   }

   //--------
   //-------- Path Operations
   //--------

   /**
    * Tests whether this abstract pathname is absolute.  The definition of
    * absolute pathname is system dependent.  On UNIX systems, a pathname is
    * absolute if its prefix is <code>"/"</code>.  On Win32 systems, a
    * pathname is absolute if its prefix is a drive specifier followed by
    * <code>"\\"</code>, or if its prefix is <code>"\\"</code>.
    *
    * @return  <code>true</code> if this abstract pathname is absolute,
    *          <code>false</code> otherwise
    */
   public boolean isAbsolute()
   {

      // always absolute
      return true;
   }

   /**
    * Returns the absolute pathname string of this abstract pathname.
    *
    * <p> If this abstract pathname is already absolute, then the pathname
    * string is simply returned as if by the <code>{@link #getPath}</code>
    * method.  If this abstract pathname is the empty abstract pathname then
    * the pathname string of the current user directory, which is named by the
    * system property <code>user.dir</code>, is returned.  Otherwise this
    * pathname is resolved in a system-dependent way.  On UNIX systems, a
    * relative pathname is made absolute by resolving it against the current
    * user directory.  On Win32 systems, a relative pathname is made absolute
    * by resolving it against the current directory of the drive named by the
    * pathname, if any; if not, it is resolved against the current user
    * directory.
    *
    * @return  The absolute pathname string denoting the same file or
    *          directory as this abstract pathname
    *
    * @see     java.io.File#isAbsolute()
    */
   public String getAbsolutePath()
   {
      return fd.getPath();
   }

   /**
    * Returns the absolute form of this abstract pathname.  Equivalent to
    * <code>new&nbsp;File(this.{@link #getAbsolutePath}())</code>.
    *
    * @return  The absolute abstract pathname denoting the same file or
    *          directory as this abstract pathname
    */
   public OWFile getAbsoluteFile()
   {
      return new OWFile(fd.getOneWireContainers(), fd.getPath());
   }

   /**
    * Returns the canonical pathname string of this abstract pathname.
    *
    * <p> The precise definition of canonical form is system-dependent, but
    * canonical forms are always absolute.  Thus if this abstract pathname is
    * relative it will be converted to absolute form as if by the <code>{@link
    * #getAbsoluteFile}</code> method.
    *
    * <p> Every pathname that denotes an existing file or directory has a
    * unique canonical form.  Every pathname that denotes a nonexistent file
    * or directory also has a unique canonical form.  The canonical form of
    * the pathname of a nonexistent file or directory may be different from
    * the canonical form of the same pathname after the file or directory is
    * created.  Similarly, the canonical form of the pathname of an existing
    * file or directory may be different from the canonical form of the same
    * pathname after the file or directory is deleted.
    *
    * @return  The canonical pathname string denoting the same file or
    *          directory as this abstract pathname
    *
    * @throws  IOException
    *          If an I/O error occurs, which is possible because the
    *          construction of the canonical pathname may require
    *          filesystem queries
    *
    * @since   JDK1.1
    */
   public String getCanonicalPath()
      throws IOException
   {
      return fd.getPath();
   }

   /**
    * Returns the canonical form of this abstract pathname.  Equivalent to
    * <code>new&nbsp;File(this.{@link #getCanonicalPath}())</code>.
    *
    * @return  The canonical pathname string denoting the same file or
    *          directory as this abstract pathname
    *
    * @throws  IOException
    *          If an I/O error occurs, which is possible because the
    *          construction of the canonical pathname may require
    *          filesystem queries
    */
   public OWFile getCanonicalFile()
      throws IOException
   {
      return new OWFile(fd.getOneWireContainers(), fd.getPath());
   }

   //--------
   //-------- Attribute 'get' Methods
   //--------

   /**
    * Tests whether the application can read the file denoted by this
    * abstract pathname.
    *
    * @return  <code>true</code> if and only if the file specified by this
    *          abstract pathname exists <em>and</em> can be read by the
    *          application; <code>false</code> otherwise
    */
   public boolean canRead()
   {
      return fd.canRead();
   }

   /**
    * Tests whether the application can modify to the file denoted by this
    * abstract pathname.
    *
    * @return  <code>true</code> if and only if the Filesystem actually
    *          contains a file denoted by this abstract pathname <em>and</em>
    *          the application is allowed to write to the file;
    *          <code>false</code> otherwise.
    *
    */
   public boolean canWrite()
   {
      return fd.canWrite();
   }

   /**
    * Tests whether the file denoted by this abstract pathname exists.
    *
    * @return  <code>true</code> if and only if the file denoted by this
    *          abstract pathname exists; <code>false</code> otherwise
    *
    */
   public boolean exists()
   {
      return fd.exists();
   }

   /**
    * Tests whether the file denoted by this abstract pathname is a
    * directory.
    *
    * @return <code>true</code> if and only if the file denoted by this
    *          abstract pathname exists <em>and</em> is a directory;
    *          <code>false</code> otherwise
    */
   public boolean isDirectory()
   {
      return fd.isDirectory();
   }

   /**
    * Tests whether the file denoted by this abstract pathname is a normal
    * file.  A file is <em>normal</em> if it is not a directory and, in
    * addition, satisfies other system-dependent criteria.  Any non-directory
    * file created by a Java application is guaranteed to be a normal file.
    *
    * @return  <code>true</code> if and only if the file denoted by this
    *          abstract pathname exists <em>and</em> is a normal file;
    *          <code>false</code> otherwise
    */
   public boolean isFile()
   {
      return fd.isFile();
   }

   /**
    * Tests whether the file named by this abstract pathname is a hidden
    * file.  The exact definition of <em>hidden</em> is system-dependent.  On
    * UNIX systems, a file is considered to be hidden if its name begins with
    * a period character (<code>'.'</code>).  On Win32 systems, a file is
    * considered to be hidden if it has been marked as such in the filesystem.
    *
    * @return  <code>true</code> if and only if the file denoted by this
    *          abstract pathname is hidden according to the conventions of the
    *          underlying platform
    */
   public boolean isHidden()
   {
      return fd.isHidden();
   }

   /**
    * Returns the time that the file denoted by this abstract pathname was
    * last modified.
    *
    * @return  A <code>long</code> value representing the time the file was
    *          last modified, measured in milliseconds since the epoch
    *          (00:00:00 GMT, January 1, 1970), or <code>0L</code> if the
    *          file does not exist or if an I/O error occurs
    */
   public long lastModified()
   {

      // not supported
      return 0;
   }

   /**
    * Returns the length of the file denoted by this abstract pathname.
    *
    * @return  The length, in bytes, of the file denoted by this abstract
    *          pathname, or <code>0L</code> if the file does not exist
    */
   public long length()
   {
      return fd.length();
   }

   //--------
   //-------- File Operation Methods
   //--------

   /**
    * Atomically creates a new, empty file named by this abstract pathname if
    * and only if a file with this name does not yet exist.  The check for the
    * existence of the file and the creation of the file if it does not exist
    * are a single operation that is atomic with respect to all other
    * filesystem activities that might affect the file.
    *
    * @return  <code>true</code> if the named file does not exist and was
    *          successfully created; <code>false</code> if the named file
    *          already exists
    *
    * @throws  IOException
    *          If an I/O error occurred
    */
   public boolean createNewFile()
      throws IOException
   {
      return fd.createNewFile();
   }

   /**
    * Deletes the file or directory denoted by this abstract pathname.  If
    * this pathname denotes a directory, then the directory must be empty in
    * order to be deleted.
    *
    * @return  <code>true</code> if and only if the file or directory is
    *          successfully deleted; <code>false</code> otherwise
    */
   public boolean delete()
   {
      return fd.delete();
   }

   /**
    * Returns an array of strings naming the files and directories in the
    * directory denoted by this abstract pathname.
    *
    * <p> If this abstract pathname does not denote a directory, then this
    * method returns <code>null</code>.  Otherwise an array of strings is
    * returned, one for each file or directory in the directory.  Names
    * denoting the directory itself and the directory's parent directory are
    * not included in the result.  Each string is a file name rather than a
    * complete path.
    *
    * <p> There is no guarantee that the name strings in the resulting array
    * will appear in any specific order; they are not, in particular,
    * guaranteed to appear in alphabetical order.
    *
    * @return  An array of strings naming the files and directories in the
    *          directory denoted by this abstract pathname.  The array will be
    *          empty if the directory is empty.  Returns <code>null</code> if
    *          this abstract pathname does not denote a directory, or if an
    *          I/O error occurs.
    */
   public String[] list()
   {
      if (isFile() ||!isDirectory())
         return null;
      else
         return fd.list();
   }

   /**
    * Returns an array of abstract pathnames denoting the files in the
    * directory denoted by this abstract pathname.
    *
    * <p> If this abstract pathname does not denote a directory, then this
    * method returns <code>null</code>.  Otherwise an array of
    * <code>OWFile</code> objects is returned, one for each file or directory in
    * the directory.  Pathnames denoting the directory itself and the
    * directory's parent directory are not included in the result.  Each
    * resulting abstract pathname is constructed from this abstract pathname
    * using the <code>{@link #OWFile(com.dalsemi.onewire.application.file.OWFile, java.lang.String)
    * OWFile(OWFile,&nbsp;String)}</code> constructor.  Therefore if this pathname
    * is absolute then each resulting pathname is absolute; if this pathname
    * is relative then each resulting pathname will be relative to the same
    * directory.
    *
    * <p> There is no guarantee that the name strings in the resulting array
    * will appear in any specific order; they are not, in particular,
    * guaranteed to appear in alphabetical order.
    *
    * @return  An array of abstract pathnames denoting the files and
    *          directories in the directory denoted by this abstract
    *          pathname.  The array will be empty if the directory is
    *          empty.  Returns <code>null</code> if this abstract pathname
    *          does not denote a directory, or if an I/O error occurs.
    */
   public OWFile[] listFiles()
   {
      if (isFile() ||!isDirectory())
         return null;
      else
      {
         String[] str_list;
         OWFile[] file_list;
         String   new_path;

         str_list  = fd.list();
         file_list = new OWFile [str_list.length];

         for (int i = 0; i < str_list.length; i++)
         {
            if ((fd.getPath() == null) || fd.getPath().endsWith("/"))
               new_path = "/" + str_list [i];
            else
               new_path = fd.getPath() + separator + str_list [i];

            file_list [i] = new OWFile(fd.getOneWireContainers(), new_path);
         }

         return file_list;
      }
   }

   /**
    * Creates the directory named by this abstract pathname.
    *
    * @return  <code>true</code> if and only if the directory was
    *          created; <code>false</code> otherwise
    */
   public boolean mkdir()
   {
      try
      {
         fd.create(false, true, false, -1, -1);

         return true;
      }
      catch (OWFileNotFoundException e)
      {
         return false;
      }
   }

   /**
    * Creates the directory named by this abstract pathname, including any
    * necessary but nonexistent parent directories.  Note that if this
    * operation fails it may have succeeded in creating some of the necessary
    * parent directories.
    *
    * @return  <code>true</code> if and only if the directory was created,
    *          along with all necessary parent directories; <code>false</code>
    *          otherwise
    */
   public boolean mkdirs()
   {
      try
      {
         fd.create(false, true, true, -1, -1);

         return true;
      }
      catch (OWFileNotFoundException e)
      {
         return false;
      }
   }

   /**
    * Renames the file denoted by this abstract pathname.
    *
    * @param  dest  The new abstract pathname for the named file
    *
    * @return  <code>true</code> if and only if the renaming succeeded;
    *          <code>false</code> otherwise
    *
    * @throws  NullPointerException
    *          If parameter <code>dest</code> is <code>null</code>
    */
   public boolean renameTo(OWFile dest)
   {
      return fd.renameTo(dest);
   }

   /**
    * Sets the last-modified time of the file or directory named by this
    * abstract pathname.
    *
    * <p> All platforms support file-modification times to the nearest second,
    * but some provide more precision.  The argument will be truncated to fit
    * the supported precision.  If the operation succeeds and no intervening
    * operations on the file take place, then the next invocation of the
    * <code>{@link #lastModified}</code> method will return the (possibly
    * truncated) <code>time</code> argument that was passed to this method.
    *
    * @param  time  The new last-modified time, measured in milliseconds since
    *               the epoch (00:00:00 GMT, January 1, 1970)
    *
    * @return <code>true</code> if and only if the operation succeeded;
    *          <code>false</code> otherwise
    *
    * @throws  IllegalArgumentException  If the argument is negative
    */
   public boolean setLastModified(long time)
   {
      return false;   // not supported
   }

   /**
    * Marks the file or directory named by this abstract pathname so that
    * only read operations are allowed.  After invoking this method the file
    * or directory is guaranteed not to change until it is either deleted or
    * marked to allow write access.  Whether or not a read-only file or
    * directory may be deleted depends upon the underlying system.
    *
    * @return <code>true</code> if and only if the operation succeeded;
    *          <code>false</code> otherwise
    */
   public boolean setReadOnly()
   {
      boolean result = fd.setReadOnly();

      return result;
   }

   //--------
   //-------- Filesystem Interface Methods
   //--------

   /**
    * List the available filesystem roots.
    *
    * <p> A particular Java platform may support zero or more
    * hierarchically-organized Filesystems.  Each Filesystem has a
    * <code>root</code> directory from which all other files in that file
    * system can be reached.  Windows platforms, for example, have a root
    * directory for each active drive; UNIX platforms have a single root
    * directory, namely <code>"/"</code>.  The set of available filesystem
    * roots is affected by various system-level operations such the insertion
    * or ejection of removable media and the disconnecting or unmounting of
    * physical or virtual disk drives.
    *
    * <p> This method returns an array of <code>OWFile</code> objects that
    * denote the root directories of the available filesystem roots.  It is
    * guaranteed that the canonical pathname of any file physically present on
    * the local machine will begin with one of the roots returned by this
    * method.
    *
    * <p> The canonical pathname of a file that resides on some other machine
    * and is accessed via a remote-filesystem protocol such as SMB or NFS may
    * or may not begin with one of the roots returned by this method.  If the
    * pathname of a remote file is syntactically indistinguishable from the
    * pathname of a local file then it will begin with one of the roots
    * returned by this method.  Thus, for example, <code>OWFile</code> objects
    * denoting the root directories of the mapped network drives of a Windows
    * platform will be returned by this method, while <code>OWFile</code>
    * objects containing UNC pathnames will not be returned by this method.
    *
    * @param owc  OneWireContainer that this Filesystem resides on
    *
    * @return  An array of <code>OWFile</code> objects denoting the available
    *          filesystem roots, or <code>null</code> if the set of roots
    *          could not be determined.  The array will be empty if there are
    *          no filesystem roots.
    */
   public static OWFile[] listRoots(OneWireContainer owc)
   {
      OWFile[] roots = new OWFile [1];

      roots [0] = new OWFile(owc, "/");

      return roots;
   }

   //--------
   //-------- Misc Methods
   //--------

   /**
    * Compares two abstract pathnames lexicographically.  The ordering
    * defined by this method depends upon the underlying system.  On UNIX
    * systems, alphabetic case is significant in comparing pathnames; on Win32
    * systems it is not.
    *
    * @param   pathname  The abstract pathname to be compared to this abstract
    *                    pathname
    *
    * @return  Zero if the argument is equal to this abstract pathname, a
    *          value less than zero if this abstract pathname is
    *          lexicographically less than the argument, or a value greater
    *          than zero if this abstract pathname is lexicographically
    *    -      greater than the argument
    */
   public int compareTo(OWFile pathname)
   {
      OneWireContainer[] owd = fd.getOneWireContainers();
      String this_path    = owd[0].getAddressAsString() + getPath();
      String compare_path =
         pathname.getOneWireContainer().getAddressAsString()
         + pathname.getPath();

      return this_path.compareTo(compare_path);
   }

   /**
    * Compares this abstract pathname to another object.  If the other object
    * is an abstract pathname, then this function behaves like <code>{@link
    * #compareTo(OWFile)}</code>.  Otherwise, it throws a
    * <code>ClassCastException</code>, since abstract pathnames can only be
    * compared to abstract pathnames.
    *
    * @param   o  The <code>Object</code> to be compared to this abstract
    *             pathname
    *
    * @return  If the argument is an abstract pathname, returns zero
    *          if the argument is equal to this abstract pathname, a value
    *          less than zero if this abstract pathname is lexicographically
    *          less than the argument, or a value greater than zero if this
    *          abstract pathname is lexicographically greater than the
    *          argument
    *
    * @throws  <code>ClassCastException</code> if the argument is not an
    *          abstract pathname
    *
    * @see     java.lang.Comparable
    */
   public int compareTo(Object o)
   {
      return compareTo((OWFile) o);
   }

   /**
    * Tests this abstract pathname for equality with the given object.
    * Returns <code>true</code> if and only if the argument is not
    * <code>null</code> and is an abstract pathname that denotes the same file
    * or directory as this abstract pathname.  Whether or not two abstract
    * pathnames are equal depends upon the underlying system.  On UNIX
    * systems, alphabetic case is significant in comparing pathnames; on Win32
    * systems it is not.
    *
    * @param   obj   The object to be compared with this abstract pathname
    *
    * @return  <code>true</code> if and only if the objects are the same;
    *          <code>false</code> otherwise
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;

      if (!(obj instanceof OWFile))
         return false;

      return (compareTo((OWFile) obj) == 0);
   }

   /**
    * Computes a hash code for this abstract pathname.  Because equality of
    * abstract pathnames is inherently system-dependent, so is the computation
    * of their hash codes.  On UNIX systems, the hash code of an abstract
    * pathname is equal to the exclusive <em>or</em> of its pathname string
    * and the decimal value <code>1234321</code>.  On Win32 systems, the hash
    * code is equal to the exclusive <em>or</em> of its pathname string,
    * convered to lower case, and the decimal value <code>1234321</code>.
    *
    * @return  A hash code for this abstract pathname
    */
   public int hashCode()
   {
      return fd.getHashCode();
   }

   /**
    * Returns the pathname string of this abstract pathname.  This is just the
    * string returned by the <code>{@link #getPath}</code> method.
    *
    * @return  The string form of this abstract pathname
    */
   public String toString()
   {
      return fd.getPath();
   }

   //--------
   //-------- Custom additions, not normally in File class
   //--------

   /**
    * Returns the <code>OWFileDescriptor</code>
    * object  that represents the connection to
    * the actual file in the Filesystem being
    * used by this <code>OWFileInputStream</code>.
    *
    * @return     the file descriptor object associated with this File.
    * @exception  IOException  if an I/O error occurs.
    * @see        com.dalsemi.onewire.application.file.OWFileDescriptor
    */
   public OWFileDescriptor getFD()
      throws IOException
   {
      return fd;
   }

   /**
    * Gets the OneWireContainer that this File resides on.  This
    * is where the 'filesystem' resides.  If this Filesystem
    * spans multiple devices then this method returns the
    * 'MASTER' device.
    *
    * @return     the OneWireContainer for this Filesystem
    */
   public OneWireContainer getOneWireContainer()
   {
      OneWireContainer[] owd = fd.getOneWireContainers();
      return owd[0];
   }

   /**
    * Gets the OneWireContainer(s) that this File resides on.  This
    * is where the 'filesystem' resides.  The first device
    * is the 'MASTER' device and the other devices are 'SATELLITE'
    * devices.
    *
    * @return     the OneWireContainer(s) for this Filesystem
    */
   public OneWireContainer[] getOneWireContainers()
   {
      return fd.getOneWireContainers();
   }

   /**
    * Format the Filesystem on the 1-Wire device provided in
    * the constructor.  This operation is required before any
    * file IO is possible. <P>
    * <b>WARNING</b> this will remove any files/directories.
    * <P>
    * @exception  IOException  if an I/O error occurs.
    */
   public void format()
      throws IOException
   {
      try
      {
         fd.format();
      }
      catch (OneWireException e)
      {
         throw new IOException(e.toString());
      }
   }

   /**
    * Gets the number of bytes available on this device for
    * file and directory information.
    *
    * @return     number of free bytes in the Filesystem
    *
    * @exception  IOException  if an I/O error occurs
    */
   public int getFreeMemory()
      throws IOException
   {
      try
      {
         return fd.getFreeMemory();
      }
      catch (OneWireException e)
      {
         throw new IOException(e.toString());
      }
   }

   /**
    * Closes this file and releases any system resources
    * associated with this stream. This file may no longer
    * be used after this operation.
    *
    * @exception  IOException  if an I/O error occurs.
    */
   public void close()
      throws IOException
   {
      fd.close();

      fd = null;
   }

   /**
    * Get's an array of integers that represents the page
    * list of the file or directory represented by this
    * OWFile.
    *
    * @return     node page list file or directory
    *
    * @exception  IOException  if an I/O error occurs.
    */
   public int[] getPageList()
      throws IOException
   {
      if (fd != null)
      {
         if (!fd.exists())
            return new int[0];
      }
      else
         return new int[0];

      try
      {
         return fd.getPageList();
      }
      catch (OneWireException e)
      {
         throw new IOException(e.toString());
      }
   }

   /**
    * Returns an integer which represents the starting memory page
    * of the file or directory represented by this OWFile.
    *
    * @return The starting page of the file or directory.
    *
    * @exception IOException if the file doesn't exist
    */
   public int getStartPage()
      throws IOException
   {
      if(fd != null && fd.exists())
      {
         return fd.getStartPage();
      }
      else
      {
         throw new FileNotFoundException();
      }
   }

   /**
    * Get's the memory bank object for the specified page.
    * This is significant if the Filesystem spans memory banks
    * on the same or different devices.
    *
    * @return   PagedMemoryBank for the specified page
    */
   public PagedMemoryBank getMemoryBankForPage(int page)
   {
      if (fd != null)
      {
         if (!fd.exists())
            return null;
      }
      else
         return null;

      return fd.getMemoryBankForPage(page);
   }

   /**
    * Get's the local page number on the memory bank object for
    * the specified page.
    * This is significant if the Filesystem spans memory banks
    * on the same or different devices.
    *
    * @return  local page for the specified Filesystem page
    *          (memory bank specific)
    */
   public int getLocalPage(int page)
   {
      if (fd != null)
      {
         if (!fd.exists())
            return 0;
      }
      else
         return 0;

      return fd.getLocalPage(page);
   }

   /**
    * Cleans up the connection to the file, and ensures that the
    * <code>close</code> method of this file output stream is
    * called when there are no more references to this stream.
    *
    * @exception  IOException  if an I/O error occurs.
    * @see        java.io.FileInputStream#close()
    */
   protected void finalize()
      throws IOException
   {
      if (fd != null)
         fd.close();
   }
}
