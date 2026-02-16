Для применения конфигурации из файла для деплоймента
```shell
cd /Users/evggorainov/IdeaProjects/k8s/specifications
kubectl apply -f deployment.yml
```
Для сервиса
```shell
kubectl apply -f service.yml
```
Либо одной командой
```shell
kubectl apply -f deployment.yml -f service.yml
```

После этого выполнить
```shell
minikube tunnel
```
И сервис будет доступен на 3030 порту `curl -v localhost:3030`


Для графического отображения кластера выполнить команду и дождаться открытия вебинтерфейса
```shell
minikube dashboard
```

Для удаления конфигурации
```shell
kubectl delete -f deployment.yml -f service.yml
```