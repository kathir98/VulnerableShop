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

@WebServlet("/updateProfile")
public class UpdateProfileServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String userId = request.getParameter("userId");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String address = request.getParameter("address");
        String creditCard = request.getParameter("creditCard");
        String cvv = request.getParameter("cvv");
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
                                        "UPDATE_PROFILE", ipAddress, 
                                        "IDOR attempt blocked");
                
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Access denied. You can only edit your own profile.\"}");
                return;
            }
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET email=?, full_name=?, address=?, credit_card=?, cvv=? WHERE user_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, fullName);
            stmt.setString(3, address);
            stmt.setString(4, creditCard);
            stmt.setString(5, cvv);
            stmt.setInt(6, Integer.parseInt(userId));
            
            int rowsAffected = stmt.executeUpdate();
            
            // Log the profile update action
            String details = String.format("Updated email=%s, fullName=%s, creditCard=****%s, cvv=%s (Mode: %s)", 
                                          email, fullName, 
                                          creditCard.substring(Math.max(0, creditCard.length()-4)), 
                                          cvv,
                                          isVulnerable ? "VULNERABLE" : "SECURE");
            SecurityLogger.logAction(sessionUserId, sessionUsername, userId, 
                                    "UPDATE_PROFILE", ipAddress, details);
            
            // Log IDOR if updating someone else's profile in vulnerable mode
            if (isVulnerable && !sessionUserId.equals(userId)) {
                SecurityLogger.logIDORAttempt(sessionUserId, sessionUsername, userId, 
                                             "UPDATE_PROFILE", ipAddress);
            }
            
            response.setContentType("application/json");
            if (rowsAffected > 0) {
                response.getWriter().write("{\"success\":true}");
            } else {
                response.getWriter().write("{\"success\":false}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false}");
        }
    }
}