#!/bin/bash

# Default values
VERSION="1.0.0"
QUIET=false
VERBOSE=false

# Function to display usage
usage() {
    echo "Usage: $0 [options]"
    echo
    echo "Options:"
    echo "  -e, --email      Your BambuLab email"
    echo "  -p, --password   Your BambuLab password"
    echo "  -c, --code       Verification code from your email (required in quiet mode)"
    echo "  -q, --quiet      Enable quiet mode (only output access token)"
    echo "  -v, --version    Display the version of this tool"
    echo "  -V, --verbose    Enable verbose output for debugging"
    echo "  -h, --help       Display this help message"
    exit 1
}

# Dependency check
if ! command -v curl &> /dev/null; then
    echo "Error: curl is not installed. Please install it to use this script." >&2
    exit 1
fi

# Parse command-line arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -e|--email) EMAIL="$2"; shift ;;
        -p|--password) PASSWORD="$2"; shift ;;
        -c|--code) CODE="$2"; shift ;;
        -q|--quiet) QUIET=true ;;
        -v|--version)
            echo "Version: $VERSION"
            exit 0
            ;;
        -V|--verbose) VERBOSE=true ;;
        -h|--help) usage ;;
        *)
            echo "Unknown parameter passed: $1" >&2
            usage
            ;;
    esac
    shift
done

# Check for conflicting arguments
if [ "$QUIET" = true ] && [ "$VERBOSE" = true ]; then
    echo "Error: --quiet and --verbose options are mutually exclusive." >&2
    usage
fi

# Prompt for email and password if not provided via arguments
if [ -z "$EMAIL" ]; then
    if [ "$QUIET" = true ]; then
        echo "Error: --email is required in quiet mode." >&2
        usage
    fi
    read -r -p "Enter your BambuLab email: " EMAIL
fi

if [ -z "$PASSWORD" ]; then
    if [ "$QUIET" = true ]; then
        echo "Error: --password is required in quiet mode." >&2
        usage
    fi
    read -r -s -p "Enter your BambuLab password: " PASSWORD
    echo
fi

# Final check to ensure we have the credentials
if [ -z "$EMAIL" ] || [ -z "$PASSWORD" ]; then
    echo "Email and password are required." >&2
    usage
fi

# Step 1: Login with email and password
if [ "$QUIET" = false ]; then
    echo "🔒 Step 1: Logging in with email and password..."
fi

login_payload=$(printf '{"account": "%s", "password": "%s"}' "$EMAIL" "$PASSWORD")

if [ "$QUIET" = false ] && [ "$VERBOSE" = true ]; then
    echo "Request to BambuLab API:"
    printf 'curl -s -X POST "https://api.bambulab.com/v1/user-service/user/login" -H "Content-Type: application/json" -d %s\n' "'$(printf '{\"account\": \"%s\", \"password\": \"*****\"}' "$EMAIL")'"
fi

response=$(curl -s -X POST "https://api.bambulab.com/v1/user-service/user/login" \
     -H "Content-Type: application/json" \
     -d "$login_payload")

if [ "$QUIET" = false ] && [ "$VERBOSE" = true ]; then
    echo "Response from BambuLab API:"
    echo "$response"
fi

# Prompt for the verification code
if [ -z "$CODE" ]; then
    if [ "$QUIET" = true ]; then
        echo "Error: --code is required in quiet mode." >&2
        usage
    fi
    read -r -p "📧 Enter the verification code from your email: " CODE
fi

# Step 2: Confirm login with token from email
if [ "$QUIET" = false ]; then
    echo "🔐 Step 2: Confirming login with the verification code..."
fi

token_payload=$(printf '{"account": "%s", "code": "%s"}' "$EMAIL" "$CODE")

if [ "$QUIET" = false ] && [ "$VERBOSE" = true ]; then
    echo "Request to BambuLab API:"
    printf 'curl -s -X POST "https://api.bambulab.com/v1/user-service/user/login" -H "Content-Type: application/json" -d %s\n' "'$(printf '{\"account\": \"%s\", \"code\": \"%s\"}' "$EMAIL" "$CODE")'"
fi

response=$(curl -s -X POST "https://api.bambulab.com/v1/user-service/user/login" \
     -H "Content-Type: application/json" \
     -d "$token_payload")

if [ "$QUIET" = false ] && [ "$VERBOSE" = true ]; then
    echo "Response from BambuLab API:"
    echo "$response"
fi

# Extract and display the access token
if command -v jq &> /dev/null; then
    access_token=$(echo "$response" | jq -r .accessToken)
else
    access_token=$(echo "$response" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
fi

if [ -n "$access_token" ] && [ "$access_token" != "null" ]; then
    if [ "$QUIET" = true ]; then
        echo "$access_token"
    else
        echo "✅ Your access token is: $access_token"
    fi
else
    if [ "$QUIET" = false ]; then
        echo "❌ Failed to retrieve the access token. Please check your credentials and the verification code." >&2
        if [ "$VERBOSE" = false ]; then
            echo "💡 For more details, try running with the --verbose flag." >&2
        fi
    fi
    exit 1
fi
