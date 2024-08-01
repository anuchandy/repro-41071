<<<<<<< HEAD
https://github.com/Azure/azure-sdk-for-java/issues/41071
=======
## with attempt to update message size
```
Created topic: topic-1722496803583 and subscription: sub0
Max message size:1024MB
Exception in thread "main" com.azure.core.exception.HttpResponseException: SubCode=40000. Bad Request. To know more visit https://aka.ms/sbResourceMgrExceptions. . TrackingId:31288929-c802-478f-8179-b93093d2f2a9_G29, SystemTracker:bowerready.servicebus.windows.net:topic-1722496803583, Timestamp:2024-08-01T07:20:05
```

## with default topic

```
Created topic: topic-1722150318427 and subscription: sub0
Sending a message.
Press any key to receive the message and send it to DLQ.

Receiving message...
Message received:{"uri":"null","queryParams":{"msgType":"AzureServiceBus","warehouseId":"0000000"},"body":"{\r\n    \"messageHeader\": {\r\n        \"msgId\": 14317,\r\n        \"msgTime\": \"2023-04-18T14:11:31.5515919-04:00\",\r\n        \"msgType\": \"AzureServiceBus\",\r\n        \"sender\": \"Raja\",\r\n        \"receiver\": \"Microsoft\",\r\n        \"version\": \"1.0\",\r\n    }\r\n}","headers":{"accept":"*/*","connection":"keep-alive","host":"localhost:7071","user-agent":"PostmanRuntime/7.40.0","accept-encoding":"gzip, deflate, br","content-type":"application/json","content-length":"502","msgid":"14317","postman-token":"eaba8efc-d0ff-43df-a70d-7cc96f56d115"},"destination":"Microsoft","destinationEndPointUrl":null,"responseMsgTopicName":null,"publishMsgTopicName":null,"blobContainerEndpointDetails":null,"blobNamePrefixIncludeFlag":null,"serviceBusSubjectValue":null}
Sent to DLQ

Press any key after resending the from DLQ to back to the topic.

Receiving message again...
Recevied it from DLQ{"uri":"null","queryParams":{"msgType":"AzureServiceBus","warehouseId":"0000000"},"body":"{\r\n    \"messageHeader\": {\r\n        \"msgId\": 14317,\r\n        \"msgTime\": \"2023-04-18T14:11:31.5515919-04:00\",\r\n        \"msgType\": \"AzureServiceBus\",\r\n        \"sender\": \"Raja\",\r\n        \"receiver\": \"Microsoft\",\r\n        \"version\": \"1.0\",\r\n    }\r\n}","headers":{"accept":"*/*","connection":"keep-alive","host":"localhost:7071","user-agent":"PostmanRuntime/7.40.0","accept-encoding":"gzip, deflate, br","content-type":"application/json","content-length":"502","msgid":"14317","postman-token":"eaba8efc-d0ff-43df-a70d-7cc96f56d115"},"destination":"Microsoft","destinationEndPointUrl":null,"responseMsgTopicName":null,"publishMsgTopicName":null,"blobContainerEndpointDetails":null,"blobNamePrefixIncludeFlag":null,"serviceBusSubjectValue":null}

```
>>>>>>> b0229b2aef41edf8036a0e81c5a4270912ec36b0
