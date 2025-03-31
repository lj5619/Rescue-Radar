package com.example.sos;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {
    private EditText userInput;
    private TextView chatDisplay;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize UI components
        userInput = findViewById(R.id.userInput);
        chatDisplay = findViewById(R.id.chatDisplay);
        sendButton = findViewById(R.id.sendButton);

        // Debugging Log
        if (userInput == null || chatDisplay == null || sendButton == null) {
            Log.e("ChatActivity", "UI Elements are not properly initialized! Check layout XML.");
            Toast.makeText(this, "UI initialization failed!", Toast.LENGTH_LONG).show();
            return;
        }

        // Send Button Click Listener
        sendButton.setOnClickListener(v -> {
            String message = userInput.getText().toString().trim();

            // Prevent sending empty messages
            if (message.isEmpty()) {
                Toast.makeText(ChatActivity.this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            chatDisplay.append("\nYou: " + message);
            userInput.setText("");

            ChatbotAPI.sendMessage(message, new ChatbotAPI.ChatbotCallback() {
                @Override
                public void onResponse(String response) {
                    runOnUiThread(() -> {
                        if (response != null && !response.trim().isEmpty()) {
                            chatDisplay.append("\nBot: " + response);
                        } else {
                            chatDisplay.append("\nBot: [No Response]");
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> chatDisplay.append("\nError: " + error));
                }
            });
        });
    }
}
