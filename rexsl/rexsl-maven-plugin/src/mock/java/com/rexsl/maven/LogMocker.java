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
package com.rexsl.maven;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Mocker of Maven log.
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
public final class LogMocker {

    /**
     * The mock.
     */
    private final transient Log log = Mockito.mock(Log.class);

    /**
     * Lines recorded.
     */
    private final transient List<String> messages = new LinkedList<String>();

    /**
     * Public ctor.
     */
    public LogMocker() {
        final Answer<?> rec = new Answer<Object>() {
            public Object answer(final InvocationOnMock invocation) {
                final String text = (String) invocation.getArguments()[0];
                LogMocker.this.messages.add(text);
                return null;
            }
        };
        Mockito.doAnswer(rec).when(this.log).info(Mockito.anyString());
        Mockito.doAnswer(rec).when(this.log).error(Mockito.anyString());
        Mockito.doAnswer(rec).when(this.log).debug(Mockito.anyString());
        Mockito.doAnswer(rec).when(this.log).warn(Mockito.anyString());
    }

    /**
     * Mock the log.
     * @return The log
     */
    public Log mock() {
        StaticLoggerBinder.getSingleton().setMavenLog(this.log);
        return this.log;
    }

    /**
     * Get messages recorded.
     * @return The messages
     */
    public List<String> getMessages() {
        return Collections.unmodifiableList(this.messages);
    }

}
