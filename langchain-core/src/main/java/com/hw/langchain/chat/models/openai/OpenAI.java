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

package com.hw.langchain.chat.models.openai;

import com.alibaba.fastjson.JSONObject;
import com.hw.langchain.schema.AIMessage;
import com.hw.langchain.schema.BaseMessage;
import com.hw.langchain.schema.ChatFunctionMessage;
import com.hw.langchain.schema.ChatMessage;
import com.hw.langchain.schema.FunctionMessage;
import com.hw.langchain.schema.HumanMessage;
import com.hw.langchain.schema.SystemMessage;
import com.hw.openai.entity.chat.ChatChoice;
import com.hw.openai.entity.chat.FunctionCallResult;
import com.hw.openai.entity.chat.Message;
import com.hw.openai.entity.chat.Role;
import org.apache.commons.lang3.StringUtils;

/**
 * @author HamaWhite
 */
public class OpenAI {

    private OpenAI() {
    }

    public static Message convertLangChainToOpenAI(BaseMessage message) {
        if (message instanceof ChatMessage chatMessage) {
            return Message.of(chatMessage.getRole(), message.getContent());
        } else if (message instanceof HumanMessage) {
            return Message.of(message.getContent());
        } else if (message instanceof AIMessage aiMessage) {
            String functionCall = aiMessage.getFunctionCall();
            if (StringUtils.isNotBlank(functionCall)) {
                FunctionCallResult functionCallResult = JSONObject.parseObject(functionCall, FunctionCallResult.class);
                return Message.ofFunctionCall(functionCallResult);
            }
            return Message.ofAssistant(message.getContent());
        } else if (message instanceof SystemMessage) {
            return Message.ofSystem(message.getContent());
        } else if (message instanceof FunctionMessage functionMessage) {
            return Message.ofFunction(message.getContent(), functionMessage.getName());
        } else {
            throw new IllegalArgumentException("Got unknown type " + message.getClass().getSimpleName());
        }
    }

    public static BaseMessage convertOpenAiToLangChain(Message message) {
        Role role = message.getRole();
        String content = message.getContent();
        FunctionCallResult functionCall = message.getFunctionCall();
        switch (role) {
            case USER -> {
                return new HumanMessage(content);
            }
            case ASSISTANT -> {
                content = content != null ? content : "";

                if (functionCall != null) {
                    return new ChatFunctionMessage(functionCall.getName(), functionCall.getArguments());
                } else {
                    return new AIMessage(content);
                }

            }
            case SYSTEM -> {
                return new SystemMessage(content);
            }
            default -> {
                return new ChatMessage(content, role.getValue());
            }
        }
    }

    public static BaseMessage convertOpenAiToLangChain(ChatChoice chatChoice) {
        Message message = chatChoice.getMessage();
        return convertOpenAiToLangChain(message);
    }
}
