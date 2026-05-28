# WIP: refining hello.sh
#!/bin/sh
echo "Content-Type: text/html"
echo ""
echo "<html><body>"
echo "<h1>Shell CGI Script</h1>"
echo "<p>Hello from Shell CGI!</p>"
echo "<p>Method: $REQUEST_METHOD</p>"
echo "<p>Path: $PATH_INFO</p>"
echo "<p>Query: $QUERY_STRING</p>"
echo "<p>Date: $(date)</p>"
echo "</body></html>"
