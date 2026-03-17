# Проект по развертыванию сервисов на spring boot в k8s.
Сервисы взаимодействуют друг с другом через rest с использование basic авторизации.

### Для запуска в кубере (minikube): 
- Настроить сам кластер [Установка и запуск minikube.md](../../%D0%A3%D1%81%D1%82%D0%B0%D0%BD%D0%BE%D0%B2%D0%BA%D0%B0%20%D0%B8%20%D0%B7%D0%B0%D0%BF%D1%83%D1%81%D0%BA%20minikube.md)
- Развернуть деплоймент [deployment.yml](../spec/deployment.yml) `kubectl apply -f deployment.yml`
- После этого при открытии туннуле `minikube tunnel` можно постучаться в кластер до сервиса через его LoadBalancer. эндпоинты
    ```shell
    ### Обращение напрямую к rest-service
    GET http://localhost:9080/rest-service/inner/test
    ### rest-service обращается внтури кластера к db-service
    GET http://localhost:9080/rest-service/db/test
    ```
  
### Настройка ingress
- Для настройки istioIngress использовать [istio_in_minikube.md](../../istio/istio_in_minikube.md)
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
  127.0.0.1       myapp.com
  ```
- Включить форвардинг портов с локальной машины до кластера `port-forward svc/istio-ingressgateway -n istio-system 8080:80` Теперь севис будет доступен по доменному имени
  ```shell
  evggorainov@MacBook-Air-Evgenij jetbra % curl http://myapp.com:8080/rest-service/db/test 
  {"id":"b1f19808-e122-4008-97e9-5e8a78f41fbd","data":"Hello from db service"}% 
  ```
- 
