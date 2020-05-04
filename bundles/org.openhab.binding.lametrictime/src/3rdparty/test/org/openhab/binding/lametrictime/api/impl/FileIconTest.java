/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.lametrictime.api.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openhab.binding.lametrictime.api.test.AbstractTest;

public class FileIconTest extends AbstractTest
{
    @Test
    public void testLocalPathGif()
    {
        FileIcon icon = new FileIcon(getTestDataPath("smile.gif"));
        assertEquals("data:image/gif;base64,R0lGODlhCAAIAPEAAPz+BPz+/AAAAAAAACH5BAkKAAIAIf8LTkVUU0NBUEUyLjADAQAAACwAAAAACAAIAAACEZSAYJfIElREIdaGs3PPNFMAACH5BAkKAAIALAAAAAAIAAgAAAIRlIBgl8gSVEQh1oazU4szJxQAIfkECTIAAgAsAAAAAAgACAAAAhKUgGCXyBJURCHWhlU7fCmzCQUAIfkECRQAAgAsAAAAAAgACAAAAhGUgGCXyBIaClFa1Y5eymRRAAAh+QQJMgACACwAAAAACAAIAAACEpSAYJfIElREIdaGVTt8KbMJBQA7",
                     icon.toRaw());
    }

    @Test
    public void testLocalPathPng()
    {
        FileIcon icon = new FileIcon(getTestDataPath("info.png"));
        assertEquals("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAICAYAAADED76LAAAAL0lEQVQYlWN0NPv3nwEPYIEx9p1kRJFwMofoY0IXQGczMRAAVFSA7EhkNiMhbwIAA/sN+bH1CpgAAAAASUVORK5CYII=",
                     icon.toRaw());
    }

    @Test
    public void testLocalFileGif()
    {
        FileIcon icon = new FileIcon(getTestDataFile("smile.gif"));
        assertEquals("data:image/gif;base64,R0lGODlhCAAIAPEAAPz+BPz+/AAAAAAAACH5BAkKAAIAIf8LTkVUU0NBUEUyLjADAQAAACwAAAAACAAIAAACEZSAYJfIElREIdaGs3PPNFMAACH5BAkKAAIALAAAAAAIAAgAAAIRlIBgl8gSVEQh1oazU4szJxQAIfkECTIAAgAsAAAAAAgACAAAAhKUgGCXyBJURCHWhlU7fCmzCQUAIfkECRQAAgAsAAAAAAgACAAAAhGUgGCXyBIaClFa1Y5eymRRAAAh+QQJMgACACwAAAAACAAIAAACEpSAYJfIElREIdaGVTt8KbMJBQA7",
                     icon.toRaw());
    }

    @Test
    public void testLocalFilePng()
    {
        FileIcon icon = new FileIcon(getTestDataFile("info.png"));
        assertEquals("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAICAYAAADED76LAAAAL0lEQVQYlWN0NPv3nwEPYIEx9p1kRJFwMofoY0IXQGczMRAAVFSA7EhkNiMhbwIAA/sN+bH1CpgAAAAASUVORK5CYII=",
                     icon.toRaw());
    }
}
