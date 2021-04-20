/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.rendering.internal.block.diff;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for {@link DefaultBlockDiffMarker}.
 * 
 * @version $Id$
 * @since 13.3RC1
 */
@ComponentTest
@AllComponents
class DefaultBlockDiffMarkerTest
{
    @InjectMockComponents
    private DefaultBlockDiffMarker diffMarker;

    private Parser parser;

    private BlockRenderer xhtmlRenderer;

    @BeforeEach
    void configure(MockitoComponentManager componentManager) throws Exception
    {
        this.parser = componentManager.getInstance(Parser.class, "xwiki/2.1");
        this.xhtmlRenderer = componentManager.getInstance(BlockRenderer.class, "xhtml/1.0");
    }

    @ParameterizedTest
    @MethodSource("getTestFiles")
    void markDiff(File testFile) throws Exception
    {
        Map<String, String> testData = getTestData(testFile);
        if (testData.containsKey("expected-marker")) {
            XDOM left = this.parser.parse(new StringReader(testData.get("left")));
            XDOM right = this.parser.parse(new StringReader(testData.get("right")));
            String noChangeHTML = toHTML(left);
            boolean changed = this.diffMarker.markDiff(left, right);
            String actualHTML = toHTML(left);
            assertEquals(changed, !noChangeHTML.equals(actualHTML));
            String expectedHTML = testData.get("expected-marker");
            assertEquals(expectedHTML, actualHTML);
        }
    }

    String toHTML(XDOM xdom)
    {
        WikiPrinter printer = new DefaultWikiPrinter();
        this.xhtmlRenderer.render(xdom, printer);
        return printer.toString();
    }

    static Stream<File> getTestFiles() throws Exception
    {
        File inputFolder = new File("src/test/resources");
        return Stream.of(inputFolder.listFiles(file -> file.getName().endsWith(".test")));
    }

    Map<String, String> getTestData(File file) throws Exception
    {
        Map<String, String> testData = new HashMap<>();
        List<String> lines = IOUtils.readLines(new FileReader(file));
        String key = null;
        StringBuilder data = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("## ")) {
                // Store previous data.
                if (key != null) {
                    testData.put(key, data.toString());
                }
                // Prepare next data.
                key = line.substring(3);
                data.delete(0, data.length());
            } else if (!line.startsWith("##-")) {
                if (key != null && key.startsWith("expected-")) {
                    // Remove whitespace at the start of the line (in order to ignore formatting).
                    line = StringUtils.stripStart(line, null);
                    // Remove line ending (in order to ignore formatting).
                    line = StringUtils.chomp(line);
                }
                data.append(line);
            }
        }
        if (key != null) {
            testData.put(key, data.toString());
        }
        return testData;
    }
}
