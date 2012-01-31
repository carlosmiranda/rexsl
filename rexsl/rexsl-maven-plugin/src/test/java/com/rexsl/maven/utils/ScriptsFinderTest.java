/**
 * Copyright (c) 2011, ReXSL.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the ReXSL.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rexsl.maven.utils;

import com.rexsl.maven.Environment;
import com.rexsl.maven.EnvironmentMocker;
import java.io.File;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * ScriptsFinder test.
 * @author Dmitry Bashkin (dmitry.bashkin@rexsl.com)
 * @version $Id: ScriptsFinderTest.java 570 2011-12-25 16:32:21Z guard $
 */
public final class ScriptsFinderTest {

    /**
     * Test string.
     */
    private static final String TEST = "assert true";

    /**
     * Checks alphabetical order.
     * @throws Exception .
     */
    @Test
    public void testOrdered() throws Exception {
        final Environment environment = new EnvironmentMocker()
            .withTextFile("src/test/rexsl/setup/g.groovy", this.TEST)
            .withTextFile("src/test/rexsl/setup/f.groovy", this.TEST)
            .withTextFile("src/test/rexsl/setup/a.groovy", this.TEST)
            .mock();
        final File directory = environment.basedir();
        final ScriptsFinder finder = new ScriptsFinder(directory);
        final Collection<File> files = finder.ordered();
        String previous = "";
        for (File file : files) {
            final String name = file.getName();
            final int result = name.compareTo(previous);
            MatcherAssert.assertThat(
                "order",
                result,
                Matchers.greaterThanOrEqualTo(0)
            );
            previous = name;
        }
    }
}