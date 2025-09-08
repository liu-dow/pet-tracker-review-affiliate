#!/bin/bash

# EC2 Server Initialization Script for Pet Tracker Review Application
# Run this script on your EC2 instance to prepare the deployment environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_status "🚀 Initializing EC2 server for Pet Tracker Review deployment..."

# Update system packages
print_status "📦 Updating system packages..."
sudo apt update
sudo apt upgrade -y

# Install required packages
print_status "📥 Installing required packages..."
sudo apt install -y curl wget unzip htop tree

# Check if Java 8 is installed
print_status "☕ Checking Java installation..."
if java -version 2>&1 | grep -q "1.8.0"; then
    print_status "✅ Java 8 is already installed"
    java -version
else
    print_warning "Java 8 not found. Installing OpenJDK 8..."
    sudo apt install -y openjdk-8-jdk
    
    # Set JAVA_HOME
    echo 'export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64' >> ~/.bashrc
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
    
    print_status "✅ Java 8 installed successfully"
    java -version
fi

# Create application directory structure
print_status "📁 Creating application directory structure..."
sudo mkdir -p /home/project/affiliate
sudo chown -R $USER:$USER /home/project/affiliate
sudo chmod -R 755 /home/project/affiliate

cd /home/project/affiliate

# Create subdirectories
mkdir -p logs
mkdir -p uploads/images
mkdir -p uploads/metadata
mkdir -p backups

print_status "✅ Directory structure created:"
tree /home/project/affiliate 2>/dev/null || ls -la /home/project/affiliate

# Configure firewall for port 8089
print_status "🔥 Configuring firewall for port 8089..."
if command -v ufw > /dev/null 2>&1; then
    sudo ufw allow 8089/tcp
    print_status "✅ UFW firewall rule added for port 8089"
else
    print_warning "UFW not available, please manually configure firewall"
fi

# Create systemd service file for the application
print_status "⚙️  Creating systemd service file..."
sudo tee /etc/systemd/system/pet-tracker-review.service > /dev/null << 'EOF'
[Unit]
Description=Pet Tracker Review Application
After=network.target

[Service]
Type=simple
User=ubuntu
Group=ubuntu
WorkingDirectory=/home/project/affiliate
ExecStart=/usr/bin/java -jar /home/project/affiliate/pet-tracker-review.jar --spring.profiles.active=prod --server.port=8089
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=pet-tracker-review

[Install]
WantedBy=multi-user.target
EOF

# Reload systemd and enable service
sudo systemctl daemon-reload
print_status "✅ Systemd service created and enabled"

# Create helpful scripts
print_status "📝 Creating management scripts..."

# Create start script
cat > start-app.sh << 'EOF'
#!/bin/bash
echo "Starting Pet Tracker Review application..."
sudo systemctl start pet-tracker-review
sudo systemctl status pet-tracker-review
EOF

# Create stop script
cat > stop-app.sh << 'EOF'
#!/bin/bash
echo "Stopping Pet Tracker Review application..."
sudo systemctl stop pet-tracker-review
EOF

# Create status script
cat > status-app.sh << 'EOF'
#!/bin/bash
echo "Pet Tracker Review application status:"
sudo systemctl status pet-tracker-review --no-pager
echo ""
echo "Application logs (last 20 lines):"
tail -20 logs/application.log 2>/dev/null || echo "No log file found"
EOF

# Create logs script
cat > view-logs.sh << 'EOF'
#!/bin/bash
echo "Viewing Pet Tracker Review application logs..."
if [ -f logs/application.log ]; then
    tail -f logs/application.log
else
    echo "Following systemd logs..."
    sudo journalctl -u pet-tracker-review -f
fi
EOF

# Make scripts executable
chmod +x *.sh

print_status "✅ Management scripts created:"
ls -la *.sh

# Create log rotation configuration
print_status "📋 Setting up log rotation..."
sudo tee /etc/logrotate.d/pet-tracker-review > /dev/null << 'EOF'
/home/project/affiliate/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 ubuntu ubuntu
    postrotate
        /bin/systemctl reload pet-tracker-review > /dev/null 2>&1 || true
    endscript
}
EOF

print_status "✅ Log rotation configured"

# Install nginx (optional, for reverse proxy)
print_status "🌐 Installing Nginx for reverse proxy..."
sudo apt install -y nginx

# Create nginx configuration
sudo tee /etc/nginx/sites-available/pet-tracker-review > /dev/null << 'EOF'
server {
    listen 80;
    server_name _;
    
    # Redirect to HTTPS if needed
    # return 301 https://$server_name$request_uri;
    
    location / {
        proxy_pass http://localhost:8089;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeout settings
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Static files (if needed)
    location /static/ {
        alias /home/project/affiliate/static/;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
EOF

# Enable nginx site
sudo ln -sf /etc/nginx/sites-available/pet-tracker-review /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl enable nginx
sudo systemctl restart nginx

print_status "✅ Nginx configured and started"

# Display system information
print_status "📊 System Information:"
echo "OS: $(lsb_release -d | cut -f2)"
echo "Java: $(java -version 2>&1 | head -1)"
echo "Free disk space: $(df -h /home/project/affiliate | tail -1 | awk '{print $4}')"
echo "Free memory: $(free -h | grep '^Mem:' | awk '{print $7}')"

print_status "🎉 EC2 server initialization completed!"
print_status ""
print_status "📋 Next steps:"
print_status "1. Configure GitHub Secrets with your EC2 details"
print_status "2. Push your code to trigger GitHub Actions deployment"
print_status "3. Monitor deployment with: ./view-logs.sh"
print_status "4. Check application status with: ./status-app.sh"
print_status ""
print_status "🌐 Once deployed, your application will be available at:"
print_status "   - Direct: http://YOUR_EC2_IP:8089"
print_status "   - Via Nginx: http://YOUR_EC2_IP"
print_status ""
print_status "🔧 Management commands:"
print_status "   - Start: ./start-app.sh"
print_status "   - Stop: ./stop-app.sh"
print_status "   - Status: ./status-app.sh"
print_status "   - Logs: ./view-logs.sh"