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
