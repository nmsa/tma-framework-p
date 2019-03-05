-- Resource Consumption per Pod
INSERT INTO QualityModel(modelName, modelDescriptionReference) VALUES ("ResConsPod", 1);

-- Metric (not all fields are present
INSERT INTO Metric(qualityModelId, metricName) VALUES (1, "ResConsPod");

-- MetricData (sample data)
INSERT INTO MetricData(qualityModelId, metricId, valueTime, value, resourceId) 
VALUES (1, 1, NOW(), 0.82, 9);

------------------------------------------------------------------------------------------

-- Action
INSERT INTO Action(actuatorId, resourceId, actionName) VALUES (5, 9, "scale");

-- Configuration
INSERT INTO Configuration(actionId, keyName, domain) VALUES
(1, "metadata.namespace", "string"),
(1, "metadata.name", "string"),
(1, "spec.replicas", "integer");