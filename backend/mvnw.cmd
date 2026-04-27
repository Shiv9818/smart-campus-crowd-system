@echo off
setlocal

set "DIRNAME=%~dp0"
if "%DIRNAME%" == "" set "DIRNAME=."

@REM Remove trailing backslash from DIRNAME for property use
set "APP_HOME=%DIRNAME%"
if "%APP_HOME:~-1%"=="\" set "APP_HOME=%APP_HOME:~0,-1%"

@REM Find java.exe
if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

@REM Check if java is working
"%JAVA_EXE%" -version >NUL 2>&1
if "%ERRORLEVEL%" neq "0" (
    echo ERROR: java command not found. Please ensure Java is installed and in your PATH.
    exit /b 1
)

set "WRAPPER_JAR=%DIRNAME%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_CLASS=org.apache.maven.wrapper.MavenWrapperMain"

@REM Execute Maven Wrapper
@REM Note: Using %DIRNAME% for file paths and %APP_HOME% for properties to avoid escaping issues
"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%APP_HOME%" %WRAPPER_CLASS% %*

if "%ERRORLEVEL%" neq "0" (
    exit /b %ERRORLEVEL%
)

endlocal
