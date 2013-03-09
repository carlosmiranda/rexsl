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
package com.rexsl.core;

import com.jcabi.log.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Makes a decision whether page should be transformed to HTML or returned
 * to the user as untouched XML (or anything else).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@ToString
@EqualsAndHashCode(of = { "page", "request" })
final class PageAnalyzer {

    /**
     * The page.
     */
    private final transient String page;

    /**
     * The request.
     */
    private final transient HttpServletRequest request;

    /**
     * Public ctor.
     * @param text The text of response
     * @param rqst The request
     */
    public PageAnalyzer(final String text, final HttpServletRequest rqst) {
        this.page = text;
        this.request = rqst;
    }

    /**
     * Do we need filtering?
     * @return Do we need to transform to XHTML?
     */
    public boolean needsTransformation() {
        final UserAgent agent = new UserAgent(
            this.request.getHeader(HttpHeaders.USER_AGENT)
        );
        final TypesMatcher accept = new TypesMatcher(
            this.request.getHeader(HttpHeaders.ACCEPT)
        );
        // @checkstyle BooleanExpressionComplexity (1 line)
        final boolean dontTouch =
            this.page.isEmpty()
            || !this.page.startsWith("<?xml ")
            || !this.page.contains("<?xml-stylesheet ")
            || accept.explicit(MediaType.APPLICATION_XML)
            || (
                agent.isXsltCapable()
                && accept.accepts(MediaType.APPLICATION_XML)
            );
        if (dontTouch) {
            Logger.debug(
                this,
                // @checkstyle LineLength (1 line)
                "#needsTransformation('%s': %d chars): User-Agent='%s', Accept='%s', no need to transform",
                this.request.getRequestURI(),
                this.page.length(),
                agent,
                accept
            );
        } else {
            Logger.debug(
                this,
                // @checkstyle LineLength (1 line)
                "#filtneedsTransformationer('%s': %d chars): User-Agent='%s', Accept='%s', transformation required",
                this.request.getRequestURI(),
                this.page.length(),
                agent,
                accept
            );
        }
        return !dontTouch;
    }

}