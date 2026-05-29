@rem Gradle startup script for Windows
@if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%%~dp0
if "%%DIRNAME%%"=="" set DIRNAME=.
set APP_BASE_NAME=%%~nx0
set DEFAULT_JVM_OPTS=

set CLASSPATH=%%DIRNAME%%gradle\wrapper\gradle-wrapper.jar

"%%JAVA_HOME%%/bin/java.exe" %%DEFAULT_JVM_OPTS%% -classpath "%%CLASSPATH%%" org.gradle.wrapper.GradleWrapperMain %%*
