package com.manusfree.ai;

import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManusAI {
    
    private static final String TAG = "ManusAI";
    private Context context;
    private ExecutorService executorService;
    private WebSearchTool webSearchTool;
    private FileManager fileManager;
    private Map<String, String> conversationMemory;
    private Random random;
    
    // מאגר תשובות חכמות
    private String[] greetings = {
        "שלום! איך אוכל לעזור לך היום?",
        "היי! אני כאן בשבילך. מה תרצה לדעת?",
        "ברוך הבא! איך אוכל לסייע לך?"
    };
    
    private String[] helpResponses = {
        "אני יכול לעזור לך עם:\n• חיפוש מידע ברשת\n• כתיבה וקריאה של קבצים\n• שיחות חכמות\n• ייעוץ וטיפים",
        "הכלים שלי כוללים:\n🔍 חיפוש ברשת\n📁 ניהול קבצים\n💬 שיחה אינטליגנטית\n🎯 פתרון בעיות"
    };
    
    public interface ResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }
    
    public ManusAI(Context context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(3);
        this.conversationMemory = new HashMap<>();
        this.random = new Random();
    }
    
    public void initialize() {
        try {
            webSearchTool = new WebSearchTool();
            fileManager = new FileManager(context);
            Log.d(TAG, "Manus-Free AI initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Manus AI", e);
            throw new RuntimeException("Failed to initialize AI components", e);
        }
    }
    
    public void processMessage(String message, ResponseCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = generateResponse(message);
                    callback.onResponse(response);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing message", e);
                    callback.onError("שגיאה בעיבוד ההודעה: " + e.getMessage());
                }
            }
        });
    }
    
    private String generateResponse(String message) {
        String lowerMessage = message.toLowerCase().trim();
        
        // זיהוי פקודות כלים
        if (lowerMessage.startsWith("tool:")) {
            return processToolCommand(message);
        }
        
        // תשובות לשאלות נפוצות
        if (containsAny(lowerMessage, "שלום", "היי", "הי", "hello")) {
            return greetings[random.nextInt(greetings.length)];
        }
        
        if (containsAny(lowerMessage, "עזרה", "help", "מה אתה יכול", "איך אתה עובד")) {
            return helpResponses[random.nextInt(helpResponses.length)];
        }
        
        if (containsAny(lowerMessage, "מה שלומך", "איך אתה", "מה נשמע")) {
            return "אני בסדר גמור! תודה ששאלת. איך אני יכול לעזור לך?";
        }
        
        if (containsAny(lowerMessage, "תודה", "thanks", "תנקיו")) {
            return "בכיף! אני כאן בשבילך תמיד 😊";
        }
        
        // שאלות על זמן
        if (containsAny(lowerMessage, "מה השעה", "איזה זמן", "what time")) {
            return "אני לא יכול לראות את השעה הנוכחית, אבל אתה יכול לבדוק בשעון של המכשיר שלך.";
        }
        
        // שאלות על מזג אויר
        if (containsAny(lowerMessage, "מזג אויר", "weather", "גשם", "שמש")) {
            return "לבדיקת מזג האוויר, נסה: tool:search_web מזג אויר [שם העיר]";
        }
        
        // שאלות טכנולוגיה
        if (containsAny(lowerMessage, "טכנולוגיה", "ai", "בינה מלאכותית", "מחשב")) {
            return "טכנולוגיה זה התחום שלי! מה בדיוק מעניין אותך? אני יכול לחפש מידע עדכני ברשת.";
        }
        
        // שאלות עסקיות
        if (containsAny(lowerMessage, "עסק", "business", "שיווק", "מכירות")) {
            return "אני יכול לעזור עם רעיונות עסקיים! רוצה שאחפש מידע עדכני? נסה: tool:search_web [הנושא שמעניין אותך]";
        }
        
        // תשובה כללית חכמה
        return generateSmartResponse(message);
    }
    
    private String processToolCommand(String command) {
        try {
            if (command.startsWith("tool:search_web ")) {
                String query = command.substring("tool:search_web ".length());
                return webSearchTool.search(query);
            }
            
            if (command.startsWith("tool:write_file ")) {
                String params = command.substring("tool:write_file ".length());
                return fileManager.writeFile(params);
            }
            
            if (command.startsWith("tool:read_file ")) {
                String filename = command.substring("tool:read_file ".length());
                return fileManager.readFile(filename);
            }
            
            return "כלי לא מוכר. הכלים הזמינים:\n• tool:search_web [שאילתה]\n• tool:write_file [נתיב] [תוכן]\n• tool:read_file [נתיב]";
            
        } catch (Exception e) {
            return "שגיאה בהפעלת הכלי: " + e.getMessage();
        }
    }
    
    private String generateSmartResponse(String message) {
        // ניתוח הודעה וייצור תשובה חכמה
        if (message.contains("?") || message.contains("איך") || message.contains("מה") || message.contains("למה")) {
            return "זו שאלה מעניינת! אני יכול לחפש מידע עדכני ברשת כדי לתת לך תשובה מדויקת. רוצה שאחפש עבורך?";
        }
        
        if (message.length() > 50) {
            return "אני רואה שיש לך הרבה מה לומר! אני כאן להקשיב ולעזור. איך אוכל לסייע לך בנושא הזה?";
        }
        
        // תשובות כלליות חכמות
        String[] smartResponses = {
            "מעניין! ספר לי עוד על זה.",
            "אני מבין. איך אוכל לעזור לך עם זה?",
            "זה נושא חשוב. רוצה שאחפש מידע נוסף?",
            "יש לי כמה רעיונות. מה הכיוון שמעניין אותך יותר?",
            "בואו נחשוב על זה יחד. מה המטרה שלך?"
        };
        
        return smartResponses[random.nextInt(smartResponses.length)];
    }
    
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    public void cleanup() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

