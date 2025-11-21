package com.vulnerable.shop;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
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
                                        "VIEW_PROFILE (BLOCKED)", ipAddress, 
                                        "IDOR attempt blocked - secure mode enabled");
                
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Access denied. You can only view your own profile.\"}");
                return;
            }
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT username, email, full_name, address, credit_card, cvv FROM users WHERE user_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(userIdParam));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Log the profile view action
                String details = "Viewed profile data";
                SecurityLogger.logAction(sessionUserId, sessionUsername, userIdParam, 
                                        "VIEW_PROFILE", ipAddress, details);
                
                // Log IDOR if accessing someone else's profile in vulnerable mode
                if (isVulnerable && !sessionUserId.equals(userIdParam)) {
                    SecurityLogger.logIDORAttempt(sessionUserId, sessionUsername, userIdParam, 
                                                 "VIEW_PROFILE", ipAddress);
                }
                
                JSONObject json = new JSONObject();
                json.put("username", rs.getString("username"));
                json.put("email", rs.getString("email"));
                json.put("fullName", rs.getString("full_name"));
                json.put("address", rs.getString("address"));
                json.put("creditCard", rs.getString("credit_card"));
                json.put("cvv", rs.getString("cvv"));
                
                response.setContentType("application/json");
                response.getWriter().write(json.toString());
            } else {
                response.setStatus(404);
                response.getWriter().write("{\"error\":\"User not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"Server error\"}");
        }
    }
}