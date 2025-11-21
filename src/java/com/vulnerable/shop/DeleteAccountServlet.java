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

@WebServlet("/deleteAccount")
public class DeleteAccountServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String userIdParam = request.getParameter("userId");
        String vulnerableParam = request.getParameter("vulnerable");
        boolean isVulnerable = vulnerableParam != null && vulnerableParam.equals("true");
        
        // Get session information
        HttpSession session = request.getSession(false);
        String sessionUserId = session != null ? session.getAttribute("userId").toString() : "NONE";
        String sessionUsername = session != null ? (String) session.getAttribute("username") : "ANONYMOUS";
        String ipAddress = request.getRemoteAddr();
        
        // SECURITY CHECK: If not in vulnerable mode, verify authorization
        if (!isVulnerable) {
            if (!sessionUserId.equals(userIdParam)) {
                // Log the blocked IDOR attempt
                SecurityLogger.logAction(sessionUserId, sessionUsername, userIdParam, 
                                        "DELETE_ACCOUNT", ipAddress, 
                                        "IDOR attempt blocked");
                
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Access denied. You can only delete your own account.\"}");
                return;
            }
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            // First delete related records due to foreign key constraints
            String deleteOrders = "DELETE FROM orders WHERE user_id=?";
            PreparedStatement ordersStmt = conn.prepareStatement(deleteOrders);
            ordersStmt.setInt(1, Integer.parseInt(userIdParam));
            ordersStmt.executeUpdate();
            
            String deleteComments = "DELETE FROM comments WHERE user_id=?";
            PreparedStatement commentsStmt = conn.prepareStatement(deleteComments);
            commentsStmt.setInt(1, Integer.parseInt(userIdParam));
            commentsStmt.executeUpdate();
            
            // Delete user account
            String sql = "DELETE FROM users WHERE user_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(userIdParam));
            
            int rowsAffected = stmt.executeUpdate();
            
            // Log the account deletion action
            String details = "Account permanently deleted along with all orders and comments (Mode: " + 
                           (isVulnerable ? "VULNERABLE" : "SECURE") + ")";
            SecurityLogger.logAction(sessionUserId, sessionUsername, userIdParam, 
                                    "DELETE_ACCOUNT", ipAddress, details);
            
            // Log IDOR if deleting someone else's account in vulnerable mode
            if (isVulnerable && !sessionUserId.equals(userIdParam)) {
                SecurityLogger.logIDORAttempt(sessionUserId, sessionUsername, userIdParam, 
                                             "DELETE_ACCOUNT (Data Destruction)", ipAddress);
            }
            
            response.setContentType("application/json");
            if (rowsAffected > 0) {
                response.getWriter().write("{\"success\":true,\"message\":\"Account deleted\"}");
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"User not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.setStatus(500);
            response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}