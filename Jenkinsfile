// Load the external script.groovy file
def gv

pipeline {
    agent any

    environment {
        ImageRegistry = 'elvin98bc'
        EC2_IP = '13.215.46.190'
        DockerComposeFile = 'docker-compose.yml'
        DotEnvFile = '.env' 
        DockerImageTag = '${ImageRegistry}/${JOB_NAME}:${BUILD_NUMBER}'
    }

    stages {
        stage ("init"){
            steps {
                script {
                    gv = load "script.groovy"
                }
            }
        }
        
        stage("buildImage") {
            steps {
                script {
                    gv.buildImage()
                }
            }
        }

        stage("pushImage") {
            steps {
                script {
                    gv.pushImage()
                }
            }
        }

        stage("deployCompose") {
            steps {
                script {
                    gv.deployCompose()
                }
            }
        }
    }
}