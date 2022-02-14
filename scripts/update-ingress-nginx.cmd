SET SCRIPT_DIR=%~dp0
SET ROOT_DIR=%SCRIPT_DIR%\..

SET VERSION=v1.1.1

mkdir %ROOT_DIR%\third-party\ingress-nginx\%VERSION%
curl.exe -L https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-%VERSION%/deploy/static/provider/aws/deploy.yaml --output %ROOT_DIR%/third-party/ingress-nginx/%VERSION%/install.yaml
