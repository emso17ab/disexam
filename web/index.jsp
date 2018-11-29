<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Simple Form</title>
  <style>
    #softuni-login-form {
      width: 400px;
      height: 320px;
      background-color: #EEEEEE;
      font-family: tahoma;
    }

    header h3 {
      color: #1C3E60;
    }
    header p {
      font-size: 75%;
    }
    form {
      display: inline-block;
      float: left;
      width: 250px;
      height: 213px;
      padding-top: 10px;
      color: #5F5F5F;
    }
    form .form-header {
      text-transform: uppercase;
      margin-left: 10px;
      font-weight: bold;
      font-size: 80%;
    }
    form .form-header #reg {
      color: #16395C;
    }
    form .input-text {
      width: 90%;
      height: 11%;
      margin: 10px 10px 0;
    }
    form #reg-button {
      border: 2px solid #264767;
      height: 13%;
      width: 45%;
      color: #264767;
      float: right;
      margin-right: 10px;
      margin-top: 10px;
    }
    #login-form {
      background-color: #234465;
      color: white;
      width: 350px;
      height: 220px;
      align-items: center;
    }
    #login-form #remember {
      display: inline;
      font-size: 85%;
    }
    #login-form #remember input {
      margin-top: 10px;
      margin-left: 10px;
    }
    #login-form #login-button {
      border: 2px solid #FD9B01;
      color: white;
      background-color: #234465;
      display: inline;
      float: right;
      margin-top: 10px;
      margin-right: 10px;
      width: 40%;
      height: 13%;
    }
    #login-form #forgot-pass {
      float: right;
      color: white;
      margin-right: 15px;
      font-size: 70%;
    }
  </style>

</head>
<body>
<div id="softuni-login-form">
  <header>
    <h3>Enter the system</h3>
    <p>It is necessary to login to Your account in order to use the webshop.</p>
  </header>
  <form action="user/login" method="post" id="login-form">
    <span class="form-header">Login</span>
    <br><input type="text" placeholder="User name" name="username" required class="input-text"/>
    <br><input type="password" placeholder="Password" name="password" required class="input-text"/>
    <input type="submit" value="Login" id="login-button"/>
  </form>
</div>
</body>
</html>