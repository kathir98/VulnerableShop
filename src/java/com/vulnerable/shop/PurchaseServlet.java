package com.vulnerable.shop;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/purchase")
public class PurchaseServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String userId = request.getParameter("userId");
        String productId = request.getParameter("productId");
        String quantity = request.getParameter("quantity");
        String totalPrice = request.getParameter("totalPrice");
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO orders (user_id, product_id, quantity, total_price) VALUES (?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(userId));
            stmt.setInt(2, Integer.parseInt(productId));
            stmt.setInt(3, Integer.parseInt(quantity));
            stmt.setDouble(4, Double.parseDouble(totalPrice));
            
            stmt.executeUpdate();
            response.getWriter().write("{\"success\":true,\"message\":\"Order placed successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false,\"message\":\"Purchase failed\"}");
        }
    }
}