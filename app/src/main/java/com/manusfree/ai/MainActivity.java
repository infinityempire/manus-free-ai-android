package com.manusfree.ai;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
        
        // בדיקת null כדי למנוע קריסות
        if (chatRecyclerView == null || messageEditText == null || sendButton == null) {
            Toast.makeText(this, "שגיאה בטעינת הממשק", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
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
        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                sendMessage();
                return true;
            }
        });
    }
    
    private void initializeAI() {
        try {
            manusAI = new ManusAI(this);
            manusAI.initialize();
        } catch (Exception e) {
            Log.e("MainActivity", "Error initializing AI", e);
            Toast.makeText(this, "שגיאה באתחול המערכת", Toast.LENGTH_LONG).show();
            // המשך בלי AI - האפליקציה תעבוד במצב מוגבל
        }
    }
    
    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // הוספת הודעת המשתמש
        addMessage(message, true);
        messageEditText.setText("");
        
        // בדיקה אם AI זמין
        if (manusAI == null) {
            addMessage("המערכת לא זמינה כרגע. אנא נסה שוב מאוחר יותר.", false);
            return;
        }
        
        // הצגת אינדיקטור טעינה
        addMessage("מעבד...", false);
        
        // שליחה ל-AI
        try {
            manusAI.processMessage(message, new ManusAI.ResponseCallback() {
                @Override
                public void onResponse(String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // הסרת אינדיקטור הטעינה
                            if (!chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).getMessage().equals("מעבד...")) {
                                chatMessages.remove(chatMessages.size() - 1);
                                chatAdapter.notifyItemRemoved(chatMessages.size());
                            }
                            
                            // הוספת תשובת ה-AI
                            addMessage(response, false);
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // הסרת אינדיקטור הטעינה
                            if (!chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).getMessage().equals("מעבד...")) {
                                chatMessages.remove(chatMessages.size() - 1);
                                chatAdapter.notifyItemRemoved(chatMessages.size());
                            }
                            
                            addMessage("סליחה, אירעה שגיאה: " + error, false);
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error sending message", e);
            // הסרת אינדיקטור הטעינה
            if (!chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).getMessage().equals("מעבד...")) {
                chatMessages.remove(chatMessages.size() - 1);
                chatAdapter.notifyItemRemoved(chatMessages.size());
            }
            addMessage("שגיאה בשליחת ההודעה", false);
        }
    }
    
    private void addMessage(String message, boolean isUser) {
        try {
            if (message == null || message.trim().isEmpty()) {
                return;
            }
            
            if (chatMessages == null || chatAdapter == null || chatRecyclerView == null) {
                Log.e("MainActivity", "Chat components not initialized");
                return;
            }
            
            ChatMessage chatMessage = new ChatMessage(message, isUser, System.currentTimeMillis());
            chatMessages.add(chatMessage);
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        } catch (Exception e) {
            Log.e("MainActivity", "Error adding message", e);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manusAI != null) {
            manusAI.cleanup();
        }
    }
}

