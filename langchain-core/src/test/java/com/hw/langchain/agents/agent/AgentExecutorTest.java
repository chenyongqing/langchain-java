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

package com.hw.langchain.agents.agent;

import static com.hw.langchain.agents.initialize.Initialize.initializeAgent;
import static com.hw.langchain.agents.load.tools.LoadTools.loadTools;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hw.langchain.agents.agent.types.AgentType;
import com.hw.langchain.base.language.BaseLanguageModel;
import com.hw.langchain.chat.models.openai.ChatOpenAI;
import com.hw.langchain.llms.base.BaseLLM;
import com.hw.langchain.llms.openai.OpenAI;
import com.hw.langchain.llms.openai.OpenAIChat;
import com.hw.langchain.memory.buffer.ConversationBufferMemory;
import com.hw.langchain.tools.base.BaseTool;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HamaWhite
 */
@Disabled("Test requires costly OpenAI calls, can be run manually.")
class AgentExecutorTest {

    private static final Logger LOG = LoggerFactory.getLogger(AgentExecutorTest.class);
    private static final String OPENAI_API_KEY = "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";
    private static final String SERPAPI_API_KEY = "18401c486d0920d206d7abd8dc51a2d7e6047b65341ece53cefa6960870eee7d";

    private static final Map<String, Object> kwargs = Map.of("serpapi_api_key",
        "18401c486d0920d206d7abd8dc51a2d7e6047b65341ece53cefa6960870eee7d");

    // before初始化llm  和 tools

    private static BaseLLM llm;
    private static BaseLanguageModel chat;

    private static List<BaseTool> tools;

    @BeforeAll
    public static void setup() {
        // First, let's load the language model we're going to use to control the agent.
        llm = OpenAIChat.builder()
            .openaiApiKey(OPENAI_API_KEY)
            .temperature(0).build().init();

        chat = ChatOpenAI.builder()
            .openaiApiKey(OPENAI_API_KEY)
            .temperature(0).build().init();

        // Next, let's load some tools to use. Note that the `llm-math` tool uses an LLM, so we need to pass that in.
        tools = loadTools(List.of("serpapi", "llm-math"), llm, kwargs);
    }

    @Test
    void testAgentWithLLM() {
        // First, let's load the language model we're going to use to control the agent.
//        var llm = OpenAI.builder()
//            .openaiApiKey(OPENAI_API_KEY)
//            .temperature(0).build().init();
//
//        // Next, let's load some tools to use. Note that the `llm-math` tool uses an LLM, so we need to pass that in.
//        var tools = loadTools(List.of("serpapi", "llm-math"), llm, kwargs);

        var memory = new ConversationBufferMemory(true);

        // Finally, let's initialize an agent with the tools, the language model, and the type of agent we want to use.
        var agent = initializeAgent(tools, llm, AgentType.ZERO_SHOT_REACT_DESCRIPTION);

        // Now let's test it out!
        String actual = agent.run(
            "What was the high temperature in SF yesterday in Fahrenheit?");
        LOG.info("actual: \n{}", actual);


        String actua2 = agent.run(
            "What is that number raised to the .023 power?");
        LOG.info("actual: \n{}", actua2);

//        assertTrue(actual.matches("^1\\.\\d+$"));
    }

    @Test
    void testAgentWithChatModel() {
        // First, let's load the language model we're going to use to control the agent.
//        var chat = ChatOpenAI.builder().temperature(0).build().init();
//
//        // Next, let's load some tools to use. Note that the `llm-math` tool uses an LLM, so we need to pass that in.
//        var llm = OpenAI.builder().temperature(0).build().init();
//        var tools = loadTools(List.of("serpapi", "llm-math"), llm);

        // Finally, let's initialize an agent with the tools, the language model, and the type of agent we want to use.
        var agent = initializeAgent(tools, chat, AgentType.CHAT_ZERO_SHOT_REACT_DESCRIPTION);

        /*
         * Now let's test it out!,
         *
         * The correct return result for the final step is similar to the following: "The answer to the second question
         * is 2.42427848557. Final Answer: Jason Sudeikis, and his age raised to the 0.23 power is 2.42427848557."
         *
         * However, sometimes OpenAI only returns "I now know the answer to the second part of the question." without
         * including "Final Answer: xxx", which can cause parsing errors in the results.
         *
         * My temperature setting is 0, so this issue should not occur. If you know the answer, please feel free to let
         * me know.
         */

        String result = agent.run("Who is Olivia Wilde's boyfriend? What is his current age raised to the 0.23 power?");

        // Jason Sudeikis, and his age raised to the 0.23 power is 2.42427848557.
        LOG.info("result: \n{}", result);
        assertNotNull(result, "result should not be null");
    }
}
