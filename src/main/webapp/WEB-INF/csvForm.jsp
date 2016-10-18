<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Welcome to Webxen</title>
</head>
<body>

	<form action="/WebxenBNY/generateCsv" method = "post">
	<input type = "text" name = "directoryPath"/> 
	<input type = "submit" value = "Generate CSV"/>
	</form>

</body>
</html>