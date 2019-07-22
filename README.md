# user-api-sts
Example client access to AWS API Gateway using STS token

Simple example of how to use an STS token to access an API Gateway resource.

Two properties files are required:

creds.properties: AWS Account ID credentials
aws-gw.properties: AWS Gateway parameters (api-key, various ids)

See pom.xml for additional jar file requirement:
                <!-- https://github.com/awslabs/aws-request-signing-apache-interceptor.git -->

