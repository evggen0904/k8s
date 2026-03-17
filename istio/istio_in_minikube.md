Для запуска миникуба вместе с истио требуется больше ресурсов чем при обычном запуске
```shell
minikube start --cpus 6 --memory 8192
```
Устанавливаем истио
```shell
curl -L https://istio.io/downloadIstio | sh -
```
Пропишем PATH в .zshrc файле
```shell
export PATH="/Users/evggorainov/Downloads/istio-1.28.3/bin:$PATH"
```
Теперь можно проверить доступность выполнив команду
```shell
istioctl
```
Установить истио в кластере k8s
```shell
istioctl install
```
Появится неймспейс istio-system
```shell
evggorainov@MacBook-Air-Evgenij Downloads % kubectl get ns
NAME                   STATUS   AGE
default                Active   14d
istio-system           Active   61s
kube-node-lease        Active   14d
kube-public            Active   14d
kube-system            Active   14d
kubernetes-dashboard   Active   4d5h
```
```shell
evggorainov@MacBook-Air-Evgenij Downloads % k get pods -n istio-system 
NAME                                    READY   STATUS    RESTARTS   AGE
istio-ingressgateway-6b89d47f9d-zx4nn   1/1     Running   0          2m28s
istiod-5b4c5695cd-ctcrb                 1/1     Running   0          2m38s
```

Для того, чтобы истио начал работать в нашем неймспейсе необходимо добавить метку
```shell
evggorainov@MacBook-Air-Evgenij istio % kubectl label namespace default istio-injection=enabled 
namespace/default labeled

evggorainov@MacBook-Air-Evgenij istio % kubectl get ns default --show-labels                   
NAME      STATUS   AGE   LABELS
```

Теперь при запуске деплоймента будет вместо 1 пода 2
```shell
evggorainov@MacBook-Air-Evgenij two_deployments % k apply -f k8s-web-to-nginx.yml 
service/k8s-web-to-nginx created
deployment.apps/k8s-web-to-nginx created

evggorainov@MacBook-Air-Evgenij two_deployments % k get pod                      
NAME                                READY   STATUS    RESTARTS   AGE
k8s-web-to-nginx-55d9c6bbbb-2bblj   2/2     Running   0          38s
k8s-web-to-nginx-55d9c6bbbb-qmh77   2/2     Running   0          38s
k8s-web-to-nginx-55d9c6bbbb-xqhlw   2/2     Running   0          38s
```

Далее необходимо создать gateway и virtualService. Например, для моего сервиса работающего в кубере на порту 9080 [ingress.yml](../k8s-project/spec/ingress.yml)

Чтобы ingress стал доступен снаружи миникуба нужно выполнить portForward. Порт 8080 с локальной машины пробрасывает на 80 порт в кубере
```shell
kubectl port-forward svc/istio-ingressgateway -n istio-system 8080:80
```

После этого сервис при правильной настройке проксирования будет доступен при обращении через 8080 порт на localhost
```shell
curl -v http://localhost:8080/rest-service/db/test
```