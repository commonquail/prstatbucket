<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Unresolved Reviews</title>
</head>
<body>
<table>
    <tr>
        <th>Destination</th>
        <th>Title</th>
        <th>Age</th>
    <tr>
    <#list pullRequests as pr>
    <tr>
        <td>${pr.destination}</td>
        <td>${pr.title}</td>
        <td>${pr.age}</td>
    </tr>
    </#list>
</table>
</body>
</html>
