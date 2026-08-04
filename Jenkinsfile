pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6'
    }

    environment {
        IMAGE_NAME          = 'jadwal-api'
        IMAGE_TAG           = "${env.BUILD_NUMBER}"
        DEPLOY_DIR          = '/opt/jadwal/jadwal-backend'
        HOST_IP             = '10.1.49.202'
        HOST_USER           = 'root'
        FIREBASE_FILE_NAME  = 'jadwal-7650f-firebase-adminsdk-fbsvc-1c0415a063.json'
        IMAGE_TAR           = "${IMAGE_NAME}-${IMAGE_TAG}.tar.gz"
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
                withCredentials([
                    file(credentialsId: 'jadwal-firebase-secret', variable: 'FIREBASE_FILE')
                ]) {
                    sh '''
                        cp $FIREBASE_FILE src/main/resources/${FIREBASE_FILE_NAME}
                        docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                    '''
                }
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
                        # Siapkan direktori deploy di server dev
                        ssh -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $SSH_USER@${HOST_IP} "mkdir -p ${DEPLOY_DIR}"

                        # Kirim image, compose file, dan env
                        scp -i $SSH_KEY -o StrictHostKeyChecking=no \
                            ${IMAGE_TAR} \
                            $SSH_USER@${HOST_IP}:${DEPLOY_DIR}/${IMAGE_TAR}

                        scp -i $SSH_KEY -o StrictHostKeyChecking=no \
                            docker-compose.deploy.yml \
                            $SSH_USER@${HOST_IP}:${DEPLOY_DIR}/docker-compose.yml

                        scp -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $ENV_FILE $SSH_USER@${HOST_IP}:${DEPLOY_DIR}/.env

                        # Load image dan jalankan ulang container
                        ssh -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $SSH_USER@${HOST_IP} "
                                cd ${DEPLOY_DIR} &&
                                docker compose --env-file .env down || true &&
                                gunzip -c ${IMAGE_TAR} | docker load &&
                                docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:0.0.1 &&
                                docker compose --env-file .env up -d &&
                                rm -f ${IMAGE_TAR} .env &&
                                docker image prune -f
                            "
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'rm -f ${IMAGE_TAR} src/main/resources/${FIREBASE_FILE_NAME} || true'
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