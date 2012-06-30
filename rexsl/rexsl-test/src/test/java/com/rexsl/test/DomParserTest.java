/**
 * Copyright (c) 2011-2012, ReXSL.com
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
package com.rexsl.test;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link DomParser}.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class DomParserTest {

    /**
     * DomParser can parse XML text properly.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void parsesIncomingXmlDocument() throws Exception {
        final String xml = "<a><b>\u0443\u0440\u0430!</b></a>";
        final DomParser parser = new DomParser(xml);
        MatcherAssert.assertThat(
            parser.document(),
            XhtmlMatchers.hasXPath("/a/b")
        );
    }

    /**
     * DomParser throws exception when XML is invalid.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void throwsWhenInvalidXml() throws Exception {
        final String xml = "<a><!-- broken XML -->";
        try {
            new DomParser(xml).document();
            Assert.fail("exception expected");
        } catch (IllegalArgumentException ex) {
            MatcherAssert.assertThat(
                ex.getMessage(),
                Matchers.containsString(xml)
            );
        }
    }

    /**
     * DomParser can parse XML text properly.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void parsesIncomingXmlDocumentComment() throws Exception {
        final String xml = "<?xml version='1.0'?><!-- test --><root/>";
        final DomParser parser = new DomParser(xml);
        MatcherAssert.assertThat(
            parser.document(),
            XhtmlMatchers.hasXPath("/root")
        );
    }

    /**
     * DomParser throws exception when it is not an XML at all.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenNonXmlDocument() throws Exception {
        new DomParser("some text, which is not an XML");
    }

    /**
     * DomParser throws exception when it is NULL.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void throwsWhenNull() throws Exception {
        new DomParser(null);
    }

    /**
     * DomParser throws exception when input txt is not well-formed XML.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenInvalidElement() throws Exception {
        new DomParser("<123/>");
    }

    /**
     * DomParser allows XML documents with prolog.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void allowsProlog() throws Exception {
        new DomParser("<?xml version=\"1.0\"?><a/>");
    }

    /**
     * DomParser allows XML documents with prolog.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void allowsStartCharacter() throws Exception {
        new DomParser("<:a/>");
        new DomParser("<_a/>");
        new DomParser("<\u00c0a/>");
    }
}
