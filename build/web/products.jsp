<%@ page import="java.sql.*" %>
<%@ page import="com.vulnerable.shop.DBConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    Integer userId = (Integer) session.getAttribute("userId");
    String username = (String) session.getAttribute("username");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Products - Vulnerable ShopHub</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="navbar">
        <div class="nav-container">
            <h2 class="logo">Vuln ShopHub</h2>
            <div class="nav-links">
                <span>Welcome, <%= username %></span>
                <a href="profile.jsp?userId=<%= userId %>">My Profile</a>
                <a href="logout.jsp">Logout</a>
            </div>
        </div>
    </div>
    
    <div class="container">        
        <div class="products-grid">
            <%
                try (Connection conn = DBConnection.getConnection()) {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM products");
                    
                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        String productName = rs.getString("name");
                        double price = rs.getDouble("price");
            %>
                        <div class="product-card">
                            <div class="product-image">
                                <img src="<%= rs.getString("image_url") %>" alt="<%= productName %>">
                            </div>
                            <div class="product-details">
                                <h3><%= productName %></h3>
                                <p class="description"><%= rs.getString("description") %></p>
                                <div class="product-footer">
                                    <span class="price">$<%= price %></span>
                                    <button class="btn-buy" onclick="buyProduct(<%= productId %>, <%= price %>)">Buy Now</button>
                                </div>
                            </div>
                        </div>
            <%
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            %>
        </div>
    </div>
    
    <script>
        const userId = <%= userId %>;
        
        function buyProduct(productId, price) {
            fetch('purchase', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'userId=' + userId + '&productId=' + productId + '&quantity=1&totalPrice=' + price
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    window.location.href = 'products.jsp?purchased=true';
                }
            });
        }
    </script>
    
    <% if (request.getParameter("purchased") != null) { %>
        <div class="notification">
            <p>Order placed successfully! Thank you for your purchase.</p>
        </div>
    <% } %>
</body>
</html>