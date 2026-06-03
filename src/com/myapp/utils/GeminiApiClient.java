package com.myapp.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiApiClient {


    private static final String API_KEY = "";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent?key=" + API_KEY;

    /**
     * Generates a service description using the Gemini API.
     * @param serviceTitle The title of the service.
     * @return The AI-generated description.
     * @throws Exception if the API call fails.
     */
    public static String generateDescription(String serviceTitle) throws Exception {

        HttpClient client = HttpClient.newHttpClient();


        String jsonPayload = "{" +
                "\"contents\": [{" +
                "\"role\": \"user\"," +
                "\"parts\": [{" +
                "\"text\": \"Generate a compelling and professional service description for a service titled '" + serviceTitle + "'. The description should be suitable for a service booking app. Make it sound appealing to customers. Include a brief introduction, a list of key services offered, and a concluding sentence on why the customer should choose this service.\"" +
                "}]" +
                "}]" +
                "}";


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseResponse(response.body());
        } else {
            throw new RuntimeException("Failed to call Gemini API. Status: " + response.statusCode() + " Body: " + response.body());
        }
    }

    /**
     * A simple parser to extract the text from the Gemini API's JSON response.
     * @param responseBody The JSON response from the API.
     * @return The extracted text content.
     */
    private static String parseResponse(String responseBody) {
        try {
            int textIndex = responseBody.indexOf("\"text\": \"");
            if (textIndex == -1) {
                return "Error: Could not parse the response.";
            }
            int startIndex = textIndex + 9;
            int endIndex = responseBody.indexOf("\"", startIndex);

            String text = responseBody.substring(startIndex, endIndex);
            return text.replace("\\n", "\n");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing the API response.";
        }
    }
}
