# TMA Planning

This project aims to:
* Consume the items from a topic queue (To be changed to the database);
* Check if the scores from the queue are in the proper threshold;
* Create the adaptation plan with action items when an adaption is needed.

## Prerequisites

This component requires the software available in [tma-utils](https://github.com/joseadp/tma-utils).

## Installation

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

## Authors
* José D'Abruzzo Pereira
