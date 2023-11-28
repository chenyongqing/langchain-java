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

package com.hw.langchain.examples.chains;

import static com.hw.langchain.examples.utils.PrintUtils.println;

import com.alibaba.fastjson.JSONObject;
import com.hw.langchain.chains.functioncall.base.FunctionCallChain;
import com.hw.langchain.chat.models.openai.ChatOpenAI;
import com.hw.langchain.examples.runner.RunnableExample;
import com.hw.langchain.schema.ChatFunctionMessage;
import com.hw.openai.common.OpenaiApiType;
import com.hw.openai.entity.chat.ChatFunction;
import com.hw.openai.entity.chat.ChatFunction.ChatParameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HamaWhite
 */
@RunnableExample
public class FunctionCallChainExample {

    public static void main(String[] args) {
        var chat = ChatOpenAI.builder()
            .temperature(0)
            .openaiApiKey("4b33f3c7a40e40cd9062c19ca3b3e425")
            .openaiApiVersion("2023-07-01-preview")
            .openaiApiType(OpenaiApiType.AZURE)
            .openaiApiBase("https://jayr110.openai.azure.com/")
            .requestTimeout(9000)
            .model("gpt35-16k")
            .build()
            .init();

        List<ChatFunctionMessage> functions = getFunctions();

        var chain = new FunctionCallChain(chat, functions);
        var result = chain
            .run(Map.of("input", "I want to know the top 10 frontend margin of article about 1001 store yesterday."));


        println(result);
        result = chain.run(Map.of("input", "ok, what can you do."));
        println(result);
    }


    public static List<ChatFunctionMessage> getFunctions() {

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

        ChatFunctionMessage functionMessage = new ChatFunctionMessage(
            "get_data",
            "The get data function is used to get retail dataset, base on tenant, date, dimension, indicators and filter by filter conditions,order by order conditions",
            JSONObject.toJSONString(parameters)
        );
        return List.of(functionMessage);

    }
}
