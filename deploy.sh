#!/bin/bash

# EC2 Deployment Script for Pet Tracker Review Application
# This script handles the deployment of the Spring Boot application on EC2

set -e  # Exit on any error

APP_NAME="pet-tracker-review"
APP_DIR="/home/project/affiliate"
JAR_NAME="pet-tracker-review.jar"
LOG_DIR="$APP_DIR/logs"
LOG_FILE="$LOG_DIR/app.log"
PID_FILE="$APP_DIR/$APP_NAME.pid"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if application is running
is_running() {
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null 2>&1; then
            return 0
        else
            rm -f "$PID_FILE"
            return 1
        fi
    fi
    return 1
}

# Function to stop the application
stop_app() {
    print_status "Stopping $APP_NAME..."
    
    if is_running; then
        local pid=$(cat "$PID_FILE")
        print_status "Killing process $pid"
        kill "$pid"
        
        # Wait for process to stop
        local count=0
        while [ $count -lt 30 ] && ps -p "$pid" > /dev/null 2>&1; do
            sleep 1
            count=$((count + 1))
        done
        
        if ps -p "$pid" > /dev/null 2>&1; then
            print_warning "Process didn't stop gracefully, force killing..."
            kill -9 "$pid"
        fi
        
        rm -f "$PID_FILE"
        print_status "Application stopped successfully"
    else
        print_warning "Application is not running"
    fi
}

# Function to start the application
start_app() {
    print_status "Starting $APP_NAME..."
    
    if is_running; then
        print_error "Application is already running (PID: $(cat $PID_FILE))"
        return 1
    fi
    
    # Create necessary directories
    mkdir -p "$LOG_DIR"
    mkdir -p "$APP_DIR/uploads/images"
    mkdir -p "$APP_DIR/uploads/metadata"
    
    # Check if JAR file exists
    if [ ! -f "$APP_DIR/$JAR_NAME" ]; then
        print_error "JAR file not found: $APP_DIR/$JAR_NAME"
        return 1
    fi
    
    # Check Java version
    print_status "Java version:"
    java -version
    
    # Start application
    cd "$APP_DIR"
    nohup java -jar "$JAR_NAME" \
        --spring.profiles.active=prod \
        --server.port=8089 \
        > "$LOG_FILE" 2>&1 &
    
    local pid=$!
    echo "$pid" > "$PID_FILE"
    
    print_status "Application started with PID: $pid"
    
    # Wait a moment and check if it's still running
    sleep 5
    if is_running; then
        print_status "✅ Application is running successfully on port 8089"
        return 0
    else
        print_error "❌ Application failed to start"
        print_error "Check log file: $LOG_FILE"
        return 1
    fi
}

# Function to check application status
status_app() {
    if is_running; then
        local pid=$(cat "$PID_FILE")
        print_status "Application is running (PID: $pid)"
        
        # Try to check health endpoint
        if command -v curl > /dev/null 2>&1; then
            if curl -f http://localhost:8089/actuator/health > /dev/null 2>&1; then
                print_status "✅ Health check passed"
            else
                print_warning "⚠️  Health endpoint not responding"
            fi
        fi
    else
        print_warning "Application is not running"
    fi
}

# Function to show logs
show_logs() {
    if [ -f "$LOG_FILE" ]; then
        tail -f "$LOG_FILE"
    else
        print_error "Log file not found: $LOG_FILE"
    fi
}

# Function to restart the application
restart_app() {
    print_status "Restarting $APP_NAME..."
    stop_app
    sleep 2
    start_app
}

# Main script logic
case "$1" in
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        restart_app
        ;;
    status)
        status_app
        ;;
    logs)
        show_logs
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|logs}"
        echo ""
        echo "Commands:"
        echo "  start   - Start the application"
        echo "  stop    - Stop the application"
        echo "  restart - Restart the application"
        echo "  status  - Show application status"
        echo "  logs    - Show application logs (follow mode)"
        exit 1
        ;;
esac