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
        <th>Comments</th>
        <th>Tasks</th>
    </tr>
    <#list pullRequests as pr>
    <tr>
        <td>${pr.destination}</td>
        <td><a href="${pr.url}">${pr.title}</a></td>
        <td>${pr.age}</td>
        <td>${pr.commentCount}</td>
        <td>${pr.taskCount}</td>
    </tr>
    </#list>
</table>
</body>
</html>
