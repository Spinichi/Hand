pipeline {
    agent any

    parameters {
        booleanParam(name: 'FORCE_BUILD_BACKEND', defaultValue: false, description: '무조건 Backend 빌드')
        booleanParam(name: 'FORCE_BUILD_MOBILE', defaultValue: false, description: '무조건 Mobile App 빌드')
        booleanParam(name: 'FORCE_BUILD_WEAR', defaultValue: false, description: '무조건 Wear 빌드')
        booleanParam(name: 'FORCE_BUILD_AI', defaultValue: false, description: '무조건 AI 빌드')
    }

    environment {
        // GitLab
        GITLAB_CREDENTIALS = 'gitlab-token'
        GITLAB_URL = 'https://lab.ssafy.com/s13-final/S13P31A106.git'

        // Docker Registry
        REGISTRY_LOCAL = "localhost:${env.REGISTRY_PORT}"
        REGISTRY_PRIVATE = "${env.REGISTRY_PRIVATE_IP}:${env.REGISTRY_PORT}"
        REGISTRY_PUBLIC = "${env.REGISTRY_PUBLIC_IP}:${env.REGISTRY_PORT}"

        // Backend
        BACKEND_IMAGE = 'hand-backend'
        BACKEND_SERVER = "${env.BACKEND_SERVER_IP}"

        // AI
        AI_IMAGE = 'hand-ai'
        AI_SERVER = "${env.AI_SERVER_IP}"

        // Credentials
        SSH_CREDENTIALS = 'server-ssh-key'
    }

    options {
        gitLabConnection('GitLab')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    triggers {
        gitlab(
            triggerOnPush: true,
            triggerOnMergeRequest: false,
            triggerOnAcceptedMergeRequest: true,
            branchFilterType: 'NameBasedFilter',
            includeBranchesSpec: 'dev',
            targetBranchRegex: 'dev',
            secretToken: "${env.GITLAB_WEBHOOK_TOKEN}"
        )
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code...'
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/dev']],
                    userRemoteConfigs: [[
                        url: "${GITLAB_URL}",
                        credentialsId: "${GITLAB_CREDENTIALS}"
                    ]]
                ])
                sh 'git log -1 --oneline'
            }
        }

        stage('Parallel Build & Deploy') {
            parallel {
                stage('Backend CI/CD') {
                    when {
                        beforeAgent true
                        anyOf {
                            changeset pattern: "backend/**", caseSensitive: true
                            expression { return params.FORCE_BUILD_BACKEND }
                        }
                    }
                    stages {
                        stage('Backend Docker Build & Push') {
                            steps {
                                dir('backend') {
                                    echo '🐳 Building and Pushing Docker Image to Registry...'
                                    sh """
                                        # Docker Multi-stage build로 Gradle 빌드 포함
                                        docker build -t ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:${BUILD_NUMBER} .
                                        docker tag ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:${BUILD_NUMBER} ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:latest

                                        # Registry에 Push
                                        docker push ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:${BUILD_NUMBER}
                                        docker push ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:latest

                                        echo "✅ Pushed to Registry: ${REGISTRY_LOCAL}/${BACKEND_IMAGE}:latest"
                                    """
                                }
                            }
                        }

                        stage('Backend Deploy to Server 2') {
                            steps {
                                echo '🚀 Deploying Backend to Server 2...'
                                withCredentials([
                                    file(credentialsId: 'backend-env', variable: 'ENV_FILE')
                                ]) {
                                    sshagent([SSH_CREDENTIALS]) {
                                        sh """
                                            # .env 파일 전송
                                            echo "📤 Transferring .env file..."
                                            scp -o StrictHostKeyChecking=no \${ENV_FILE} ubuntu@${BACKEND_SERVER}:/home/ubuntu/.env

                                            # 서버2에서 배포 실행
                                            ssh -o StrictHostKeyChecking=no ubuntu@${BACKEND_SERVER} '
                                                # Registry에서 이미지 Pull
                                                echo "📥 Pulling image from Registry..."
                                                docker pull ${REGISTRY_PUBLIC}/${BACKEND_IMAGE}:latest

                                                # 기존 컨테이너 중지 및 제거
                                                echo "🛑 Stopping old container..."
                                                docker stop hand-backend 2>/dev/null || true
                                                docker rm hand-backend 2>/dev/null || true

                                                # 새 컨테이너 실행
                                                echo "🚀 Starting new container..."
                                                docker run -d \\
                                                    --name hand-backend \\
                                                    -p 8080:8080 \\
                                                    --env-file /home/ubuntu/.env \\
                                                    --restart unless-stopped \\
                                                    ${REGISTRY_PUBLIC}/${BACKEND_IMAGE}:latest

                                                # 컨테이너 실행 확인
                                                echo "⏳ Waiting for container to start..."
                                                sleep 10

                                                if docker ps | grep -q hand-backend; then
                                                    echo "✅ Container is running!"
                                                    docker ps | grep hand-backend
                                                else
                                                    echo "❌ Container failed to start!"
                                                    docker logs hand-backend
                                                    exit 1
                                                fi

                                                # 오래된 이미지 정리
                                                echo "🧹 Cleaning old images..."
                                                docker images | grep ${REGISTRY_PUBLIC}/${BACKEND_IMAGE} | grep -v latest | awk "{print \$3}" | xargs -r docker rmi -f || true
                                                docker image prune -f || true
                                            '
                                        """
                                    }
                                }
                            }
                        }
                    }
                }

                stage('Mobile App CI/CD') {
                    when {
                        beforeAgent true
                        anyOf {
                            changeset pattern: "frontend/app/**", caseSensitive: true
                            expression { return params.FORCE_BUILD_MOBILE }
                        }
                    }
                    stages {
                        stage('Prepare Firebase Credentials') {
                            steps {
                                dir('frontend') {
                                    echo '🔧 Preparing Firebase and Google Services credentials...'
                                    withCredentials([
                                        file(credentialsId: 'firebase_sa_json', variable: 'FIREBASE_SA_FILE'),
                                        file(credentialsId: 'google-services-json', variable: 'GOOGLE_SERVICES_FILE')
                                    ]) {
                                        sh """
                                            # Firebase 서비스 계정 JSON 파일 복사
                                            echo "📋 Copying Firebase service account..."
                                            cp \${FIREBASE_SA_FILE} firebase-service-account.json
                                            chmod 600 firebase-service-account.json

                                            # google-services.json 복사
                                            echo "📋 Copying google-services.json..."
                                            cp \${GOOGLE_SERVICES_FILE} app/google-services.json
                                            chmod 600 app/google-services.json

                                            # 파일 존재 확인
                                            if [ ! -f "app/google-services.json" ]; then
                                                echo "❌ google-services.json not found!"
                                                exit 1
                                            fi
                                            echo "✅ google-services.json prepared successfully!"
                                        """
                                    }
                                }
                            }
                        }

                        stage('Build & Upload APK') {
                            agent {
                                docker {
                                    image 'mingc/android-build-box:latest'
                                    args '-v /var/jenkins_home/.gradle:/root/.gradle -u root'
                                    reuseNode true
                                }
                            }
                            steps {
                                dir('frontend') {
                                    withCredentials([
                                        string(credentialsId: 'firebase_app_id_text', variable: 'FIREBASE_APP_ID_VALUE')
                                    ]) {
                                        script {
                                            def gitCommit = sh(returnStdout: true, script: 'git log -1 --oneline').trim()
                                            def releaseNotes = """
빌드 번호: ${BUILD_NUMBER}
빌드 시간: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
커밋: ${gitCommit}
배포자: Jenkins CI/CD
                                            """.trim()

                                            echo '📦 Building Debug APK and Uploading to Firebase...'
                                            sh """
                                                chmod +x gradlew
                                                ./gradlew --version

                                                echo "🔨 Building app module APK..."
                                                ./gradlew :app:assembleDebug

                                                # APK 확인
                                                if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
                                                    echo "✅ APK built successfully!"
                                                    ls -lh app/build/outputs/apk/debug/app-debug.apk
                                                else
                                                    echo "❌ APK build failed!"
                                                    exit 1
                                                fi

                                                # Firebase 환경변수
                                                export FIREBASE_SERVICE_ACCOUNT_JSON=\$(pwd)/firebase-service-account.json
                                                export FIREBASE_APP_ID=${FIREBASE_APP_ID_VALUE}
                                                export RELEASE_NOTES='${releaseNotes}'

                                                # Firebase 업로드
                                                echo "📤 Uploading to Firebase..."
                                                ./gradlew :app:appDistributionUploadDebug

                                                echo "✅ Uploaded to Firebase!"
                                            """
                                        }
                                    }
                                }
                            }
                            post {
                                always {
                                    dir('frontend') {
                                        echo '🧹 Cleaning up sensitive files...'
                                        sh '''
                                            rm -f firebase-service-account.json || true
                                            rm -f app/google-services.json || true
                                        '''
                                    }
                                }
                            }
                        }
                    }
                }

                stage('AI CI/CD') {
                    when {
                        beforeAgent true
                        anyOf {
                            changeset pattern: "ai/**", caseSensitive: true
                            expression { return params.FORCE_BUILD_AI }
                        }
                    }
                    stages {
                        stage('AI Docker Build & Push') {
                            steps {
                                dir('ai') {
                                    echo '🐳 Building AI Docker Image...'
                                    sh """
                                        # Docker 빌드
                                        docker build -t ${REGISTRY_LOCAL}/${AI_IMAGE}:${BUILD_NUMBER} .
                                        docker tag ${REGISTRY_LOCAL}/${AI_IMAGE}:${BUILD_NUMBER} ${REGISTRY_LOCAL}/${AI_IMAGE}:latest

                                        # Registry에 Push
                                        docker push ${REGISTRY_LOCAL}/${AI_IMAGE}:${BUILD_NUMBER}
                                        docker push ${REGISTRY_LOCAL}/${AI_IMAGE}:latest

                                        echo "✅ Pushed to Registry: ${REGISTRY_LOCAL}/${AI_IMAGE}:latest"
                                    """
                                }
                            }
                        }

                        stage('AI Deploy to Server 3') {
                            steps {
                                echo '🚀 Deploying AI to Server 3...'
                                withCredentials([
                                    file(credentialsId: 'ai-env', variable: 'ENV_FILE')
                                ]) {
                                    sshagent([SSH_CREDENTIALS]) {
                                        sh """
                                            # 기존 읽기 전용 .env 파일 제거 후 전송
                                            echo "📤 Transferring .env file..."
                                            ssh -o StrictHostKeyChecking=no ubuntu@${AI_SERVER} 'rm -f /home/ubuntu/ai/.env' || true
                                            scp -o StrictHostKeyChecking=no \${ENV_FILE} ubuntu@${AI_SERVER}:/home/ubuntu/ai/.env
                                            ssh -o StrictHostKeyChecking=no ubuntu@${AI_SERVER} 'chmod 644 /home/ubuntu/ai/.env'

                                            # docker-compose.yml 파일 전송
                                            echo "📤 Transferring docker-compose.yml..."
                                            scp -o StrictHostKeyChecking=no ai/docker-compose.yml ubuntu@${AI_SERVER}:/home/ubuntu/ai/docker-compose.yml

                                            # 서버3에서 배포 실행
                                            ssh -o StrictHostKeyChecking=no ubuntu@${AI_SERVER} '
                                                cd /home/ubuntu/ai

                                                # Registry에서 이미지 Pull
                                                echo "📥 Pulling image from Registry..."
                                                docker pull ${REGISTRY_PRIVATE}/${AI_IMAGE}:latest

                                                # 기존 컨테이너 중지 및 제거
                                                echo "🛑 Stopping old containers..."
                                                docker compose down 2>/dev/null || true

                                                # docker compose로 서비스 시작
                                                echo "🚀 Starting AI services..."
                                                REGISTRY_URL=${REGISTRY_PRIVATE} docker compose up -d

                                                # 컨테이너 실행 확인
                                                echo "⏳ Waiting for containers to start..."
                                                sleep 15

                                                if docker ps | grep -q hand-ai && docker ps | grep -q hand-weaviate; then
                                                    echo "✅ AI containers are running!"
                                                    docker ps | grep hand-
                                                else
                                                    echo "❌ AI containers failed to start!"
                                                    docker compose logs
                                                    exit 1
                                                fi

                                                # 오래된 이미지 정리
                                                echo "🧹 Cleaning old images..."
                                                docker images | grep ${REGISTRY_PRIVATE}/${AI_IMAGE} | grep -v latest | awk "{print \$3}" | xargs -r docker rmi -f || true
                                                docker image prune -f || true
                                            '
                                        """
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ 빌드/배포 성공!'
            updateGitlabCommitStatus name: 'build', state: 'success'
        }
        failure {
            echo '❌ 빌드/배포 실패!'
            updateGitlabCommitStatus name: 'build', state: 'failed'
        }
        always {
            echo '🧹 Cleaning up...'
            sh '''
                # Jenkins 서버 로컬 이미지 정리
                docker images | grep ${BACKEND_IMAGE} | grep -v latest | awk '{print $3}' | xargs -r docker rmi -f || true
                docker images | grep ${AI_IMAGE} | grep -v latest | awk '{print $3}' | xargs -r docker rmi -f || true

                # Registry에서 오래된 이미지 태그 정리 (latest + 최근 1개만 유지)
                echo "🗑️ Cleaning old tags from Registry..."

                # Backend 태그 정리
                echo "🔍 Checking Backend tags..."
                ALL_BACKEND_TAGS=$(curl -s http://registry:5000/v2/${BACKEND_IMAGE}/tags/list | jq -r '.tags[]')
                echo "All Backend tags: $ALL_BACKEND_TAGS"
                OLD_BACKEND_TAGS=$(echo "$ALL_BACKEND_TAGS" | grep -v latest | sort -rn | tail -n +2)
                echo "Tags to delete: $OLD_BACKEND_TAGS"

                if [ ! -z "$OLD_BACKEND_TAGS" ]; then
                    for tag in $OLD_BACKEND_TAGS; do
                        echo "Deleting ${BACKEND_IMAGE}:$tag"
                        DIGEST=$(curl -I -s -H "Accept: application/vnd.docker.distribution.manifest.v2+json" \
                            http://registry:5000/v2/${BACKEND_IMAGE}/manifests/$tag | \
                            grep -i Docker-Content-Digest | awk '{print $2}' | tr -d '\r')
                        echo "Digest: $DIGEST"
                        if [ ! -z "$DIGEST" ]; then
                            curl -X DELETE http://registry:5000/v2/${BACKEND_IMAGE}/manifests/$DIGEST || true
                            echo "✅ Deleted ${BACKEND_IMAGE}:$tag"
                        fi
                    done
                else
                    echo "No old Backend tags to delete"
                fi

                # AI 태그 정리
                echo "🔍 Checking AI tags..."
                ALL_AI_TAGS=$(curl -s http://registry:5000/v2/${AI_IMAGE}/tags/list | jq -r '.tags[]')
                echo "All AI tags: $ALL_AI_TAGS"
                OLD_AI_TAGS=$(echo "$ALL_AI_TAGS" | grep -v latest | sort -rn | tail -n +2)
                echo "Tags to delete: $OLD_AI_TAGS"

                if [ ! -z "$OLD_AI_TAGS" ]; then
                    for tag in $OLD_AI_TAGS; do
                        echo "Deleting ${AI_IMAGE}:$tag"
                        DIGEST=$(curl -I -s -H "Accept: application/vnd.docker.distribution.manifest.v2+json" \
                            http://registry:5000/v2/${AI_IMAGE}/manifests/$tag | \
                            grep -i Docker-Content-Digest | awk '{print $2}' | tr -d '\r')
                        echo "Digest: $DIGEST"
                        if [ ! -z "$DIGEST" ]; then
                            curl -X DELETE http://registry:5000/v2/${AI_IMAGE}/manifests/$DIGEST || true
                            echo "✅ Deleted ${AI_IMAGE}:$tag"
                        fi
                    done
                else
                    echo "No old AI tags to delete"
                fi

                docker image prune -f || true
            '''
            cleanWs(
              deleteDirs: true,
              patterns: [
                [pattern: '**',                      type: 'INCLUDE'], // 기본은 모두 삭제
                [pattern: 'frontend/app/build/**',   type: 'EXCLUDE']  // 여기만 보존
              ]
            )
        }
    }
}