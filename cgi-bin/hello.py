#!/usr/bin/env python3
import os
import datetime

print("Content-Type: text/html")
print()
print("<html><body>")
print("<h1>CGI Script Output</h1>")
print("<p>Hello from Python CGI!</p>")
print(f"<p>Time: {datetime.datetime.now()}</p>")
print(f"<p>Method: {os.environ.get('REQUEST_METHOD', 'N/A')}</p>")
print(f"<p>Path: {os.environ.get('PATH_INFO', 'N/A')}</p>")
print(f"<p>Query: {os.environ.get('QUERY_STRING', 'N/A')}</p>")
print("</body></html>")
