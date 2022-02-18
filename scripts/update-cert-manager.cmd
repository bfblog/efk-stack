SET SCRIPT_DIR=%~dp0
SET ROOT_DIR=%SCRIPT_DIR%\..

SET VERSION=v1.7.1

mkdir %ROOT_DIR%\third-party\cert-manager\%VERSION%
curl.exe -L https://github.com/cert-manager/cert-manager/releases/download/%VERSION%/cert-manager.yaml --output %ROOT_DIR%/third-party/cert-manager/%VERSION%/release.yaml
