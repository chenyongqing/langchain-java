/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hw.langchain.chains.api.base;

import com.hw.langchain.chat.models.openai.ChatOpenAI;
import com.hw.langchain.llms.openai.OpenAI;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hw.langchain.chains.api.meteo.OpenMeteoDocs.OPEN_METEO_DOCS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <a href="https://python.langchain.com/docs/modules/chains/popular/api">API chains</a>
 *
 * @author HamaWhite
 */
@Disabled("Test requires costly OpenAI calls, can be run manually.")
class ApiChainTest {

    private static final Logger LOG = LoggerFactory.getLogger(ApiChainTest.class);

    private static final String OPENAI_API_KEY = "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";
    private static final String SERPAPI_API_KEY = "18401c486d0920d206d7abd8dc51a2d7e6047b65341ece53cefa6960870eee7d";

    @Test
    void testApiChain() {
        var llm = OpenAI.builder().openaiApiKey(OPENAI_API_KEY).temperature(0).build().init();


        var chat = ChatOpenAI.builder().openaiApiKey(OPENAI_API_KEY).temperature(0).build().init();
        var chain = ApiChain.fromLlmAndApiDocs(chat, OPEN_METEO_DOCS);
        var result = chain.run("What is the weather like right now in Hangzhou, China in degrees Fahrenheit?");

        // The current temperature in Hangzhou, China is 79.8°F with a windSpeed of 7.4 km/h and a wind direction of
        // 133°.
        LOG.info("result: \n{}", result);
        assertNotNull(result, "result should not be null");
    }

}