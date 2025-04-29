// script.groovy

def buildImage() {
    echo "Building Docker Image..."
    sh "docker build --platform linux/amd64 -t ${env.DockerImageTag} ."
}

def pushImage() {
    echo "Pushing Image to DockerHub..."
    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh "echo $PASS | docker login -u $USER --password-stdin"
        sh "docker push ${env.DockerImageTag}"
    }
}

def deployCompose() {
    echo "Deploying with Docker Compose..."
    sshagent(['ec2']) {
        sh """
        scp -o StrictHostKeyChecking=no ${env.DotEnvFile} ${env.DockerComposeFile} ubuntu@${env.EC2_IP}:/home/ubuntu
        ssh -o StrictHostKeyChecking=no ubuntu@${env.EC2_IP} "
            export DC_IMAGE_NAME=${env.DockerImageTag} && \
            echo \$DC_IMAGE_NAME && \
            docker compose -f /home/ubuntu/${env.DockerComposeFile} --env-file /home/ubuntu/${env.DotEnvFile} down && \
            docker compose -f /home/ubuntu/${env.DockerComposeFile} --env-file /home/ubuntu/${env.DotEnvFile} up -d
        "
        """
    }
}

return this