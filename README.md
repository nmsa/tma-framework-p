# Planning Component @ TMA Framework

> TBC: needs figure?

The `TMA_Planning` is responsible for analyzing the measurements calculated by `TMA_Analyze`. 
All the scores that need to be consumed by `TMA_Planning` and will enqueued by `TMA_Analyze` on the `planning` topic. 
The `TMA_Planning` is responsible for executing the verification of the predefined rules.
For each rule that is not respected, one or more adaptations will be generated, which will be gathererd in a set of adaptations.

The next step is to consolidate this set of adaptations in a "adaptation plan" by dealing with conflicting rules and priorities. In practice, this consolidated "adaptation plan" is optimized to allow the system to recover the desired levels of trustworthiness.

The resulting "adaptation plan", containing the adaptations that need to be performed, will be added to a queue to be executed by `TMA_Execute`, and will also be stored in the `TMA_Knowledge` for future reference. 

> TBD: discuss plan structure better?

Each adaptation is added as a message to a queue. Each message should contain the following attributes:

* `resourceId` -- identifies the resource to which the adaptation is targeted
* `action` -- identifies the adaptation to be promoted by the actuator
* `configuration` -- configuration data for the action, which can be included in the form of key/value format

