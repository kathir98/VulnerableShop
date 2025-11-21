package com.vulnerable.shop;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SecurityLogger {
    private static final String LOG_FILE = "security_audit.log";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static void logAction(String sessionUserId, String sessionUsername, 
                                  String targetUserId, String action, 
                                  String ipAddress, String details) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            String timestamp = dateFormat.format(new Date());
            String logEntry = String.format("[%s] | Session user ID:%s | Target User ID: %s | Action: %s | IP: %s | Details: %s",
                timestamp, sessionUserId, targetUserId, action, ipAddress, details);
            
            pw.println(logEntry);
            
            // Also check for potential IDOR
            if (!sessionUserId.equals(targetUserId)) {
                String idorAlert = String.format("[%s] | POTENTIAL IDOR DETECTED️ | User ID:%s performed %s on User ID:%s | IP: %s",
                    timestamp, sessionUserId, action, targetUserId, ipAddress);
                pw.println(idorAlert);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void logIDORAttempt(String sessionUserId, String sessionUsername,
                                      String targetUserId, String action, String ipAddress) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            String timestamp = dateFormat.format(new Date());
            String idorLog = String.format("[%s] |  IDOR ATTACK  | Attacker user ID:%s | Victim User ID:%s | Action: %s | IP: %s",
                timestamp, sessionUserId, targetUserId, action, ipAddress);
            
            pw.println(idorLog);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}