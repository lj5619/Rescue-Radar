package com.example.sos;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ChatbotAPI {
    private static final String API_URL = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.1";
    private static final String API_KEY = "YOUR_HUGGINGFACE_API_KEY";

    public interface ChatbotCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public static void sendMessage(String userMessage, ChatbotCallback callback) {
        // Set up OkHttpClient with timeouts
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)  // Set timeout to 15 seconds
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        // Prepare JSON request
        JSONObject json = new JSONObject();
        try {
            json.put("inputs", userMessage);
        } catch (Exception e) {
            callback.onError("JSON Error: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        // Make the API call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("API Call Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Unexpected response: " + response.message());
                    return;
                }

                // Check for null body
                if (response.body() == null) {
                    callback.onError("API response body is null");
                    return;
                }

                // Read response as a string
                String responseBody = response.body().string();
                try {
                    JSONArray jsonResponse = new JSONArray(responseBody);
                    if (jsonResponse.length() > 0) {
                        String botReply = jsonResponse.getJSONObject(0).getString("generated_text");
                        callback.onResponse(botReply);
                    } else {
                        callback.onError("No response from chatbot.");
                    }
                } catch (Exception e) {
                    callback.onError("Parsing Error: " + e.getMessage());
                }
            }
        });
    }
}
