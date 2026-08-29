package com.adrovis.adrovis_backend.interview.client;

import com.adrovis.adrovis_backend.interview.config.GeminiInterviewProperties;
import com.adrovis.adrovis_backend.interview.dto.ai.AiInterviewPackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiInterviewClient {

    private final RestClient restClient;
    private final GeminiInterviewProperties properties;
    private final ObjectMapper objectMapper;

    public AiInterviewPackage generate(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new GeminiInterviewException(
                    "Gemini prompt cannot be empty."
            );
        }

        if (properties.getApiKey() == null ||
                properties.getApiKey().isBlank()) {

            throw new GeminiInterviewException(
                    "Gemini API key is not configured."
            );
        }

        if (properties.getBaseUrl() == null ||
                properties.getBaseUrl().isBlank()) {

            throw new GeminiInterviewException(
                    "Gemini base URL is not configured."
            );
        }

        if (properties.getModel() == null ||
                properties.getModel().isBlank()) {

            throw new GeminiInterviewException(
                    "Gemini model is not configured."
            );
        }

        String url =
                properties.getBaseUrl()
                        + "/v1beta/models/"
                        + properties.getModel()
                        + ":generateContent";

        log.info(
                "Preparing Gemini interview generation request. model={}, promptCharacters={}, maxOutputTokens={}, thinkingBudget={}",
                properties.getModel(),
                prompt.length(),
                properties.getMaxOutputTokens(),
                properties.getThinkingBudget()
        );

        Map<String, Object> requestBody =
                new LinkedHashMap<>();

        /*
         * =========================================================
         * CONTENT
         * =========================================================
         */

        Map<String, Object> textPart =
                new LinkedHashMap<>();

        textPart.put(
                "text",
                prompt
        );

        Map<String, Object> content =
                new LinkedHashMap<>();

        content.put(
                "parts",
                List.of(textPart)
        );

        requestBody.put(
                "contents",
                List.of(content)
        );

        /*
         * =========================================================
         * GENERATION CONFIG
         *
         * IMPORTANT:
         * No additionalProperties.
         * No ["STRING", "NULL"] nullable type arrays.
         * =========================================================
         */

        Map<String, Object> generationConfig =
                new LinkedHashMap<>();

        generationConfig.put(
                "response_mime_type",
                "application/json"
        );

        generationConfig.put(
                "temperature",
                properties.getTemperature()
        );

        generationConfig.put(
                "max_output_tokens",
                properties.getMaxOutputTokens()
        );

        /*
         * Reserve/limit hidden reasoning tokens so they can't silently
         * consume the entire maxOutputTokens budget and truncate the
         * visible JSON before it completes.
         */

        generationConfig.put(
                "response_schema",
                buildResponseSchema()
        );

        requestBody.put(
                "generationConfig",
                generationConfig
        );

        /*
         * =========================================================
         * REQUEST / RETRIES
         * =========================================================
         */

        int maxAttempts =
                Math.max(
                        1,
                        properties.getMaxRetries() + 1
                );

        for (int attempt = 1;
             attempt <= maxAttempts;
             attempt++) {

            log.info(
                    "Gemini interview generation request started. model={}, attempt={}/{}",
                    properties.getModel(),
                    attempt,
                    maxAttempts
            );

            try {

                String responseBody =
                        restClient.post()

                                .uri(URI.create(url))

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .header(
                                        "x-goog-api-key",
                                        properties.getApiKey()
                                )

                                .body(requestBody)

                                .retrieve()

                                .onStatus(
                                        HttpStatusCode::isError,
                                        (request, response) -> {

                                            String errorBody;

                                            try {

                                                errorBody =
                                                        new String(
                                                                response.getBody()
                                                                        .readAllBytes(),
                                                                StandardCharsets.UTF_8
                                                        );

                                            } catch (Exception ex) {

                                                errorBody =
                                                        "Unable to read Gemini error response.";

                                                log.error(
                                                        "Could not read Gemini error response body.",
                                                        ex
                                                );
                                            }

                                            log.error(
                                                    "Gemini API rejected request. status={}, body={}",
                                                    response.getStatusCode().value(),
                                                    errorBody
                                            );

                                            throw new GeminiApiException(
                                                    response.getStatusCode().value(),
                                                    errorBody
                                            );
                                        }
                                )

                                .body(String.class);

                if (responseBody == null ||
                        responseBody.isBlank()) {

                    throw new GeminiInterviewException(
                            "Gemini returned an empty response."
                    );
                }

                log.info(
                        "Gemini interview generation response received successfully. attempt={}",
                        attempt
                );

                return parseResponse(responseBody);

            } catch (GeminiApiException ex) {

                /*
                 * 400 / 401 / 403 are request/configuration problems.
                 * Do NOT waste retries on them.
                 */

                if (ex.getStatusCode() == 400 ||
                        ex.getStatusCode() == 401 ||
                        ex.getStatusCode() == 403) {

                    log.error(
                            "Gemini request rejected permanently. status={}, response={}",
                            ex.getStatusCode(),
                            ex.getResponseBody()
                    );

                    throw new GeminiInterviewException(
                            "AI interview preparation request was rejected by Gemini. "
                                    + "HTTP "
                                    + ex.getStatusCode()
                                    + ": "
                                    + ex.getResponseBody(),
                            ex
                    );
                }

                if (attempt >= maxAttempts) {

                    log.error(
                            "Gemini request failed permanently. status={}, attempts={}, response={}",
                            ex.getStatusCode(),
                            attempt,
                            ex.getResponseBody()
                    );

                    throw new GeminiInterviewException(
                            "AI interview preparation is temporarily unavailable.",
                            ex
                    );
                }

                log.warn(
                        "Gemini transient error. Retrying. status={}, attempt={}/{}",
                        ex.getStatusCode(),
                        attempt,
                        maxAttempts
                );

                sleepBeforeRetry(attempt);

            } catch (GeminiInterviewException ex) {

                /*
                 * Includes MAX_TOKENS / SAFETY / malformed-JSON failures
                 * raised from parseResponse(). These are not transient
                 * network issues — retrying with the same prompt and
                 * the same token budget would almost certainly fail
                 * again the same way, so we fail fast with a clear
                 * message instead of burning retry attempts.
                 */

                throw ex;

            } catch (Exception ex) {

                if (attempt >= maxAttempts) {

                    log.error(
                            "Gemini request failed after {} attempts.",
                            attempt,
                            ex
                    );

                    throw new GeminiInterviewException(
                            "AI interview preparation is temporarily unavailable.",
                            ex
                    );
                }

                log.warn(
                        "Unexpected Gemini request failure. Retrying. attempt={}/{}",
                        attempt,
                        maxAttempts,
                        ex
                );

                sleepBeforeRetry(attempt);
            }
        }

        throw new GeminiInterviewException(
                "AI interview preparation is temporarily unavailable."
        );
    }

    /*
     * =============================================================
     * GEMINI RESPONSE SCHEMA
     * =============================================================
     */

    private Map<String, Object> buildResponseSchema() {

        /*
         * ---------------------------------------------------------
         * QUESTION OBJECT
         * ---------------------------------------------------------
         */

        Map<String, Object> questionSchema =
                new LinkedHashMap<>();

        questionSchema.put(
                "type",
                "OBJECT"
        );

        Map<String, Object> questionProperties =
                new LinkedHashMap<>();

        questionProperties.put(
                "questionNumber",
                integerSchema()
        );

        questionProperties.put(
                "category",
                stringSchema()
        );

        questionProperties.put(
                "difficulty",
                stringSchema()
        );

        questionProperties.put(
                "answerSource",
                stringSchema()
        );

        questionProperties.put(
                "question",
                stringSchema()
        );

        questionProperties.put(
                "expectedAnswer",
                stringSchema()
        );

        questionProperties.put(
                "keyPoints",
                arrayOfStringsSchema()
        );

        questionProperties.put(
                "followUpQuestions",
                arrayOfFollowUpQuestionsSchema()
        );

        questionProperties.put(
                "resumeBasis",
                stringSchema()
        );

        questionProperties.put(
                "interviewerGoal",
                stringSchema()
        );

        questionSchema.put(
                "properties",
                questionProperties
        );

        questionSchema.put(
                "required",
                List.of(
                        "questionNumber",
                        "category",
                        "difficulty",
                        "answerSource",
                        "question",
                        "expectedAnswer",
                        "keyPoints",
                        "followUpQuestions",
                        "resumeBasis",
                        "interviewerGoal"
                )
        );

        /*
         * ---------------------------------------------------------
         * ROOT OBJECT
         * ---------------------------------------------------------
         */

        Map<String, Object> root =
                new LinkedHashMap<>();

        root.put(
                "type",
                "OBJECT"
        );

        Map<String, Object> rootProperties =
                new LinkedHashMap<>();

        Map<String, Object> questionsSchema =
                new LinkedHashMap<>();

        questionsSchema.put(
                "type",
                "ARRAY"
        );

        questionsSchema.put(
                "items",
                questionSchema
        );

        rootProperties.put(
                "questions",
                questionsSchema
        );

        root.put(
                "properties",
                rootProperties
        );

        root.put(
                "required",
                List.of(
                        "questions"
                )
        );

        return root;
    }

    private Map<String, Object> stringSchema() {

        Map<String, Object> schema =
                new LinkedHashMap<>();

        schema.put(
                "type",
                "STRING"
        );

        return schema;
    }

    private Map<String, Object> integerSchema() {

        Map<String, Object> schema =
                new LinkedHashMap<>();

        schema.put(
                "type",
                "INTEGER"
        );

        return schema;
    }

    private Map<String, Object> arrayOfFollowUpQuestionsSchema() {

        Map<String, Object> followUpQuestionSchema =
                new LinkedHashMap<>();

        followUpQuestionSchema.put(
                "type",
                "OBJECT"
        );

        Map<String, Object> properties =
                new LinkedHashMap<>();

        properties.put(
                "question",
                stringSchema()
        );

        properties.put(
                "expectedAnswer",
                stringSchema()
        );

        followUpQuestionSchema.put(
                "properties",
                properties
        );

        followUpQuestionSchema.put(
                "required",
                List.of(
                        "question",
                        "expectedAnswer"
                )
        );

        Map<String, Object> schema =
                new LinkedHashMap<>();

        schema.put(
                "type",
                "ARRAY"
        );

        schema.put(
                "items",
                followUpQuestionSchema
        );

        return schema;
    }

    private Map<String, Object> arrayOfStringsSchema() {

        Map<String, Object> itemSchema =
                new LinkedHashMap<>();

        itemSchema.put(
                "type",
                "STRING"
        );

        Map<String, Object> schema =
                new LinkedHashMap<>();

        schema.put(
                "type",
                "ARRAY"
        );

        schema.put(
                "items",
                itemSchema
        );

        return schema;
    }

    /*
     * =============================================================
     * RESPONSE PARSING
     * =============================================================
     */

    private AiInterviewPackage parseResponse(
            String responseBody
    ) {

        JsonNode root;

        try {

            root =
                    objectMapper.readTree(
                            responseBody
                    );

        } catch (Exception ex) {

            log.error(
                    "Gemini response envelope could not be parsed as JSON.",
                    ex
            );

            throw new GeminiInterviewException(
                    "Gemini returned an unreadable response envelope.",
                    ex
            );
        }

        JsonNode candidates =
                root.path("candidates");

        if (!candidates.isArray()
                || candidates.isEmpty()) {

            String blockReason =
                    root.path("promptFeedback")
                            .path("blockReason")
                            .asText(null);

            throw new GeminiInterviewException(
                    blockReason != null
                            ? "Gemini blocked the request. blockReason="
                            + blockReason
                            : "Gemini returned no candidates."
            );
        }

        JsonNode firstCandidate =
                candidates.get(0);

        String finishReason =
                firstCandidate
                        .path("finishReason")
                        .asText(null);

        JsonNode parts =
                firstCandidate
                        .path("content")
                        .path("parts");

        if (!parts.isArray()
                || parts.isEmpty()) {

            throw new GeminiInterviewException(
                    "Gemini returned no response content. finishReason="
                            + finishReason
            );
        }

        String jsonText =
                parts.get(0)
                        .path("text")
                        .asText();

        int responseCharacters =
                jsonText == null
                        ? 0
                        : jsonText.length();

        log.info(
                "Gemini candidate received. finishReason={}, responseCharacters={}",
                finishReason,
                responseCharacters
        );

        if (jsonText == null
                || jsonText.isBlank()) {

            throw new GeminiInterviewException(
                    "Gemini returned empty interview content. finishReason="
                            + finishReason
            );
        }

        /*
         * Gemini completed successfully.
         *
         * Do not reject STOP responses.
         * The actual JSON parser below determines whether
         * the generated content is usable.
         */

        jsonText =
                cleanJson(jsonText);

        JsonNode generatedJson;

        try {

            generatedJson =
                    objectMapper.readTree(
                            jsonText
                    );

        } catch (JsonProcessingException ex) {

            log.error(
                    "Gemini returned malformed/incomplete JSON. "
                            + "finishReason={}, responseCharacters={}",
                    finishReason,
                    responseCharacters,
                    ex
            );

            log.error(
                    "Malformed Gemini JSON:\n{}",
                    jsonText
            );

            throw new GeminiInterviewException(
                    "Gemini returned malformed or incomplete JSON.",
                    ex
            );
        }

        if (!generatedJson.isObject()) {

            throw new GeminiInterviewException(
                    "Gemini returned an invalid interview package."
            );
        }

        /*
         * =========================================================
         * RAW GEMINI OUTPUT
         * =========================================================
         */

        log.info(
                "============================================================"
        );

        log.info(
                "RAW GEMINI GENERATED JSON START"
        );

        log.info(
                "RAW GEMINI GENERATED JSON:\n{}",
                jsonText
        );

        log.info(
                "RAW GEMINI GENERATED JSON END"
        );

        log.info(
                "============================================================"
        );

        /*
         * =========================================================
         * JACKSON -> JAVA DTO
         * =========================================================
         */

        try {

            AiInterviewPackage aiPackage =
                    objectMapper.treeToValue(
                            generatedJson,
                            AiInterviewPackage.class
                    );

            log.info(
                    "Gemini JSON successfully mapped to AiInterviewPackage. "
                            + "questionCount={}",
                    aiPackage.questions() == null
                            ? 0
                            : aiPackage.questions().size()
            );

            log.info(
                    "Mapped Gemini interview package:\n{}",
                    objectMapper.writeValueAsString(
                            aiPackage
                    )
            );

            return aiPackage;

        } catch (JsonProcessingException ex) {

            log.error(
                    "Gemini JSON was valid but could not be mapped "
                            + "to AiInterviewPackage.",
                    ex
            );

            log.error(
                    "JSON that failed DTO mapping:\n{}",
                    jsonText
            );

            throw new GeminiInterviewException(
                    "Gemini generated valid JSON, but the response "
                            + "could not be mapped to the interview package.",
                    ex
            );
        }
    }

    private String cleanJson(String value) {

        String json =
                value.trim();

        if (json.startsWith("```json")) {

            json =
                    json.substring(7);

            if (json.endsWith("```")) {

                json =
                        json.substring(
                                0,
                                json.length() - 3
                        );
            }

        } else if (json.startsWith("```")) {

            json =
                    json.substring(3);

            if (json.endsWith("```")) {

                json =
                        json.substring(
                                0,
                                json.length() - 3
                        );
            }
        }

        return json.trim();
    }

    /*
     * =============================================================
     * RETRY
     * =============================================================
     */

    private void sleepBeforeRetry(int attempt) {

        long baseDelay =
                Math.max(
                        100,
                        properties.getRetryBaseDelayMs()
                );

        long delay =
                baseDelay *
                        (1L << Math.min(attempt - 1, 5));

        try {

            Thread.sleep(delay);

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new GeminiInterviewException(
                    "Gemini retry was interrupted.",
                    ex
            );
        }
    }

    /*
     * =============================================================
     * EXCEPTIONS
     * =============================================================
     */

    public static class GeminiInterviewException
            extends RuntimeException {

        public GeminiInterviewException(
                String message
        ) {
            super(message);
        }

        public GeminiInterviewException(
                String message,
                Throwable cause
        ) {
            super(message, cause);
        }
    }

    private static class GeminiApiException
            extends RuntimeException {

        private final int statusCode;
        private final String responseBody;

        GeminiApiException(
                int statusCode,
                String responseBody
        ) {

            super(
                    "Gemini API returned HTTP "
                            + statusCode
                            + ": "
                            + responseBody
            );

            this.statusCode =
                    statusCode;

            this.responseBody =
                    responseBody;
        }

        int getStatusCode() {
            return statusCode;
        }

        String getResponseBody() {
            return responseBody;
        }
    }
}