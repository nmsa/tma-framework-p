# tma-planning

This project aims to:
* Consume the items from a topic queue;
* Check if the scores from the queue are in the proper threshold;
* Add a action item to a queue when an adaption is needed.


This is a simple module to validate the score of the metrics from Kubernetes.

To build the jar, you should run the following command on the worker node:
```sh
sh build.sh
```

The planning will consume the items from the topic `topic-planning`, and it will add new topics to `topic-execute`. To create the topic, you should run on the master node:
```sh
kubectl exec -ti kafka-0 -- kafka-topics.sh --create --topic topic-execute --zookeeper zk-0.zk-hs.default.svc.cluster.local:2181 --partitions 1 --replication-factor 1
```

To deploy the pod in the cluster, you should run the following command on the master node:

```sh
kubectl create -f tma-planning.yaml
```

You can also check the items on topic. In order to do that, you should connect to the Kafka pod and execute the consumer:
```sh
kubectl exec -ti kafka-0 -- bash
kafka-console-consumer.sh --topic topic-execute --bootstrap-server localhost:9093
```
