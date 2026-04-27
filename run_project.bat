@echo off

echo Starting Backend...
start cmd /k "cd backend && .\mvnw.cmd spring-boot:run"

timeout /t 8

echo Starting Frontend...
start cmd /k "cd frontend && npm start"