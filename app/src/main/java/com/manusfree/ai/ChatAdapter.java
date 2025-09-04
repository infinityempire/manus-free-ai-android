package com.manusfree.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    
    private List<ChatMessage> chatMessages;
    private SimpleDateFormat timeFormat;
    
    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
    
    class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;
        private TextView timeTextView;
        private View userMessageContainer;
        private View aiMessageContainer;
        
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            userMessageContainer = itemView.findViewById(R.id.userMessageContainer);
            aiMessageContainer = itemView.findViewById(R.id.aiMessageContainer);
        }
        
        public void bind(ChatMessage message) {
            messageTextView.setText(message.getMessage());
            timeTextView.setText(timeFormat.format(new Date(message.getTimestamp())));
            
            if (message.isUser()) {
                userMessageContainer.setVisibility(View.VISIBLE);
                aiMessageContainer.setVisibility(View.GONE);
            } else {
                userMessageContainer.setVisibility(View.GONE);
                aiMessageContainer.setVisibility(View.VISIBLE);
            }
        }
    }
}

