@echo off
chcp 65001 > nul
echo ========================================================
echo  🚀 HE THONG DIEU PHOI CUU TRO & AN SINH XA HOI (COORDSYSTEM)
echo  🏛️ Dia ban: Quan Ngu Hanh Son, TP. Da Nang
echo ========================================================
echo.
echo [1/3] Kiem tra moi truong Java...
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [2/3] Dang khoi dong Backend Spring Boot (http://localhost:8080)...
start "CoordSystem Spring Boot Backend" cmd /c "cd /d %~dp0backend && .\mvnw.cmd spring-boot:run"

echo [3/3] Dang mo Cong Thong Tin Frontend tren trinh duyet...
timeout /t 4 /nobreak > nul
start "" "%~dp0frontend\index.html"

echo.
echo ========================================================
echo  ✅ HE THONG DA DUOC KHOI DONG THANH CONG!
echo  - Backend API:  http://localhost:8080/api
echo  - Swagger UI:   http://localhost:8080/swagger-ui.html
echo  - Web Frontend: Da mo tren trinh duyet
echo ========================================================
exit
