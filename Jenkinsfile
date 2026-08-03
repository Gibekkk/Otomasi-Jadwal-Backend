pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6'
    }

    environment {
        IMAGE_NAME         = 'moodbites-api'
        IMAGE_TAG          = '0.0.1'
        DEPLOY_DIR         = '/home/moodbites/moodbites/moodbites-backend'
        HOST_IP            = '103.185.52.161'
        HOST_USER          = 'moodbites'
        FIREBASE_FILE_NAME = 'moodbites-7650f-firebase-adminsdk-fbsvc-1c0415a063.json'
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

        stage('Build & Deploy') {
            steps {
                withCredentials([
                    file(credentialsId: 'moodbites-env-file', variable: 'ENV_FILE'),
                    file(credentialsId: 'moodbites-firebase-secret', variable: 'FIREBASE_FILE'),
                    sshUserPrivateKey(
                        credentialsId: 'moodbites-host-ssh',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {
                    sh '''
                        # Reset repo di host
                        ssh -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $SSH_USER@${HOST_IP} "
                                cd ${DEPLOY_DIR} &&
                                git fetch origin &&
                                git reset --hard origin/main &&
                                git clean -fd
                            "

                        # Kirim compose, env, dan firebase secret setelah git reset
                        scp -i $SSH_KEY -o StrictHostKeyChecking=no \
                            docker-compose.deploy.yml \
                            $SSH_USER@${HOST_IP}:${DEPLOY_DIR}/docker-compose.yml

                        scp -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $ENV_FILE $SSH_USER@${HOST_IP}:${DEPLOY_DIR}/.env

                        scp -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $FIREBASE_FILE \
                            $SSH_USER@${HOST_IP}:${DEPLOY_DIR}/src/main/resources/${FIREBASE_FILE_NAME}

                        # Build dan deploy di host
                        ssh -i $SSH_KEY -o StrictHostKeyChecking=no \
                            $SSH_USER@${HOST_IP} "
                                docker compose -f ${DEPLOY_DIR}/docker-compose.yml \
                                    --env-file ${DEPLOY_DIR}/.env \
                                    down || true &&
                                docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ${DEPLOY_DIR} &&
                                docker compose -f ${DEPLOY_DIR}/docker-compose.yml \
                                    --env-file ${DEPLOY_DIR}/.env \
                                    up -d &&
                                rm -f ${DEPLOY_DIR}/.env
                            "
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline berhasil! Moodbites API jalan di port 8000.'
        }
        failure {
            echo 'Pipeline gagal! Periksa log di atas.'
        }
    }
}