### Запуск minikube on mac m2

В старой версии:
1) minikube config set driver docker
2) Запустить докер 
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
`kubectl get pods -o wide` - более подробная информация и подах(включая их IP)


`echo -n 'name' | base64` закодировать строку в base64

Для того, чтобы можно было ссылаться на секрет из деплоймента, секрет должен быть сначала создан или описан в конфигурации, иначе будет ошибка

`kubectl apply -f mongo-secret.yml`

`kubectl get secret` получить все созданные секреты

`kubectl delete all --all` удалить все в дефолтном неймспейсе

`minikube addons enable ingress` - включить использование ingress в minikube.
`kubectl get pods --all-namespaces` - убеждаемся, что есть ingress-nginx-controller

`minikube tunnel` - открыть туннель для сервиса(при использовании докер драйвера только так можно получить доступ)