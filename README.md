# Plan Component @ TMA Framework

The `TMA_Plan` is responsible for analyzing the measurements calculated by `TMA_Analyze` and will check the rules. If there are adaptations that need to be performed, they will be added to a queue to be executed by `TMA_Execute`. The set of adaptations is the "adaptation plan", which is required to achieve the trustworthiness level.

Each adapatation is added as a message to a queue. Each message should contain the following attributes:

* `resourceId` -- identifies the resource to which the adaptation is targeted
* `action` -- identifies the adaptation to be promoted by the actuator
* `configuration` -- configuration data for the action, which can be included in the form of key/value format

All the messages should be added in the `execute` queue, which is a topic from Kafka.