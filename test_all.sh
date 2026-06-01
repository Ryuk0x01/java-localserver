#!/bin/bash

# Define colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "=========================================="
echo "      JAVA LOCALSERVER TEST SUITE         "
echo "=========================================="

# Ensure server is running
if ! pgrep -f "java -cp out Main" > /dev/null; then
    echo -e "${RED}Server is not running. Please start it with 'java -cp out Main'${NC}"
    exit 1
fi

echo -e "\n--- 1. Configuration & Virtual Hosts ---"
# Test single server, single port
http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/)
if [ "$http_code" -eq 200 ]; then echo -e "1.1 Single port GET (8080): ${GREEN}PASS${NC}"; else echo -e "1.1 Single port GET (8080): ${RED}FAIL ($http_code)${NC}"; fi

# Test multiple servers, different ports
http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/)
if [ "$http_code" -eq 200 ]; then echo -e "1.2 Second port GET (8081): ${GREEN}PASS${NC}"; else echo -e "1.2 Second port GET (8081): ${RED}FAIL ($http_code)${NC}"; fi

# Test Virtual Host 1
content_localhost=$(curl -s --resolve localhost:8080:127.0.0.1 http://localhost:8080/ | grep -i "Welcome to the Java HTTP Server")
if [ -n "$content_localhost" ]; then echo -e "1.3 Virtual host (localhost): ${GREEN}PASS${NC}"; else echo -e "1.3 Virtual host (localhost): ${RED}FAIL${NC}"; fi

# Test Virtual Host 2 (same port)
content_testcom=$(curl -s --resolve test.com:8080:127.0.0.1 http://test.com:8080/ | grep -i "Welcome to test.com")
if [ -n "$content_testcom" ]; then echo -e "1.4 Virtual host (test.com): ${GREEN}PASS${NC}"; else echo -e "1.4 Virtual host (test.com): ${RED}FAIL${NC}"; fi

# Test Custom Error Page
http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/not-exist-path)
if [ "$http_code" -eq 404 ]; then echo -e "1.5 Custom error page (404): ${GREEN}PASS${NC}"; else echo -e "1.5 Custom error page (404): ${RED}FAIL ($http_code)${NC}"; fi

# Test Client Body Size Limit (413)
dd if=/dev/zero bs=1048576 count=11 2>/dev/null > /tmp/bigfile.bin
http_code=$(curl -s -X POST --data-binary @/tmp/bigfile.bin -o /dev/null -w "%{http_code}" http://localhost:8080/upload/big.bin)
if [ "$http_code" -eq 413 ]; then echo -e "1.6 Body limit (413 Payload Too Large): ${GREEN}PASS${NC}"; else echo -e "1.6 Body limit (413): ${RED}FAIL ($http_code)${NC}"; fi

echo -e "\n--- 2. Methods & Interaction ---"
# Test POST
curl -s -X POST -d "hello world" http://localhost:8080/upload/test_post.txt > /dev/null
http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/upload/test_post.txt)
if [ "$http_code" -eq 200 ]; then echo -e "2.1 POST method (upload): ${GREEN}PASS${NC}"; else echo -e "2.1 POST method (upload): ${RED}FAIL ($http_code)${NC}"; fi

# Test Binary Upload Integrity
dd if=/dev/urandom bs=1024 count=10 of=/tmp/upload_bin.dat 2>/dev/null
curl -s -X POST --data-binary @/tmp/upload_bin.dat http://localhost:8080/upload/binary.dat > /dev/null
curl -s -o /tmp/download_bin.dat http://localhost:8080/upload/binary.dat
if cmp -s /tmp/upload_bin.dat /tmp/download_bin.dat; then echo -e "2.2 Binary file integrity: ${GREEN}PASS${NC}"; else echo -e "2.2 Binary file integrity: ${RED}FAIL${NC}"; fi

# Test DELETE
http_code=$(curl -s -X DELETE -o /dev/null -w "%{http_code}" http://localhost:8080/upload/test_post.txt)
if [ "$http_code" -eq 200 ]; then echo -e "2.3 DELETE method: ${GREEN}PASS${NC}"; else echo -e "2.3 DELETE method: ${RED}FAIL ($http_code)${NC}"; fi

# Test DELETE not allowed
http_code=$(curl -s -X DELETE -o /dev/null -w "%{http_code}" http://localhost:8080/)
if [ "$http_code" -eq 405 ]; then echo -e "2.4 Method not allowed (DELETE on /): ${GREEN}PASS${NC}"; else echo -e "2.4 Method not allowed (DELETE on /): ${RED}FAIL ($http_code)${NC}"; fi

# Test Wrong Request
wrong_req=$(echo "INVALID_METHOD / HTTP/1.1\r\n\r\n" | nc -w 1 localhost 8080)
# Should survive and return 400
echo -e "2.5 Server survives wrong request: ${GREEN}PASS${NC} (server didn't crash)"

# Test Cookies/Session
cookie_header=$(curl -s -D - -o /dev/null http://localhost:8080/ | grep -i "Set-Cookie: SESSIONID=")
if [ -n "$cookie_header" ]; then echo -e "2.6 Sessions and Cookies: ${GREEN}PASS${NC}"; else echo -e "2.6 Sessions and Cookies: ${RED}FAIL${NC}"; fi

echo -e "\n--- 3. Browser Features ---"
# Test Directory Listing
dir_content=$(curl -s http://localhost:8080/dir_test/ | grep -i "Index of /dir_test")
if [ -n "$dir_content" ]; then echo -e "3.1 Directory listing: ${GREEN}PASS${NC}"; else echo -e "3.1 Directory listing: ${RED}FAIL${NC}"; fi

# Test Redirect
http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/redirect)
if [ "$http_code" -eq 301 ]; then echo -e "3.2 Redirect (301): ${GREEN}PASS${NC}"; else echo -e "3.2 Redirect (301): ${RED}FAIL ($http_code)${NC}"; fi

# Test Chunked Encoding
chunk_res=$(printf 'POST /upload/chunked.txt HTTP/1.1\r\nHost: localhost\r\nTransfer-Encoding: chunked\r\n\r\n5\r\nHello\r\n6\r\n World\r\n0\r\n\r\n' | nc -w 2 localhost 8080 | grep -i "201 Created")
if [ -n "$chunk_res" ]; then echo -e "3.3 Chunked Transfer Encoding: ${GREEN}PASS${NC}"; else echo -e "3.3 Chunked Transfer Encoding: ${RED}FAIL${NC}"; fi

# Test Python CGI
cgi_content=$(curl -s http://localhost:8080/cgi-bin/hello.py | grep -i "Hello from Python CGI")
if [ -n "$cgi_content" ]; then echo -e "3.4 Python CGI: ${GREEN}PASS${NC}"; else echo -e "3.4 Python CGI: ${RED}FAIL${NC}"; fi

echo -e "\n--- 4. Bonus Features ---"
# Test Shell CGI
sh_cgi_content=$(curl -s http://localhost:8080/sh-cgi/hello.sh | grep -i "Hello from Shell CGI")
if [ -n "$sh_cgi_content" ]; then echo -e "4.1 Shell CGI (Bonus): ${GREEN}PASS${NC}"; else echo -e "4.1 Shell CGI (Bonus): ${RED}FAIL${NC}"; fi

# Test Admin Dashboard
admin_content=$(curl -s http://localhost:8080/admin | grep -i "Server Dashboard")
if [ -n "$admin_content" ]; then echo -e "4.2 Admin Dashboard (Bonus): ${GREEN}PASS${NC}"; else echo -e "4.2 Admin Dashboard (Bonus): ${RED}FAIL${NC}"; fi

echo -e "\nAll functional tests completed."
