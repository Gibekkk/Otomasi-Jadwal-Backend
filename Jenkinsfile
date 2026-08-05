pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6'
    }

    environment {
        IMAGE_NAME = 'jadwal-api'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
        DEPLOY_DIR = '/opt/jadwal/jadwal-backend'
        HOST_IP    = '10.1.49.167'
        HOST_USER  = 'root'
        IMAGE_TAR  = "${IMAGE_NAME}-${IMAGE_TAG}.tar.gz"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Image') {
            steps {
                sh '''
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                '''
            }
        }

        stage('Save Image') {
            steps {
                sh '''
                    docker save ${IMAGE_NAME}:${IMAGE_TAG} | gzip > ${IMAGE_TAR}
                '''
            }
        }

        stage('Transfer & Deploy') {
            steps {
                withCredentials([
                    file(credentialsId: 'jadwal-env-file', variable: 'ENV_FILE'),
                    sshUserPrivateKey(
                        credentialsId: 'jadwal-host-ssh',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {
                    sh '''
                        ssh -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $SSH_USER@${HOST_IP} "mkdir -p ${DEPLOY_DIR}"

                        scp -i $SSH_KEY -o StrictHostKeyChecking=no \
                            ${IMAGE_TAR} \
                            $SSH_USER@${HOST_IP}:${DEPLOY_DIR}/${IMAGE_TAR}

                        scp -i $SSH_KEY -o StrictHostKeyChecking=no \
                            docker-compose.deploy.yml \
                            $SSH_USER@${HOST_IP}:${DEPLOY_DIR}/docker-compose.yml

                        scp -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $ENV_FILE $SSH_USER@${HOST_IP}:${DEPLOY_DIR}/.env

                        ssh -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $SSH_USER@${HOST_IP} "
                                cd ${DEPLOY_DIR} && \
                                docker network create jadwal-network || true && \
                                docker compose down || true && \
                                gunzip -c ${IMAGE_TAR} | docker load && \
                                docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:0.0.1 && \
                                docker compose up -d && \
                                rm -f ${IMAGE_TAR} .env && \
                                docker image prune -f
                            "
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'rm -f ${IMAGE_TAR} || true'
            sh 'docker rmi ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest || true'
        }
        success {
            echo 'Pipeline berhasil! Jadwal API jalan di port 8000.'
        }
        failure {
            echo 'Pipeline gagal! Periksa log di atas.'
        }
    }
}