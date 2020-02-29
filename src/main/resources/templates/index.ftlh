<!DOCTYPE html>
<html lang="en">
<head>
  <#import "spring.ftl" as spring />
  <#assign unresolved = "/unresolved">
  <#assign duration_density = "/duration/density">
  <meta charset="UTF-8">
  <title>Home</title>
</head>
<body>
<ul>
  <li><a href="<@spring.url unresolved/>">Unresolved pull requests</a></li>
  <li><a href="<@spring.url duration_density/>">Density estimate report of durations</a></li>
</ul>
</body>
</html>
