package com.vulnerable.shop;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/changePassword")
public class ChangePasswordServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String userId = request.getParameter("userId");
        String newPassword = request.getParameter("newPassword");
        String vulnerableParam = request.getParameter("vulnerable");
        boolean isVulnerable = vulnerableParam != null && vulnerableParam.equals("true");
        
        // Get session information
        HttpSession session = request.getSession(false);
        String sessionUserId = session != null ? session.getAttribute("userId").toString() : "NONE";
        String sessionUsername = session != null ? (String) session.getAttribute("username") : "ANONYMOUS";
        String ipAddress = request.getRemoteAddr();
        
        // SECURITY CHECK: If not in vulnerable mode, verify authorization
        if (!isVulnerable) {
            if (!sessionUserId.equals(userId)) {
                // Log the blocked IDOR attempt
                SecurityLogger.logAction(sessionUserId, sessionUsername, userId, 
                                        "CHANGE_PASSWORD", ipAddress, 
                                        "IDOR attempt blocked");
                
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Access denied. You can only change your own password.\"}");
                return;
            }
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            String updateSql = "UPDATE users SET password=? WHERE user_id=?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, newPassword);
            updateStmt.setInt(2, Integer.parseInt(userId));
            
            int rowsAffected = updateStmt.executeUpdate();
            
            // Log the password change action
            String details = "Password changed to: " + newPassword + " (Mode: " + 
                           (isVulnerable ? "VULNERABLE" : "SECURE") + ")";
            SecurityLogger.logAction(sessionUserId, sessionUsername, userId, 
                                    "CHANGE_PASSWORD", ipAddress, details);
            
            // Log IDOR if changing someone else's password in vulnerable mode
            if (isVulnerable && !sessionUserId.equals(userId)) {
                SecurityLogger.logIDORAttempt(sessionUserId, sessionUsername, userId, 
                                             "CHANGE_PASSWORD (Account Takeover)", ipAddress);
            }
            
            response.setContentType("application/json");
            
            if (rowsAffected > 0) {
                response.getWriter().write("{\"success\":true}");
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"Update failed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Server error\"}");
        }
    }
}