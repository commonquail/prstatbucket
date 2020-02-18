<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Unresolved Reviews</title>
    <style>
.number {
  text-align: right;
}
    </style>
</head>
<body>
<table>
    <tr>
        <th>Destination</th>
        <th>Title</th>
        <th>Age</th>
        <th>Comments</th>
        <th>Tasks</th>
        <th>Approvals</th>
    </tr>
    <#list pullRequests as pr>
    <tr>
        <td>${pr.destination}</td>
        <td><a href="${pr.url}">${pr.title}</a></td>
        <td>${pr.age}</td>
        <td class="number">${pr.commentCount}</td>
        <td class="number">${pr.taskCount}</td>
        <td class="number">${pr.approvalCount}</td>
    </tr>
    </#list>
</table>
</body>
</html>
