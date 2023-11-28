package com.hw.langchain.chains.functioncall.base;

import static com.hw.langchain.chains.functioncall.prompt.Prompt.PROMPT;

import com.alibaba.fastjson.JSONObject;
import com.hw.langchain.base.language.BaseLanguageModel;
import com.hw.langchain.chains.base.Chain;
import com.hw.langchain.memory.buffer.ConversationBufferMemory;
import com.hw.langchain.prompts.base.BasePromptTemplate;
import com.hw.langchain.schema.BaseLLMOutputParser;
import com.hw.langchain.schema.BaseMemory;
import com.hw.langchain.schema.ChatFunctionMessage;
import com.hw.langchain.schema.ChatGeneration;
import com.hw.langchain.schema.LLMResult;
import com.hw.langchain.schema.NoOpOutputParser;
import com.hw.langchain.schema.PromptValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description:
 * @date: 2023/11/7 10:33
 * @user: yongqing.chen
 */
public class FunctionCallChain extends Chain {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionCallChain.class);

    protected String inputKey = "input";
    protected String outputKey = "response";
    private final List<ChatFunctionMessage> functions;


    protected BaseLanguageModel llm;

    /**
     * Prompt object to use.
     */
    @Getter
    protected BasePromptTemplate prompt;

    /**
     * Output parser to use. Defaults to one that takes the most likely string but does not change it.
     */
    protected BaseLLMOutputParser<String> outputParser = new NoOpOutputParser();

    /**
     * Whether to return only the final parsed result. Defaults to true. If false, will return a bunch of extra
     * information about the generation.
     */
    protected boolean returnFinalOnly = false;

    public FunctionCallChain(BaseLanguageModel llm, List<ChatFunctionMessage> functions) {
        this(llm, PROMPT, functions, new ConversationBufferMemory(true));
    }

    public FunctionCallChain(BaseLanguageModel llm, BasePromptTemplate prompt, List<ChatFunctionMessage> functions,
        BaseMemory memory) {
        this.memory = memory;
        this.functions = functions;

        this.prompt = prompt;
        this.llm = llm;
    }


    @Override
    public String chainType() {
        return "function_call_chain";
    }

    @Override
    public List<String> inputKeys() {
        return List.of(inputKey);
    }

    @Override
    public List<String> outputKeys() {
        return List.of(outputKey);
    }

    @Override
    public Map<String, Object> prepInputs(Map<String, Object> inputs) {
        Map<String, Object> newInputs = new HashMap<>(inputs);
        if (functions != null) {
            newInputs.put("functions", functions);
        }

        if (memory != null) {
            Map<String, Object> externalContext = memory.loadMemoryVariables(inputs);
            newInputs.putAll(externalContext);
        }
        validateInputs(newInputs);
        return newInputs;
    }

    private void validateInputs(Map<String, Object> inputs) {
        Set<String> missingKeys = new HashSet<>(inputKeys());
        missingKeys.removeAll(inputs.keySet());
        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException(String.format("Missing some input keys: %s", missingKeys));
        }
    }

    @Override
    protected Map<String, String> innerCall(Map<String, Object> inputs) {
        LLMResult response = generate(List.of(inputs));
        return createOutputs(response).get(0);
    }

    private List<PromptValue> prepPrompts(List<Map<String, Object>> inputList) {
        List<PromptValue> prompts = new ArrayList<>();
        for (Map<String, Object> inputs : inputList) {
            Map<String, Object> selectedInputs = new HashMap<>();

            prompt.getInputVariables().forEach(key -> {
                if (inputs.containsKey(key)) {
                    selectedInputs.put(key, inputs.get(key));
                }
            });

            PromptValue promptValue = this.prompt.formatPrompt(selectedInputs);
            LOG.debug("Prompt after formatting:\n{}", promptValue);
            prompts.add(promptValue);
        }
        return prompts;
    }

    private LLMResult generate(List<Map<String, Object>> inputList) {
        List<String> stop = prepStop(inputList);
        List<PromptValue> prompts = prepPrompts(inputList);
        return llm.generatePrompt(prompts, stop);
    }

    @SuppressWarnings("unchecked")
    private List<String> prepStop(List<Map<String, Object>> inputList) {
        Map<String, Object> firstInput = inputList.get(0);
        return firstInput.containsKey("stop") ? (List<String>) firstInput.get("stop") : null;
    }


    private List<Map<String, String>> createOutputs(LLMResult llmResult) {
        var result = llmResult.getGenerations().stream().map(generation -> {
            ChatGeneration chatGeneration = (ChatGeneration) generation.get(0);
            if (chatGeneration.getMessage() instanceof ChatFunctionMessage functionCall) {
                String fs = functionCall.toString();

                JSONObject fcObj = new JSONObject();

                fcObj.put("name", StringUtils.defaultString(functionCall.getName(), functionCall.getContent()));
                fcObj.put("arguments", functionCall.getArguments());

                return Map.of(outputKey, functionCall.getContent(), "function_call", fcObj.toString(),
                    "full_generation", generation.toString());
            } else {
                return Map.of(outputKey, outputParser.parseResult(generation), "full_generation",
                    generation.toString());
            }
        }).toList();

        if (returnFinalOnly) {
            result = result.stream().map(r -> Map.of(outputKey, r.get(outputKey))).toList();
        }
        return result;
    }
}
