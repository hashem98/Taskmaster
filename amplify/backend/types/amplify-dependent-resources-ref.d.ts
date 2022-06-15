export type AmplifyDependentResourcesAttributes = {
    "api": {
        "taskmaster": {
            "GraphQLAPIKeyOutput": "string",
            "GraphQLAPIIdOutput": "string",
            "GraphQLAPIEndpointOutput": "string"
        }
    },
    "auth": {
        "taskmastereca2e7cc": {
            "IdentityPoolId": "string",
            "IdentityPoolName": "string",
            "UserPoolId": "string",
            "UserPoolArn": "string",
            "UserPoolName": "string",
            "AppClientIDWeb": "string",
            "AppClientID": "string"
        }
    },
    "function": {
        "S3Triggerf974f7f2": {
            "Name": "string",
            "Arn": "string",
            "Region": "string",
            "LambdaExecutionRole": "string"
        }
    },
    "storage": {
        "s30144fff8": {
            "BucketName": "string",
            "Region": "string"
        }
    },
    "predictions": {
        "translateTextebb8ea2f": {
            "region": "string",
            "sourceLang": "string",
            "targetLang": "string"
        },
        "speechGenerator7403de41": {
            "region": "string",
            "language": "string",
            "voice": "string"
        }
    },
    "analytics": {
        "taskmaster": {
            "Region": "string",
            "Id": "string",
            "appName": "string"
        }
    }
}