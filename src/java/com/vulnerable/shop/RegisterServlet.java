package com.vulnerable.shop;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String address = request.getParameter("address");
        String creditCard = request.getParameter("creditCard");
        String cvv = request.getParameter("cvv");
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, email, full_name, address, credit_card, cvv) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, fullName);
            stmt.setString(5, address);
            stmt.setString(6, creditCard);
            stmt.setString(7, cvv);
            
            stmt.executeUpdate();
            response.sendRedirect("login.jsp?registered=1");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("register.jsp?error=1");
        }
    }
}