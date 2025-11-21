<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - ShopHub</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="container">
        <h1>Login to ShopHub</h1>
        
        <% if (request.getParameter("error") != null) { %>
            <p class="error">Invalid username or password. Please try again.</p>
        <% } %>
        
        <% if (request.getParameter("registered") != null) { %>
            <p class="success">Registration successful! Please login to continue.</p>
        <% } %>
        
        <form action="login" method="post">
            <div class="form-group">
                <label>Username:</label>
                <input type="text" name="username" required>
            </div>
            
            <div class="form-group">
                <label>Password:</label>
                <input type="password" name="password" required>
            </div>
            
            <button type="submit" class="btn">Login</button>
        </form>
        
        <p>Don't have an account? <a href="register.jsp">Register here</a></p>
    </div>
</body>
</html>