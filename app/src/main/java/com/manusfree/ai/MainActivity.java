package com.manusfree.ai;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private ManusAI manusAI;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        initializeAI();
        
        // הודעת ברוכים הבאים
        addMessage("שלום! אני Manus-Free, העוזר הדיגיטלי שלך. איך אוכל לעזור לך היום?", false);
    }
    
    private void initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
    }
    
    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
    }
    
    private void setupClickListeners() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        
        // שליחה עם Enter
        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }
    
    private void initializeAI() {
        manusAI = new ManusAI(this);
        manusAI.initialize();
    }
    
    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // הוספת הודעת המשתמש
        addMessage(message, true);
        messageEditText.setText("");
        
        // הצגת אינדיקטור טעינה
        addMessage("מעבד...", false);
        
        // שליחה ל-AI
        manusAI.processMessage(message, new ManusAI.ResponseCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    // הסרת אינדיקטור הטעינה
                    if (!chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).getMessage().equals("מעבד...")) {
                        chatMessages.remove(chatMessages.size() - 1);
                    }
                    
                    // הוספת תשובת ה-AI
                    addMessage(response, false);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // הסרת אינדיקטור הטעינה
                    if (!chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).getMessage().equals("מעבד...")) {
                        chatMessages.remove(chatMessages.size() - 1);
                    }
                    
                    addMessage("סליחה, אירעה שגיאה: " + error, false);
                });
            }
        });
    }
    
    private void addMessage(String message, boolean isUser) {
        ChatMessage chatMessage = new ChatMessage(message, isUser, System.currentTimeMillis());
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manusAI != null) {
            manusAI.cleanup();
        }
    }
}

