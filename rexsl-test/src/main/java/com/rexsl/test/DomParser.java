/**
 * Copyright (c) 2011-2013, ReXSL.com
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

import com.jcabi.log.Logger;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.sourceforge.reb4j.Adopted;
import net.sourceforge.reb4j.Entity;
import net.sourceforge.reb4j.Group;
import net.sourceforge.reb4j.Literal;
import net.sourceforge.reb4j.Sequence;
import net.sourceforge.reb4j.charclass.CharClass;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.w3c.dom.Document;

/**
 * Convenient parser of XML to DOM.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode(of = "xml")
final class DomParser {

    /**
     * Pattern to detect if passed txt looks like xml.
     */
    private static final Pattern PATTERN = DomParser.buildPattern();

    /**
     * The XML as a text.
     */
    private final transient String xml;

    /**
     * Public ctor.
     *
     * <p>An {@link IllegalArgumentException} may be thrown if the parameter
     * passed is not in XML format. It doesn't perform a strict validation
     * and is not guaranteed that an exception will be thrown whenever
     * the parameter is not XML.
     *
     * @param txt The XML in text
     */
    public DomParser(@NotNull final String txt) {
        if (txt.isEmpty()) {
            throw new IllegalArgumentException("Empty document, not an XML");
        }
        if (!DomParser.PATTERN.matcher(txt.replaceAll("\\s", "")).matches()) {
            throw new IllegalArgumentException(
                Logger.format("Doesn't look like XML: '%s'", txt)
            );
        }
        this.xml = txt;
    }

    /**
     * Get document of body.
     * @return The document
     */
    @NotNull
    public Document document() {
        Document doc;
        try {
            final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            // @checkstyle LineLength (1 line)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setNamespaceAware(true);
            doc = factory
                .newDocumentBuilder()
                .parse(IOUtils.toInputStream(this.xml, CharEncoding.UTF_8));
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (org.xml.sax.SAXException ex) {
            throw new IllegalArgumentException(
                Logger.format("Invalid XML: \"%s\"", this.xml),
                ex
            );
        }
        return doc;
    }

    /**
     * Pattern initialization method.
     * @return The pattern that matches any valid XML document
     */
    private static Pattern buildPattern() {
        final CharClass start = CharClass.characters(':', '_')
            .union(CharClass.range('a', 'z'))
            .union(CharClass.range('A', 'Z'))
            .union(CharClass.range('\u00C0', '\u00D6'))
            .union(CharClass.range('\u00D8', '\u00F6'))
            .union(CharClass.range('\u00F8', '\u02FF'))
            .union(CharClass.range('\u0370', '\u037D'))
            .union(CharClass.range('\u037F', '\u1FFF'))
            .union(CharClass.range('\u200C', '\u200D'))
            .union(CharClass.range('\u2070', '\u218F'))
            .union(CharClass.range('\u2C00', '\u2FEF'))
            .union(CharClass.range('\u3001', '\uD7FF'))
            .union(CharClass.range('\uF900', '\uFDCF'))
            .union(CharClass.range('\uFDF0', '\uFFFD'));
        final CharClass letter = CharClass.characters('-', '.', '\u00B7')
            .union(CharClass.range('0', '9'))
            .union(CharClass.range('\u0300', '\u036F'))
            .union(CharClass.range('\u203F', '\u2040'));
        final Sequence element = start
            .andThen(Group.nonCapturing(letter).anyTimes());
        return Sequence.sequence(
            Group.nonCapturing(
                Adopted.fromPattern(Pattern.compile("<\\?xml.*\\?>\\s*"))
            ).optional(),
            Group.nonCapturing(
                Adopted.fromPattern(Pattern.compile("<!DOCTYPE.*>"))
            ).optional(),
            Group.nonCapturing(
                Adopted.fromPattern(Pattern.compile("<!--.*-->"))
            ).optional(),
            Literal.literal('<'),
            element,
            Entity.ANY_CHAR.anyTimes(),
            Group.nonCapturing(element.or(CharClass.character('/'))),
            Literal.literal('>')
        ).toPattern();
    }
}
