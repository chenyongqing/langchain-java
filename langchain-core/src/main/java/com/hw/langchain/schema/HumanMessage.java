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

package com.hw.langchain.schema;

/**
 * Type of message that is spoken by the human.
 * @author HamaWhite
 */
public class HumanMessage extends BaseMessage {

    public HumanMessage(String content) {
        super(content);
    }

    @Override
    public String type() {
        return "human";
    }

    @Override
    public String toString() {
        return "HumanMessage{" +
                "content='" + content + '\'' +
                ", additionalKwargs=" + additionalKwargs +
                '}';
    }
}
