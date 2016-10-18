<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Webxen</title>
</head>
<body>

${msg }
<br/>

<form action = "/WebxenBNY/getGraph" method = "post">
Type of element: <input type = "text" name = "type"/><br/>
ID: <input type = "text" name = "id"/><br/>
Type of hierarchy<input type = "text" name = "typeHierarrchy" value = "Risk" readonly="readonly"/><br/>
Type of exposure<input type = "text" name = "typeExposure"/><br/>
Minimum Exposure<input type = "text" name = "minExposure"/><br/>
Maximum Exposure<input type = "text" name = "maxExposure"/><br/>
Date: <input type = "text" name = "date"/><br/>
<input type = "submit"/><br/>

</form>

</body>
</html>