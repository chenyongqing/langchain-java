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

package com.hw.openai;

import static org.assertj.core.api.Assertions.assertThat;

import com.hw.openai.common.OpenaiApiType;
import com.hw.openai.entity.chat.ChatCompletion;
import com.hw.openai.entity.chat.ChatCompletionResp;
import com.hw.openai.entity.chat.ChatFunction;
import com.hw.openai.entity.chat.ChatFunction.ChatParameter;
import com.hw.openai.entity.chat.Message;
import com.hw.openai.entity.completions.Completion;
import com.hw.openai.entity.embeddings.Embedding;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Tingliang Wang
 */
@Disabled("Test requires costly Azure OpenAI calls, can be run manually.")
class AzureOpenAiClientTest {

    private static OpenAiClient client;

//    var openAi = ChatOpenAI.builder()
//        .openaiApiKey("9187dc878e614661bc70d3964145b05d")
//        .openaiApiType(OpenaiApiType.AZURE)
//        .openaiApiBase("https://chatgpt-test-guonei.openai.azure.com/")
//        .openaiApiVersion("2023-07-01-preview")
//        .model("gpt-4")
//        .temperature(0)
//        .requestTimeout(30000).build().init();
    @BeforeAll
    static void setup() {
        client = OpenAiClient.builder()
            .openaiApiKey("9187dc878e614661bc70d3964145b05d")
            .openaiApiVersion("2023-07-01-preview")
            .openaiApiType(OpenaiApiType.AZURE)
            .openaiApiBase("https://chatgpt-test-guonei.openai.azure.com/")
            .requestTimeout(9000)
            .build()
            .init();
    }

    @AfterAll
    static void cleanup() {
        client.close();
    }

    @Test
    void testCompletion() {
        Completion completion = Completion.builder()
            .model("text-davinci-003")
            .prompt(List.of("Say this is a test"))
            .maxTokens(700)
            .temperature(0)
            .build();

        assertThat(client.completion(completion)).isEqualTo("This is indeed a test.");
    }

    @Test
    void testChatCompletion() {
//        Message message = Message.of("Hello!");

        Message system = Message.ofSystem("你是一个富有创造力的、智慧的零售数据分析助手，你需要尽你所能回答用户提问。");
        Message message = Message.of(
            "I want to know the top 10 frontend margin of article about 1001 store yesterday");

        Message message1 = Message.of(
            "什么是门店库存周转天数");

        Map<String, Object> properties = new HashMap<>();

        properties.put("dt", Map.of(
            "type", "string",
            "description",
            "The date required to get data, format: 'yyyyMMdd' or 'yyyyMM' or 'yyyy'.Multiple parameter values should be separated by commas."
        ));

        properties.put("tenant", Map.of(
            "type", "number",
            "description", "Tenant ID required to get data,default is 51007.Can only have one parameter value."
        ));

        properties.put("dimensions", Map.of(
            "type", "string",
            "description",
            String.format("Dimensions for getting data, such as [%s]", "'article','store','store_article','vendor'")
        ));

        properties.put("indicators", Map.of(
            "type", "string",
            "description", String.format(
                "Indicators for getting data, such as [%s]. Multiple parameter values should be separated by commas,format: 'sales,profit'.",
                "'sales','profit','inventory'")
        ));

        properties.put("filters", Map.of(
            "type", "string",
            "description", String.format(
                "Filter conditions for getting data, such as 'sales_amt>100 and end_inventory_num>1000'. Feilds in conditions should be some of [%s]",
                "'sales','profit','inventory'")
        ));

        properties.put("orders", Map.of(
            "type", "string",
            "description", String.format(
                "Order conditions for getting data, such as 'sale_amt desc','end_inventory_num asc'. Feilds in conditions should be one of [%s]",
                "'sales','profit','inventory'")
        ));
        properties.put("limit", Map.of(
            "type", "number",
            "description", "The number of rows limited for getting data,default is 20."
        ));

        ChatParameter parameters = ChatParameter.builder()
            .type("object")
            .properties(properties)
            .required(Arrays.asList("dt", "tenant", "dimensions", "indicators"))
            .build();

        ChatFunction getData = ChatFunction.builder()
            .name("get_data")
            .description(
                "The get data function is used to get retail dataset, base on tenant, date, dimension, indicators and filter by filter conditions,order by order conditions")
            .parameters(parameters)
            .build();

        ChatCompletion chatCompletion = ChatCompletion.builder()
            .model("gpt-4")
            .temperature(0)
            .messages(List.of(system, message))
            .functions(List.of(getData))
            .build();

//        assertThat(client.chatCompletion(chatCompletion)).isEqualTo("Hello there! How can I assist you today?");

        ChatCompletionResp resp = client.createChatCompletion(chatCompletion);

        System.out.println(resp);
    }

    @Test
    void testEmbeddings() {
        var embedding = Embedding.builder()
            .model("text-embedding-ada-002")
            .input(List.of("The food was delicious and the waiter..."))
            .build();

        var response = client.createEmbedding(embedding);

        assertThat(response).as("Response should not be null").isNotNull();
        assertThat(response.getData()).as("Data list should have size 1").hasSize(1);
        assertThat(response.getData().get(0).getEmbedding()).as("Embedding should have size 1536").hasSize(1536);
    }
}