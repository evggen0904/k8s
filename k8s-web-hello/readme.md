1) npm init -y
2) npm install express
3) npm start -сам запуск файла index.mjs
4) `docker build . -t evggen0904/k8s-web-hello-ru:latest -t evggen0904/k8s-web-hello-ru:1.0.0` создаем образ
5) `docker push evggen0904/k8s-web-hello-ru --all-tags` пушим в хаб
6) `kubectl create deploy k8s-web-hello --image=evggen0904/k8s-web-hello-ru:1.0.0` создаем деплоймент
    Для того чтобы проверить работу можно подключиться в миникуб и дернуть запрос на нашей конкретной поде
    ```shell
    minikube ssh;
    curl -v http://10.244.0.7:3000
    ```
7) Чтобы не обращаться напрямую к поде, создаем сервис `kubectl expose deploy k8s-web-hello --type=LoadBalancer --port=3333 --target-port=3000`
    И запускаем миникуб в режиме туннеля `minikube tunnel`. Потом проверяем, что все работает
    ```shell
    curl localhost:3333
    ```
8) Чтобы увеличить количество подов `kubectl scale deploy k8s-web-hello --replicas=3`