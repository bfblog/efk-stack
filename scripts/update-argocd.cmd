SET SCRIPT_DIR=%~dp0
SET ROOT_DIR=%SCRIPT_DIR%\..

SET VERSION=v2.2.5

mkdir %ROOT_DIR%\third-party\argocd\%VERSION%
curl.exe -L https://raw.githubusercontent.com/argoproj/argo-cd/%VERSION%/manifests/install.yaml --output %ROOT_DIR%/third-party/argocd/%VERSION%/install.yaml