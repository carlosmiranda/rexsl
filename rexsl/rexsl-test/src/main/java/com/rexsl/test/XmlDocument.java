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

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Node;

/**
 * XML document.
 *
 * <p>Set of convenient XML manipulations:
 *
 * <pre> XmlDocument xml = new SimpleXml(content);
 * for (XmlDocument employee : xml.nodes("//Employee")) {
 *   String name = employee.xpath("name/text()").get(0);
 *   // ...
 * }</pre>
 *
 * <p>You can always get DOM node out of this abstraction using {@link #node()}
 * method.
 *
 * <p>{@code toString()} must produce a full XML.
 *
 * <p>Implementation of this interface must be immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @since 0.3.7
 * @see SimpleXml
 */
public interface XmlDocument {

    /**
     * Find and return text elements or attributes matched by XPath address.
     *
     * <p>The XPath query should point to text elements or attributes in the
     * XML document. If any nodes of different types (elements, comments, etc.)
     * are found in result node list - an {@link AssertionError} will be thrown.
     *
     * <p>This is a convenient method, which is used (according to our
     * experience) in 95% of all cases. Usually you don't need to get anything
     * else but a text value of some node or an attribute. And in most cases
     * you are interested to get just the first value
     * (use {@code xpath(..).get(0)}). But when/if you need to get more than
     * just a plain text - use {@link #nodes(String)}.
     *
     * <p>The {@link List} returned will throw {@link IndexOutOfBoundsException}
     * if you try to access a node which wasn't found by this XPath query.
     *
     * <p>An {@link IllegalArgumentException} is thrown if the parameter
     * passed is not a valid XPath expression.
     *
     * @param query The XPath query
     * @return The list of string values (texts)
     */
    List<String> xpath(@NotNull String query);

    /**
     * Retrieve DOM nodes from the XML response.
     *
     * <p>The {@link List} returned will throw {@link IndexOutOfBoundsException}
     * if you try to access a node which wasn't found by this XPath query.
     *
     * <p>An {@link IllegalArgumentException} is thrown if the parameter
     * passed is not a valid XPath expression.
     *
     * @param query The XPath query
     * @return Collection of DOM nodes
     */
    List<XmlDocument> nodes(@NotNull String query);

    /**
     * Register additional namespace prefix for XPath.
     *
     * <p>For example:
     *
     * <pre>
     * String name = RestTester.start(new URI("http://www.example.com"))
     *   .registerNs("ns1", "http://example.com")
     *   .registerNs("foo", "http://example.com/foo")
     *   .xpath("/ns1:root/foo:name/text()")
     *   .get(0);
     * </pre>
     *
     * <p>A number of standard namespaces are registered by default in
     * instances of {@link XmlDocument} returned by {@link RestTester}. Their
     * full list is in {@link SimpleXml#SimpleXml(String)}.
     *
     * <p>If a namespace prefix is already registered an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param prefix The XPath prefix to register
     * @param uri Namespace URI
     * @return A new XML document, with this additional namespace registered
     */
    XmlDocument registerNs(@NotNull String prefix, @NotNull Object uri);

    /**
     * Append this namespace context to the existing one.
     *
     * <p>The existing context (inside this object) and the new one provided
     * will be merged together. The existing context will be have higher
     * priority.
     *
     * @param context The context to append
     * @return A new XML document, with a merged context on board
     */
    XmlDocument merge(@NotNull NamespaceContext context);

    /**
     * Retrieve DOM node, represented by this wrapper.
     * @return DOM node
     * @since 0.3.8
     */
    Node node();

}
