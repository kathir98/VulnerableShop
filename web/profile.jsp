<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    
    String viewUserId = request.getParameter("userId");
    if (viewUserId == null) {
        viewUserId = session.getAttribute("userId").toString();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Profile - ShopHub</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="navbar">
        <div class="nav-container">
            <h2 class="logo">Vuln ShopHub</h2>
            <div class="nav-links">
                <a href="products.jsp">Products</a>
                <a href="logout.jsp">Logout</a>
            </div>
        </div>
    </div>
    
    <div class="container">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
            <h2 style="margin: 0;">My Profile</h2>
            <div class="vulnerability-toggle">
                <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
                    <input type="checkbox" id="vulnerableMode" onchange="toggleVulnerability()" checked>
                    <span style="font-size: 0.9rem; color: #4a5568;">Vulnerable Mode</span>
                </label>
            </div>
        </div>
        
        <div id="profileView">
            <div class="profile-card">
                <div id="profileData" class="profile-info"></div>
                <div class="profile-actions">
                    <button class="btn" onclick="showEditForm()">Edit Profile</button>
                    <button class="btn" onclick="showPasswordForm()">Change Password</button>
                    <button class="btn-danger" onclick="deleteAccount()">Delete Account</button>
                </div>
            </div>
        </div>
        
        <div id="editForm" class="profile-section" style="display:none;">
            <h2>Edit Profile Information</h2>
            <form id="updateForm">
                <div class="form-group">
                    <label>Email Address</label>
                    <input type="email" id="email" name="email" required>
                </div>
                <div class="form-group">
                    <label>Full Name</label>
                    <input type="text" id="fullName" name="fullName" required>
                </div>
                <div class="form-group">
                    <label>Shipping Address</label>
                    <textarea id="address" name="address" required></textarea>
                </div>
                <div class="form-group">
                    <label>Credit Card Number</label>
                    <input type="text" id="creditCard" name="creditCard" maxlength="16" required>
                </div>
                <div class="form-group">
                    <label>CVV</label>
                    <input type="text" id="cvv" name="cvv" maxlength="4" required>
                </div>
                <button type="button" class="btn" onclick="updateProfile()">Save Changes</button>
                <button type="button" class="btn-secondary" onclick="cancelEdit()">Cancel</button>
            </form>
        </div>
        
        <div id="passwordForm" class="profile-section" style="display:none;">
            <h2>Change Password</h2>
            <form id="changePasswordForm">
                <div class="form-group">
                    <label>New Password</label>
                    <input type="password" id="newPassword" name="newPassword" required>
                </div>
                <div class="form-group">
                    <label>Confirm New Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required>
                </div>
                <button type="button" class="btn" onclick="changePassword()">Update Password</button>
                <button type="button" class="btn-secondary" onclick="cancelPasswordChange()">Cancel</button>
            </form>
        </div>
    </div>
    
    <div id="notification" class="notification" style="display:none;"></div>
    
    <script>
        const currentUserId = '<%= viewUserId %>';
        const sessionUserId = '<%= session.getAttribute("userId") %>';
        let profileData = null;
        let vulnerableMode = true;
        
        function toggleVulnerability() {
            vulnerableMode = document.getElementById('vulnerableMode').checked;
            if (vulnerableMode) {
                showNotification('Vulnerable mode enabled - IDOR possible');
            } else {
                showNotification('Secure mode enabled - IDOR protection active');
            }
        }
        
        function loadProfile() {
            fetch('profile?userId=' + currentUserId + '&vulnerable=' + vulnerableMode)
                .then(res => res.json())
                .then(data => {
                    if (data.error) {
                        document.getElementById('profileData').innerHTML = '<p class="error">' + data.error + '</p>';
                    } else {
                        profileData = data;
                        document.getElementById('profileData').innerHTML = 
                            '<div class="info-row"><label>Username</label><span>' + data.username + '</span></div>' +
                            '<div class="info-row"><label>Email</label><span>' + data.email + '</span></div>' +
                            '<div class="info-row"><label>Full Name</label><span>' + data.fullName + '</span></div>' +
                            '<div class="info-row"><label>Shipping Address</label><span>' + data.address + '</span></div>' +
                            '<div class="info-row"><label>Payment Card</label><span>**** **** **** ' + data.creditCard.slice(-4) + '</span></div>' +
                            '<div class="info-row"><label>Card CVV</label><span>' + data.cvv + '</span></div>';
                    }
                })
                .catch(err => {
                    document.getElementById('profileData').innerHTML = '<p class="error">Unable to load profile</p>';
                });
        }
        
        function showEditForm() {
            if (!profileData) return;
            document.getElementById('email').value = profileData.email;
            document.getElementById('fullName').value = profileData.fullName;
            document.getElementById('address').value = profileData.address;
            document.getElementById('creditCard').value = profileData.creditCard;
            document.getElementById('cvv').value = profileData.cvv;
            
            document.getElementById('profileView').style.display = 'none';
            document.getElementById('editForm').style.display = 'block';
        }
        
        function cancelEdit() {
            document.getElementById('profileView').style.display = 'block';
            document.getElementById('editForm').style.display = 'none';
        }
        
        function updateProfile() {
            const formData = 
                'userId=' + currentUserId +
                '&email=' + encodeURIComponent(document.getElementById('email').value) +
                '&fullName=' + encodeURIComponent(document.getElementById('fullName').value) +
                '&address=' + encodeURIComponent(document.getElementById('address').value) +
                '&creditCard=' + encodeURIComponent(document.getElementById('creditCard').value) +
                '&cvv=' + encodeURIComponent(document.getElementById('cvv').value) +
                '&vulnerable=' + vulnerableMode;
            
            fetch('updateProfile', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    showNotification('Profile updated successfully');
                    cancelEdit();
                    loadProfile();
                } else {
                    showNotification(data.message || 'Failed to update profile');
                }
            });
        }
        
        function showPasswordForm() {
            document.getElementById('profileView').style.display = 'none';
            document.getElementById('passwordForm').style.display = 'block';
        }
        
        function cancelPasswordChange() {
            document.getElementById('profileView').style.display = 'block';
            document.getElementById('passwordForm').style.display = 'none';
            document.getElementById('changePasswordForm').reset();
        }
        
        function changePassword() {
            const newPass = document.getElementById('newPassword').value;
            const confirmPass = document.getElementById('confirmPassword').value;
            
            if (newPass !== confirmPass) {
                showNotification('Passwords do not match');
                return;
            }
            
            const formData = 
                'userId=' + currentUserId + 
                '&newPassword=' + encodeURIComponent(newPass) +
                '&vulnerable=' + vulnerableMode;
            
            fetch('changePassword', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    showNotification('Password changed successfully');
                    cancelPasswordChange();
                } else {
                    showNotification(data.message || 'Failed to change password');
                }
            });
        }
        
        function deleteAccount() {
            const formData = 'userId=' + currentUserId + '&vulnerable=' + vulnerableMode;
            
            fetch('deleteAccount', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    window.location.href = 'login.jsp';
                } else {
                    showNotification(data.message || 'Failed to delete account');
                }
            });
        }
        
        function showNotification(message) {
            const notif = document.getElementById('notification');
            notif.textContent = message;
            notif.style.display = 'block';
            setTimeout(() => {
                notif.style.display = 'none';
            }, 3000);
        }
        
        loadProfile();
    </script>
</body>
</html>