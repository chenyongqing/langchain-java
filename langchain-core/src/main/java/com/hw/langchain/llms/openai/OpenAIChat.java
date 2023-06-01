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

package com.hw.langchain.llms.openai;

import com.hw.langchain.llms.base.BaseLLM;
import com.hw.langchain.schema.BaseMessage;
import com.hw.langchain.schema.Generation;
import com.hw.langchain.schema.LLMResult;
import com.hw.langchain.schema.PromptValue;
import com.hw.langchain.utils.Utils;
import com.hw.openai.OpenAiClient;
import com.hw.openai.entity.chat.ChatCompletion;
import com.hw.openai.entity.chat.ChatCompletionResp;
import com.hw.openai.entity.chat.Message;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.net.Proxy;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @description: Wrapper around OpenAI Chat large language models.
 * @author: HamaWhite
 */
@SuperBuilder
public class OpenAIChat extends BaseLLM {

    protected Object client;

    /**
     * Model name to use.
     */
    @Builder.Default
    protected String modelName = "gpt-3.5-turbo";

    /**
     * What sampling temperature to use.
     */
    @Builder.Default
    protected float temperature = 0.7f;

    /**
     * The maximum number of tokens to generate in the completion.
     * -1 returns as many tokens as possible given the prompt and the model's maximal context size.
     */
    @Builder.Default
    protected int maxTokens = 256;

    /**
     * Total probability mass of tokens to consider at each step.
     */
    @Builder.Default
    protected float topP = 1f;

    /**
     * Penalizes repeated tokens according to frequency.
     */
    protected float frequencyPenalty;

    /**
     * Penalizes repeated tokens.
     */
    protected float presencePenalty;

    /**
     * How many completions to generate for each prompt.
     */
    @Builder.Default
    protected int n = 1;

    /**
     * API key for OpenAI.
     */
    protected String openaiApiKey;

    /**
     * Base URL for OpenAI API.
     */
    protected String openaiApiBase;

    /**
     * Organization ID for OpenAI.
     */
    protected String openaiOrganization;

    /**
     * Support explicit proxy for OpenAI
     */
    protected Proxy openaiProxy;

    /**
     * Maximum number of retries to make when generating.
     */
    @Builder.Default
    protected int maxRetries = 6;

    /**
     * Series of messages for Chat input.
     */
    @Builder.Default
    private List<Message> prefixMessages = new ArrayList<>();

    /**
     * Adjust the probability of specific tokens being generated.
     */
    protected Map<String, Float> logitBias;

    /**
     * Whether to stream the results or not.
     */
    protected boolean streaming;

    public OpenAIChat init() {
        openaiApiBase = Utils.getOrEnvOrDefault(openaiApiBase, "OPENAI_API_BASE", "");
        openaiApiKey = Utils.getOrEnvOrDefault(openaiApiKey, "OPENAI_API_KEY");
        openaiOrganization = Utils.getOrEnvOrDefault(openaiOrganization, "OPENAI_ORGANIZATION", "");

        this.client = OpenAiClient.builder()
                .openaiApiKey(openaiApiKey)
                .openaiOrganization(openaiOrganization)
                .proxy(openaiProxy)
                .build()
                .init();
        return this;
    }

    @Override
    public LLMResult generatePrompt(List<PromptValue> prompts, List<String> stop) {
        return null;
    }

    @Override
    public String predict(String text, List<String> stop) {
        return null;
    }

    @Override
    public BaseMessage predictMessages(List<BaseMessage> messages, List<String> stop) {
        return null;
    }

    @Override
    public String llmType() {
        return "openai-chat";
    }

    @Override
    protected LLMResult _generate(List<String> prompts, List<String> stop) {
        List<Message> messages = getChatMessages(prompts);

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(modelName)
                .temperature(temperature)
                .messages(messages)
                .maxTokens(maxTokens)
                .topP(topP)
                .frequencyPenalty(frequencyPenalty)
                .presencePenalty(presencePenalty)
                .n(n)
                .logitBias(logitBias)
                .build();

        ChatCompletionResp response = ((OpenAiClient) client).create(chatCompletion);

        List<List<Generation>> generations = new ArrayList<>();
        Generation generation = Generation.builder()
                .text(response.getChoices().get(0).getMessage().getContent())
                .build();

        generations.add(List.of(generation));

        Map<String, Object> llmOutput = new HashMap<>(2);
        llmOutput.put("tokenUsage", response.getUsage());
        llmOutput.put("modelName", modelName);

        return new LLMResult(generations, llmOutput);
    }

    private List<Message> getChatMessages(List<String> prompts) {
        checkArgument(prompts.size() == 1, "OpenAIChat currently only supports single prompt, got %s", prompts);
        List<Message> messages = new ArrayList<>(prefixMessages);
        messages.add(Message.of(prompts.get(0)));
        return messages;
    }
}
