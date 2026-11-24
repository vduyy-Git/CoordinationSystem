# Script khoi dong tu dong toan bo he thong CoordSystem (Backend + Web)
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " 🚀 KHỞI ĐỘNG HỆ THỐNG ĐIỀU PHỐI AN SINH XÃ HỘI (COORDSYSTEM)" -ForegroundColor Yellow
Write-Host " 🏛️ Địa bàn: Quận Ngũ Hành Sơn, TP. Đà Nẵng" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan

# 1. Cấu hình Java 25
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# 2. Khởi động Backend Spring Boot trong cửa sổ riêng
Write-Host "`n[1/2] Đang khởi động Backend Spring Boot API (http://localhost:8080)..." -ForegroundColor Yellow
$backendDir = Join-Path $PSScriptRoot "backend"
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "cd '$backendDir'; `$env:JAVA_HOME='$env:JAVA_HOME'; `$env:Path=`$env:JAVA_HOME + '\bin;' + `$env:Path; .\mvnw.cmd spring-boot:run"

# 3. Mở Web Frontend trên trình duyệt mặc định
Write-Host "[2/2] Đang mở giao diện Cổng Thông Tin trên trình duyệt..." -ForegroundColor Green
Start-Sleep -Seconds 3
$frontendPath = Join-Path $PSScriptRoot "frontend\index.html"
Start-Process $frontendPath

Write-Host "`n✅ ĐÃ KHỞI CHẠY TOÀN BỘ HỆ THỐNG!" -ForegroundColor Cyan
Write-Host "   - Backend API:  http://localhost:8080/api" -ForegroundColor White
Write-Host "   - Swagger Docs: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "   - Web Frontend: $frontendPath`n" -ForegroundColor White
