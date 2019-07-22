# user-api-sts
## Example client access to AWS API Gateway using STS token

Simple example of how to use an STS token to access an API Gateway resource.

## Project setup:

Two properties files are required:

* creds.properties: AWS Account ID credentials
    * accessKey = yourAWSAccessKey
    * secretKey = yourAWSSecretKey
  
* aws-gw.properties: AWS Gateway parameters (api-key, various ids)
    * assumed_role = someArnRol
    * external_id = someExtId
    * path_user_list = Prod/user
    * protocol = https
    * host = someAPIGwHost
    * api_key = someApiKey
    * region = someRegion
    * service_name = execute-api

## Run demo
    * mvn exec:java

