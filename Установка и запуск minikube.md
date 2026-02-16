### Запуск minikube on mac m2

В старой версии:
1) minikube config set driver docker
2) Запусть докер 
3) minikube start

В новой:
1) minikube start

### Основные команды
`minikube status` - проверка состояния кластера

`kubectl version` - версия кубера

`kubectl cluster-info` - информация о запущенном кластере

`kubectl get nodes` - все рабочие ноды

`kubectl get pods`

`kubectl get services`


`kubectl create deployment nginx-depl --image=nginx` создать деплоймент nginx(последняя версия)

`kubectl get deployment`

`kubectl get replicaset` - прослойка между подом и деплойментом(управляет репликами пода)

```shell
kubectl get namespaces
NAME              STATUS   AGE
default           Active   22m
kube-node-lease   Active   22m
kube-public       Active   22m
kube-system       Active   22m - в этом неймспейсе запускаются контейнеры для управления кластером с мастер ноды(api server, controller manager etc)
```


`kubectl get pods --namespace=kube-system` - посмотреть контейнеры в неймспейсе

Deployment управляет-> replicaset управляет->  pod(абстракция над container)



`kubectl describe pod mongo-depl-5ccf565747-smf2l `получить инфо о запуске пода
`kubectl logs mongo-depl-5ccf565747-smf2l` посмотреть логи пода
`kubectl exec -it mongo-depl-5ccf565747-smf2l -- bin/bash` для отладки пода (зайти в терминал и запусть команду bin/bash)

`kubectl delete deployment mongo-depl` - удалить деплоймент


`kubectl apply -f nginx-deployment.yaml` создать деплоймент из файла. 


### Типы сервисов для деплоймента
Создать деплоймент в ручную:
```shell
kubectl create deployment my-nginx-deployment --image=nginx
```
Увеличить количество подов в деплойменте
```shell
kubectl scale deploy my-nginx-deployment --replicas=3
```
Посмотреть описание деплоймента
```shell
kubectl describe deployment my-nginx-deployment
```
Создать сервис
```shell
k expose deploy my-nginx-deployment --type=NodePort --port=8888 --target-port=80
```

1) ClusterIp. Доступен для обращения только внутри кластера k8s
2) NodePort. Открывает внешний порт на каждой ноде. С этого внешнего порта будет проксирование на порт внутрь кластера, а с него уже на конкретную поду
    ```shell
    evggorainov@MacBook-Air-Evgenij ~ % minikube ip
    192.168.49.2
    
    evggorainov@MacBook-Air-Evgenij ~ % k get svc
    NAME                  TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
    kubernetes            ClusterIP   10.96.0.1       <none>        443/TCP          3h47m
    my-nginx-deployment   NodePort    10.106.59.197   <none>        8888:31768/TCP   2m5s
    ```
    К деплойменту my-nginx-deployment можно подключиться по ip адресу 192.168.49.2:31768
    ```shell
     evggorainov@MacBook-Air-Evgenij ~ % curl -v http://192.168.49.2:31768
     *   Trying 192.168.49.2:31768...
    ```
    Так как minikube запущен к докере, поэтому ip адрес ноды не доступен. 
    Для того, чтобы получить доступ, можно создать туннель с помощью миникуба
    ```shell
    minikube service my-nginx-deployment --url
    http://127.0.0.1:55970
    ```
   Теперь по этому адресу будет доступ
    ```shell
    evggorainov@MacBook-Air-Evgenij ~ % curl -v http://127.0.0.1:55970
    *   Trying 127.0.0.1:55970...
    * Connected to 127.0.0.1 (127.0.0.1) port 55970
    > GET / HTTP/1.1
    > Host: 127.0.0.1:55970
    > User-Agent: curl/8.7.1
    > Accept: */*
    >
    * Request completely sent off
      < HTTP/1.1 200 OK
      < Server: nginx/1.29.4
      < Date: Mon, 02 Feb 2026 13:35:14 GMT
      < Content-Type: text/html
      < Content-Length: 615
      < Last-Modified: Tue, 09 Dec 2025 18:28:10 GMT
      < Connection: keep-alive
      < ETag: "69386a3a-267"
      < Accept-Ranges: bytes
      <
    <!DOCTYPE html>
    <html>
    <head>
    <title>Welcome to nginx!</title>
    <style>
    html { color-scheme: light dark; }
    body { width: 35em; margin: 0 auto;
    font-family: Tahoma, Verdana, Arial, sans-serif; }
    </style>
    </head>
    <body>
    <h1>Welcome to nginx!</h1>
    <p>If you see this page, the nginx web server is successfully installed and
    working. Further configuration is required.</p>
    
    <p>For online documentation and support please refer to
    <a href="http://nginx.org/">nginx.org</a>.<br/>
    Commercial support is available at
    <a href="http://nginx.com/">nginx.com</a>.</p>
    
    <p><em>Thank you for using nginx.</em></p>
    </body>
    </html>
    * Connection #0 to host 127.0.0.1 left intact
    ```
    Удалить сервис
    ```shell
    evggorainov@MacBook-Air-Evgenij ~ % k delete svc my-nginx-deployment
    service "my-nginx-deployment" deleted from default namespace
    ```
3) LoadBalancer.
   ![LoadBalancer.png](images/LoadBalancer.png)
    Для того, чтобы можно было подключаться снаружи к кластеру миникуба
    ```shell
    evggorainov@MacBook-Air-Evgenij ~ % minikube tunnel
    ✅  Tunnel successfully started
    ```
    Создать сервис типа loadBalancer
    ```shell
    k expose deploy my-nginx-deployment --type=LoadBalancer --port=9999 --target-port=80
    ```
   ```shell
    evggorainov@MacBook-Air-Evgenij ~ % k get svc
    NAME                  TYPE           CLUSTER-IP    EXTERNAL-IP   PORT(S)          AGE
    kubernetes            ClusterIP      10.96.0.1     <none>        443/TCP          4h10m
    my-nginx-deployment   LoadBalancer   10.102.36.7   127.0.0.1     9999:30361/TCP   5s
    ```
   ![LB2.png](images/LB2.png)
    Теперь my-nginx-deployment будет доступен через `curl -v http://localhost:9999`



Стандартный пример конфиг yaml файла (спецификация, метаданные, атрибуты спецификации)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  replicas: 1
  selector: ...
  template: ...
```

Коннект между deployment и service устанавливается с помощью  labels and selectors (metadata содережит labels, spec содержит selectors)
```yaml
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  selector:
    matchLabels:
      app: nginx    
  template:
    metadata:
      labels:
        app: nginx
```

Поды получают label из template. Service соединяется с deployment через selector      
```yaml
kind: Service
metadata:
  name: nginx-service
spec:
  selector:
    app: nginx
  ports:
    - protocol: TCP
#      - порт, на котором доступен сервис
      port: 80 
#      - порт на котором работает под
      targetPort: 8080 
```

`kubectl describe service nginx-service` посмотреть конфигурацию сервиса
`kubectl get pods -o wide` - более подробная информация и подахм(включая их IP)


`echo -n 'name' | base64` задекодировать строку в бейс64

Для того, чтобы можно было ссылаться на секрет из деплоймента, секрет должен быть сначала создан или описан в конфигурации, иначе будет ошибка

`kubectl apply -f mongo-secret.yml`

`kubectl get secret` получить все созданные секреты



### ExternalService для проброса соединения наружу. Для этого указывается type: LoadBalancer. 
А так же nodePort: 30000 (должен быть между 30000-32767)

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mongo-express-service
spec:
  selector:
    app: mongo-express
  type: LoadBalancer  
  ports:
    - protocol: TCP
      port: 8081
      targetPort: 8081
#      - на этом порту будет доступен внешний сервис
      nodePort: 30000 
```

      
ExternalService помимо внутреннего clusterIP присваивает еще и внешний IP адрес. Для того, чтобы присвоить внешний IP адрес в миникубе нужно выполнить команду minikube service mongo-express-service (mongo-express-service - имя сервиса). В обычном кубере этого делать не нужно.

`kubectl delete all --all` удалить все в дефолтном неймспейсе


### Ingress. Нужен для проброса входящих соединении в k8s на внутрение сервисы
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: dashboard-ingress
  namespace: kubernetes-dashboard
  annotations:
    kubernetes.io/ingress.class: "nginx"
spec:
#  - routing rules
  rules: 
#    - доменное имя хоста, с к-го будет роутится запросы на internalService
  - host: dashboard.com 
#    - протокол, по которому данные будут маршрутизироваться на internalService(а не приниматься от внешних клиентов на доменном адресе)
    http: 
      paths:
      - path: /
        pathType: Exact  
        backend:
          service:
            name: kubernetes-dashboard - имя internalService 
            port: 
              number: 80 - порт, на котором работает внутренний сервис
```

Для ingress требуется имплементация. Это ingressController. Поднимается в качестве отдельного пода. Т.е у моего сервиса описан ingress, а ingressController уже управляем правилами со всех ingres-ов сервисов. IngressController - это входная точка в кластер k8s.


`minikube addons enable ingress` - включить использование ingress в minikube.
`kubectl get pods --all-namespaces` - убеждаемся, что есть ingress-nginx-controller


`minikube service nginx-service --url` - открыть туннель для сервиса(при использовании докер драйвера только так можно получить доступ)