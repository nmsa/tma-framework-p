# Plan Component @ TMA Framework

The `TMA_Plan` is responsible for analyzing the measurements calculated by `TMA_Analyze`. All the scores that need to be consumed by `TMA_Plan` and will be on the `planning` topic. If there are adaptations that need to be performed, they will be added to a queue to be executed by `TMA_Execute`. The component is responsible for executing the verification rules that will be consolidated in a an "adaptation plan" to achieve the required goals, or to recover the desired levels of trustworthiness.

Each adaptation is added as a message to a queue. Each message should contain the following attributes:

* `resourceId` -- identifies the resource to which the adaptation is targeted
* `action` -- identifies the adaptation to be promoted by the actuator
* `configuration` -- configuration data for the action, which can be included in the form of key/value format

All the messages should be added in the `execute` topic, which will be consumed by `TMA_Execute`.