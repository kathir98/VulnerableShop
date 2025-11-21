<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Register - ShopHub</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="container">
        <h1>Create Your Account</h1>
        
        <% if (request.getParameter("error") != null) { %>
            <p class="error">Registration failed. Username may already be taken.</p>
        <% } %>
        
        <form action="register" method="post">
            <div class="form-group">
                <label>Username:</label>
                <input type="text" name="username" required>
            </div>
            
            <div class="form-group">
                <label>Password:</label>
                <input type="password" name="password" required>
            </div>
            
            <div class="form-group">
                <label>Email:</label>
                <input type="email" name="email" required>
            </div>
            
            <div class="form-group">
                <label>Full Name:</label>
                <input type="text" name="fullName" required>
            </div>
            
            <div class="form-group">
                <label>Shipping Address:</label>
                <textarea name="address" required></textarea>
            </div>
            
            <div class="form-group">
                <label>Credit Card Number:</label>
                <input type="text" name="creditCard" maxlength="16" required>
            </div>
            
            <div class="form-group">
                <label>CVV:</label>
                <input type="text" name="cvv" maxlength="4" required>
            </div>
            
            <button type="submit" class="btn">Create Account</button>
        </form>
        
        <p>Already have an account? <a href="login.jsp">Login here</a></p>
    </div>
</body>
</html>