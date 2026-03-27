# Проект по развертыванию сервисов на spring boot в k8s.
Сервисы взаимодействуют друг с другом через rest с использование basic авторизации.

### Для запуска в кубере (minikube): 
- Настроить сам кластер [Minikube_config.md](../Minikube_config.md)
- Развернуть деплоймент [deployment.yml](spec/deployment.yml)  `kubectl apply -f deployment.yml`
- После этого при открытии туннуле `minikube tunnel` можно постучаться в кластер до сервиса через его LoadBalancer. эндпоинты
    ```shell
    ### Обращение напрямую к rest-service
    GET http://localhost:9080/rest-service/inner/test
    ### rest-service обращается внтури кластера к db-service
    GET http://localhost:9080/rest-service/db/test
    ```
  
### Настройка ingress
- Для настройки istioIngress использовать [istio_in_minikube.md](../istio/istio_in_minikube.md)
- Развернуть gateway, virtualService istio `kubectl apply -f ingress.yml`
- Включить форвардинг портов с локальной машины до кластера `port-forward svc/istio-ingressgateway -n istio-system 8080:80`
- Теперь сервис должен быть доступен на порту 8080
  ```shell
  evggorainov@MacBook-Air-Evgenij jetbra % curl  http://localhost:8080/rest-service/db/test 
  {"id":"ded793d7-ea99-46e1-bb5d-4367ef205b26","data":"Hello from db service"}%   
  ```
  
### Добавление возможности доступа по доменному имени
- Для того, чтобы можно было обратиться к сервисам по доменному имени для gateway нужно добавить имя хоста, который он будет обслуживать. Выполнить перезагрузку конфигурации
  ```yaml
  apiVersion: networking.istio.io/v1beta1
  kind: Gateway
  metadata:
    name: my-gateway
  spec:
    selector:
      istio: ingressgateway
    servers:
      - port:
          number: 80
          name: http
          protocol: HTTP
        hosts:
          - "*"
          - myapp.com
  ```
- Добавить myapp.com в hosts чтобы он резолвился на localhost `sudo vi /etc/hosts`
  ```shell
  127.0.0.1       myapp.local
  ```
- Включить форвардинг портов с локальной машины до кластера `port-forward svc/istio-ingressgateway -n istio-system 8080:80` Теперь севис будет доступен по доменному имени
  ```shell
  evggorainov@MacBook-Air-Evgenij jetbra % curl http://myapp.local:8080/rest-service/db/test 
  {"id":"b1f19808-e122-4008-97e9-5e8a78f41fbd","data":"Hello from db service"}% 
  ```

### Istio + https
Когда зашифрованный запрос приходит на порт 443 istio-ingressgateway, происходит следующее:

- `TLS Termination (Расшифровка)`: Ingress Gateway снимает (терминирует) TLS-шифрование. Для этого ему нужны сертификат (tls.crt) и ключ (tls.key).

- `Маршрутизация`: После расшифровки трафик становится обычным HTTP, и Gateway смотрит на заголовок Host, чтобы понять, какому VirtualService его отдать.

- `Хранение секретов`: Сертификаты хранятся в Kubernetes в виде специального Secret'а типа tls. Istio Gateway умеет читать эти секреты и автоматически подкладывать их в свои конфигурации

Настройка https:
1) Генерируем сертификат и закрытый ключ
  ```shell
    openssl req -x509 -nodes -days 1000 -newkey rsa:2048 \
    -keyout tls.key \
    -out tls.crt \  
    -subj "/CN=myapp.local/O=myapp" \
    -addext "subjectAltName = DNS:myapp.local"
  ```
  - req -x509: создаем самоподписанный сертификат X.509.
  
  - nodes: не шифровать приватный ключ паролем (нужно для автоматической загрузки).
  
  - days 365: сертификат действителен год.
  
  - newkey rsa:2048: генерируем новый 2048-битный RSA-ключ.
  
  - keyout tls.key: файл, куда сохранится приватный ключ.
  
  - out tls.crt: файл, куда сохранится сертификат.
  
  - subj: устанавливаем параметры субъекта сертификата (Common Name = ваш домен).
  
  - addext: добавляем расширение Subject Alternative Name (SAN), которое сейчас обязательно для современных браузеров
2) Создание Kubernetes Secret типа TLS
    ```shell
    # Убедитесь, что ваш kubectl смотрит в нужный namespace,
    # где будет жить Gateway (обычно это namespace'ы приложений или istio-system,
    # но для простоты оставим default, где лежат ваши сервисы)
    kubectl create secret tls myapp-tls-cert \
    --key tls.key \
    --cert tls.crt \
    --n istio-system
    ```
    Проверить, что создалось `kubectl get secrets -n istio-system | grep myapp-tls-cert`
    !!!Важно!!! Секрет нужно создавать в неймспейсе где установлен сам ingress, а не сервисы
3) Доработать конфигурацию gateway
    ```yaml
        spec:
          servers:
            - port:
                number: 443
                name: https
                protocol: HTTPS
              tls:
                mode: SIMPLE
                credentialName: myapp-tls-cert   # Имя секрета, который мы создали
              hosts:
                - "myapp.local"
    ```
4) Применить конфиг `kubectl apply -f ingress.yml` и открыть `minikube tunnel` либо пробросить порт как сделано в примере выше через `port-forward`
5) Проверить работоспособность
    ```shell
    evggorainov@MacBook-Air-Evgenij jetbra % curl -k https://myapp.local:443/rest-service/db/test
    {"id":"761fb65b-5784-42a6-9c1f-158033f8718a","data":"Hello from db service"}%                                                                           
    ```
6) При доступен по доменному имени myapp.local DNS резолвиться очень долго. `.local` — зарезервирован для mDNS (Bonjour) поэтому высокий таймаут
    ```shell
    evggorainov@MacBook-Air-Evgenij jetbra % curl -o /dev/null -s -w '
    DNS: %{time_namelookup}
    TCP: %{time_connect}
    TLS: %{time_appconnect}
    TTFB: %{time_starttransfer}
    TOTAL: %{time_total}
    ' https://myapp.local
    
    DNS: 5.009433
    TCP: 0.000000
    TLS: 0.000000
    TTFB: 0.000000
    TOTAL: 5.010222
    ```
7) Необходимо переделать все доменное имя myapp.dev, заменить в /etc/hosts, перевыпустить сертификат удалить из неймпейса старый и пересоздать
    ```shell
    evggorainov@MacBook-Air-Evgenij certs % kubectl delete secret myapp-tls-cert -n istio-system
    secret "myapp-tls-cert" deleted from istio-system namespace
    
    evggorainov@MacBook-Air-Evgenij certs % openssl req -x509 -nodes -days 1000 -newkey rsa:2048 \                          
      -keyout tls.key \
      -out tls.crt \
      -subj "/CN=myapp.dev/O=myapp" \
      -addext "subjectAltName = DNS:myapp.dev" 
    
    evggorainov@MacBook-Air-Evgenij certs % kubectl create secret tls myapp-tls-cert \          
      --key tls.key \
      --cert tls.crt \
      -n istio-system
    secret/myapp-tls-cert created
    
    evggorainov@MacBook-Air-Evgenij spec % k apply -f ingress.yml 
    ```
8) После этого отклик очень быстрый

    Через `port-forward`
    ```shell
    evggorainov@MacBook-Air-Evgenij jetbra % curl -k https://myapp.dev:8443/rest-service/db/test   
    {"id":"f5c7df1f-1434-4acc-9395-5602e5d74ef1","data":"Hello from db service"}%  
    ```
    Через `minikube tunnel`
    ```shell
    evggorainov@MacBook-Air-Evgenij jetbra % curl -k https://myapp.dev/rest-service/db/test 
    {"id":"8833bee1-eb7d-4a47-ad3f-5be9de73a870","data":"Hello from db service"}%  
    ```

### Если не работают запросы через istio после того, как мак перешел в режим ожидания
Проблемы с minikube (очень частое)

Minikube иногда:

- роняет DNS
- меняет IP сервисов
- ломает tunnel

👉 особенно после:

- sleep ноутбука
- restart minikube
- смены сети

Скорее всего поможет перезагрузка конфигурации istio
```shell
kubectl rollout restart deployment istio-ingressgateway -n istio-system
kubectl rollout restart deployment istiod -n istio-system
```
