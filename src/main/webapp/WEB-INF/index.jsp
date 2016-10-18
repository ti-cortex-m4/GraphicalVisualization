<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Webxen</title>
</head>
<body>
	<h1>${msg}</h1>
	<h3>Please Login to Continue</h3>
	<form action="/WebxenBNY/login" method="post">
		Username: <input type="text" name="userName" /> <br /> <br />
		Password: <input type="password" name="password" /> <br />
		<input type="submit" value="submit" />
	</form>
	<a href = "/WebxenBNY/checkNeo">check neo</a>
</body>
</html>