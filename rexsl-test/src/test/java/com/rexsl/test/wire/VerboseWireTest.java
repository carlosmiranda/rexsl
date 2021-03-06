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
package com.rexsl.test.wire;

import com.rexsl.test.mock.MkAnswer;
import com.rexsl.test.mock.MkContainer;
import com.rexsl.test.mock.MkGrizzlyContainer;
import com.rexsl.test.request.JdkRequest;
import com.rexsl.test.response.RestResponse;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Test;

/**
 * Test case for {@link VerboseWire}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class VerboseWireTest {

    /**
     * VerboseWire can log requests.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void logsRequest() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .header(HttpHeaders.USER_AGENT, "it's me")
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
    }

}
