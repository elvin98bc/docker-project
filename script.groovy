// script.groovy

def buildImage() {
    echo "Building Docker Image..."
    sh "docker build --platform linux/amd64 -t ${DockerImageTag} ."
}

def pushImage() {
    echo "Pushing Image to DockerHub..."
    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh "echo $PASS | docker login -u $USER --password-stdin"
        sh "docker push ${DockerImageTag}"
    }
}

def deployCompose() {
    echo "Deploying with Docker Compose..."
    sshagent(['ec2']) {
        sh """
        scp -o StrictHostKeyChecking=no ${DotEnvFile} ${DockerComposeFile} ubuntu@${EC2_IP}:/home/ubuntu
        ssh -o StrictHostKeyChecking=no ubuntu@${EC2_IP} "
            export DC_IMAGE_NAME=${DockerImageTag} && \
            echo $DC_IMAGE_NAME && \
            docker compose -f /home/ubuntu/${DockerComposeFile} --env-file /home/ubuntu/${DotEnvFile} down && \
            docker compose -f /home/ubuntu/${DockerComposeFile} --env-file /home/ubuntu/${DotEnvFile} up -d
        "
        """
    }
}

return this