#!/bin/bash
APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
server=$(curl -s https://api.gofile.io/servers | jq -r '.data.servers[0].name')
echo "Uploading to Gofile..."
curl -F "file=@$APK_PATH" https://${server}.gofile.io/uploadFile | jq -r '.data.downloadPage'
