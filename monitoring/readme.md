### Как развернуть стэк prometheus-grafana в кластере
1) Развернуть через helm весь стэк
    ```shell
    helm install monitoring oci://ghcr.io/prometheus-community/charts/kube-prometheus-stack
    ```
2) Смотрим какие поды стартанули
    ```shell
    evggorainov@MacBook-Air-Evgenij spec % k get pod
    NAME                                                     READY   STATUS    RESTARTS   AGE
    alertmanager-monitoring-kube-prometheus-alertmanager-0   3/3     Running   0          50m
    db-service-7d5bdc5758-qgvff                              2/2     Running   0          28h
    monitoring-grafana-7bdc6bbb7f-pbzft                      4/4     Running   0          50m
    monitoring-kube-prometheus-operator-6c5fc6df46-4fx7g     2/2     Running   0          50m
    monitoring-kube-state-metrics-67d5f7bf68-5pq6f           2/2     Running   0          50m
    monitoring-prometheus-node-exporter-5jr9d                1/1     Running   0          50m
    prometheus-monitoring-kube-prometheus-prometheus-0       3/3     Running   0          50m
    rest-service-df79c8474-ggnq7                             2/2     Running   0          28h
    ```
3) Ищем на каком порту стартанула графана
    ```shell
    evggorainov@MacBook-Air-Evgenij spec % k logs monitoring-grafana-7bdc6bbb7f-pbzft | grep "address="          
    logger=http.server t=2026-03-27T10:45:20.415529971Z level=info msg="HTTP Server Listen" address=[::]:3000 protocol=http subUrl= socket=
    logger=live t=2026-03-27T11:29:28.823929544Z level=info msg="Initialized channel handler" channel=grafana/dashboard/uid/efa86fd1d0c121a26444b636a3f509a8 address=grafana/dashboard/uid/efa86fd1d0c121a26444b636a3f509a8
    logger=live t=2026-03-27T11:29:50.691965555Z level=info msg="Initialized channel handler" channel=grafana/dashboard/uid/6581e46e4e5c7ba40a07646395ef7b23 address=grafana/dashboard/uid/6581e46e4e5c7ba40a07646395ef7b23
    logger=live t=2026-03-27T11:30:40.650008133Z level=info msg="Initialized channel handler" channel=grafana/dashboard/uid/9fa0d141-d019-4ad7-8bc5-42196ee308bd address=grafana/dashboard/uid/9fa0d141-d019-4ad7-8bc5-42196ee308bd
    ```
4) Видим порт 3000, открываем его `kubectl port-forward deployment/monitoring-grafana 3000` и проверяем доступность через localhost:3000
5) Далее необходимо найти логин и пароль, с какими поднялась конфигурация
    ```shell
    evggorainov@MacBook-Air-Evgenij spec % k get secret
    NAME                                                                                  TYPE                 DATA   AGE
    alertmanager-monitoring-kube-prometheus-alertmanager                                  Opaque               1      56m
    alertmanager-monitoring-kube-prometheus-alertmanager-cluster-tls-config               Opaque               1      56m
    alertmanager-monitoring-kube-prometheus-alertmanager-generated                        Opaque               1      56m
    alertmanager-monitoring-kube-prometheus-alertmanager-tls-assets-0                     Opaque               0      56m
    alertmanager-monitoring-kube-prometheus-alertmanager-web-config                       Opaque               1      56m
    db-service-secret                                                                     Opaque               1      28h
    monitoring-grafana                                                                    Opaque               3      56m
    monitoring-kube-prometheus-admission                                                  Opaque               3      6d21h
    myapp-tls-cert                                                                        kubernetes.io/tls    2      9d
    prometheus-monitoring-kube-prometheus-prometheus                                      Opaque               1      56m
    prometheus-monitoring-kube-prometheus-prometheus-thanos-prometheus-http-client-file   Opaque               1      56m
    prometheus-monitoring-kube-prometheus-prometheus-tls-assets-0                         Opaque               1      56m
    prometheus-monitoring-kube-prometheus-prometheus-web-config                           Opaque               1      56m
    rest-service-secret                                                                   Opaque               1      28h
    sh.helm.release.v1.monitoring.v1                                                      helm.sh/release.v1   1      56m
    
    evggorainov@MacBook-Air-Evgenij spec % kubectl get secret monitoring-grafana -o yaml                         
    apiVersion: v1
    data:
      admin-password: VjFibzF2Y2xnMlFMVTBlT3BsQmFpVHdaNGhNMXpQRDg2TEp2NEVBUA==
      admin-user: YWRtaW4=
      ldap-toml: ""
    kind: Secret
    metadata:
      annotations:
        meta.helm.sh/release-name: monitoring
        meta.helm.sh/release-namespace: default
      creationTimestamp: "2026-03-27T10:44:34Z"
      labels:
        app.kubernetes.io/component: admin-secret
        app.kubernetes.io/instance: monitoring
        app.kubernetes.io/managed-by: Helm
        app.kubernetes.io/name: grafana
        app.kubernetes.io/version: 12.4.2
        helm.sh/chart: grafana-11.3.5
      name: monitoring-grafana
      namespace: default
      resourceVersion: "294723"
      uid: e3bdcf4c-54ba-47c4-95d5-cb4480b81186
    type: Opaque
    ```
6) И потом декодировать дефолтные ключи для `admin-user` и `admin-password`

### Доступ через абстракцию сервиса:
1) Смотрим какой порт случает сервис
   ```shell
   evggorainov@MacBook-Air-Evgenij spec % k get svc | grep monitoring-grafana
   monitoring-grafana                        ClusterIP      10.111.121.70    <none>        80/TCP                       5d21h
   ```
2) Открываем порт непосредственно через сервис
   ```shell
   kubectl port-forward svc/monitoring-grafana 8080:80
   ```
3) После этого сервис доступен на `http://localhost:8080/dashboards`

### Доступ через istio
1) Необходимо создать virtualService для проксирования запроса
   ```yaml
   apiVersion: networking.istio.io/v1beta1
   kind: VirtualService
   metadata:
     name: grafana-vs
   spec:
     hosts:
       - grafana.local
     gateways:
       - my-gateway
     http:
       - match:
           - uri:
               prefix: /
         route:
           - destination:
               host: monitoring-grafana
               port:
                 number: 80
   ```
2) Добавить host в gateway
   ```yaml
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
           - myapp.dev
           - grafana.local
           - prometheus.local
   ```
3) Так как по умолчанию графана стартует без истио сайдкара, графана не будет находиться в mesh сети и траффик будет перенаправляться через tls. Необходимо его отключить
   ```yaml
   apiVersion: networking.istio.io/v1beta1
   kind: DestinationRule
   metadata:
     name: grafana-dr
   spec:
     host: monitoring-grafana
     trafficPolicy:
       tls:
         mode: DISABLE
   ```
4) Добавить домен в hosts 127.0.0.1 grafana.local
5) После этого будет доступ через 80 порт истио http://grafana.local:80/dashboards
6) Выполнить теже настройки для prometheus. Будет доступен тут http://prometheus.local/query