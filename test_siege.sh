# WIP: refining test_siege.sh
#!/bin/bash

# Define colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "=========================================="
echo "    JAVA LOCALSERVER SIEGE STRESS TEST    "
echo "=========================================="

if ! command -v siege &> /dev/null; then
    echo -e "${RED}Siege is not installed. Please install it with 'brew install siege'.${NC}"
    exit 1
fi

if ! pgrep -f "java -cp out Main" > /dev/null; then
    echo -e "${RED}Server is not running. Please start it with 'java -cp out Main'${NC}"
    exit 1
fi

echo "Running Siege: 50 concurrent users for 30 seconds..."
siege -b -c 50 -t 30S http://localhost:8080/
