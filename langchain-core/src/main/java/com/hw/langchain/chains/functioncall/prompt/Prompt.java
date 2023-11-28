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

package com.hw.langchain.chains.functioncall.prompt;

import com.hw.langchain.prompts.chat.ChatPromptTemplate;
import com.hw.langchain.prompts.chat.HumanMessagePromptTemplate;
import com.hw.langchain.prompts.chat.MessagesPlaceholder;
import com.hw.langchain.prompts.chat.SystemMessagePromptTemplate;
import java.util.List;

/**
 * @author HamaWhite
 */
public class Prompt {

    private static final String DEFAULT_TEMPLATE =
        """
             The following is a friendly conversation between a human and an AI. The AI is talkative and provides lots of specific details from its context. If the AI does not know the answer to a question, it truthfully says it does not know.
            """;

//    public static final PromptTemplate PROMPT = new PromptTemplate(List.of("history", "input","functions"), DEFAULT_TEMPLATE);


    public static final ChatPromptTemplate PROMPT = ChatPromptTemplate.fromMessages(List.of(
            SystemMessagePromptTemplate.fromTemplate(DEFAULT_TEMPLATE),
            new MessagesPlaceholder("history"),
            HumanMessagePromptTemplate.fromTemplate("{input}"),
            new MessagesPlaceholder("functions")
        )
    );

}
