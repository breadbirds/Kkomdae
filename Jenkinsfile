pipeline {
    agent any

    /////////////////////////////////////////////////////////////////////////
    // 환경 변수 설정
    /////////////////////////////////////////////////////////////////////////
    environment {
        DOCKER_IMAGE_BACKEND  = "dororo737/d-101"        // Docker Hub 리포지토리 이름 (스프링)
        DOCKER_IMAGE_FRONTEND = "dororo737/d-101-front" // Docker Hub 리포지토리 이름 (프론트엔드)
        DOCKER_IMAGE_DJANGO = "dororo737/d-101-django" 
        DOCKER_IMAGE_FLASK = "dororo737/d-101-flask"
        DOCKER_TAG          = "latest"                 // 태그
        REGISTRY_CREDENTIAL = "REGISTRY_CREDENTIAL"
        SSH_CREDENTIALS     = "SSH_CREDENTIALS"
        EC2_USER            = "ubuntu"
        EC2_HOST            = "j12d101.p.ssafy.io"
        DOCKER_COMPOSE_PATH = '/home/ubuntu/d-101'
    }

    //-----------------------------------------------------------------
    // Jenkins가 사전에 설정해둔 tool 매핑 (PATH 구성)
    //-----------------------------------------------------------------
    tools {
        jdk 'JDK17'
        gradle 'Gradle 8.12.1'
        nodejs 'NodeJS22' // Node.js 22 버전 사용
    }

    /////////////////////////////////////////////////////////////////////////
    // 파이프라인 Stages
    /////////////////////////////////////////////////////////////////////////
    stages {
        // 1) Checkout 및 변경 체크
        stage('Checkout & Diff Check') {
            steps {
                checkout scm
                script {
                    def diffCounts = [
                        backend: "backend/kkomdae",
                        django: "backend/kkomdae_django/",
                        flask: "backend/kkomdae_flask/",
                        landing: "frontend/kkomdae_landing/"
                    ]

                    diffCounts.each { key, path ->
                        def diffCount = sh(script: "git diff HEAD~1 HEAD --name-only | grep '^${path}' | wc -l", returnStdout: true).trim()
                        env."SKIP_${key.toUpperCase()}_BUILD" = (diffCount == '0') ? 'true' : 'false'
                        echo "${key.capitalize()} 변경 건수: ${diffCount} → SKIP_${key.toUpperCase()}_BUILD=${env."SKIP_${key.toUpperCase()}_BUILD"}"
                    }
                }
            }
        }
        // 2) 병렬 빌드 단계
        stage('Build & Push') {
            parallel {

                // 2-1) 백엔드 빌드 및 Docker 이미지 빌드/푸시
                stage('Backend Build & Docker Build/Push') {
                    when { expression { return env.SKIP_BACKEND_BUILD != 'true' } }
                    steps {
                        // Jenkins Credentials에 등록된 .env 파일을 가져옴
                        withCredentials([file(credentialsId: '.env', variable: 'env')]) {
                            sh '''
                                rm -f backend/kkomdae/src/main/resources/.env
                                mkdir -p backend/kkomdae/src/main/resources
                                cp "$env" backend/kkomdae/src/main/resources/.env
                                cat backend/kkomdae/src/main/resources/.env
                            '''
                        }
                        // backend/kkomdae 디렉토리로 이동하여 빌드 및 Docker 작업 진행
                        dir('backend/kkomdae') {
                            // Gradle 빌드
                            sh '''
                            chmod +x gradlew
                            ./gradlew clean build -Dspring.profiles.active=prod
                            '''
                            // JAR 파일을 Dockerfile 경로로 복사
                            sh 'cp build/libs/kkomdae-0.0.1-SNAPSHOT.jar ./app.jar'

                            // Docker 빌드 및 푸시
                            script {
                                docker.withRegistry('https://index.docker.io/v1/', REGISTRY_CREDENTIAL) {
                                    def app = docker.build("${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG}")
                                    sh 'docker info'
                                    app.push()
                                }
                            }
                        }
                    }
                }

                // 2-2) 프론트엔드 빌드 및 Docker 이미지 빌드/푸시
                stage('Landing Build & Docker Build/Push') {
                    when { expression { return env.SKIP_LANDING_BUILD != 'true' } }
                    steps {
                        dir('frontend/kkomdae_landing') {
                            sh 'npm install'
                            sh 'npm run build'
                            script {
                                docker.withRegistry('https://index.docker.io/v1/', REGISTRY_CREDENTIAL) {
                                    def app = docker.build("${DOCKER_IMAGE_FRONTEND}:${DOCKER_TAG}")
                                    sh 'docker info'
                                    app.push()
                                }
                            }
                        }
                    }
                }

                // 2-3) Django 빌드 및 Docker 이미지 빌드/푸시
                stage('Django Build & Docker Build/Push') {
                    when { expression { return env.SKIP_DJANGO_BUILD != 'true' } }
                    steps {
                        // Jenkins Credentials에 등록된 .env 파일을 가져옴
                        withCredentials([file(credentialsId: 'Django.env', variable: 'env')]) {
                            sh '''
                                rm -f backend/kkomdae_django/.env
                                cp "$env" backend/kkomdae_django/.env
                                cat backend/kkomdae_django/.env
                            '''
                        }
                        // backend/kkomdae_django 디렉토리로 이동하여 빌드 및 Docker 작업 진행
                        dir('backend/kkomdae_django') {
                            script {
                                docker.withRegistry('https://index.docker.io/v1/', REGISTRY_CREDENTIAL) {
                                    def app = docker.build("${DOCKER_IMAGE_DJANGO}:${DOCKER_TAG}")
                                    sh 'docker info'
                                    app.push()
                                }
                            }        
                        }
                    }
                }
                
                // 2-4) Flask 빌드 및 Docker 이미지 빌드/푸시
                stage('Flask Build & Docker Build/Push') {
                    when { expression { return env.SKIP_FLASK_BUILD != 'true' } }
                    steps {
                        // Jenkins Credentials에 등록된 .env 파일을 가져옴
                        withCredentials([file(credentialsId: 'Flask.env', variable: 'env')]) {
                            sh '''
                                rm -f backend/kkomdae_flask/.env
                                cp "$env" backend/kkomdae_flask/.env
                                cat backend/kkomdae_flask/.env
                            '''
                        }
                        // backend/kkomdae_flask 디렉토리로 이동하여 빌드 및 Docker 작업 진행
                        dir('backend/kkomdae_flask') {
                            script {
                                docker.withRegistry('https://index.docker.io/v1/', REGISTRY_CREDENTIAL) {
                                    def app = docker.build("${DOCKER_IMAGE_FLASK}:${DOCKER_TAG}")
                                    sh 'docker info'
                                    app.push()
                                }
                            }        
                        }
                    }
                }
            }
        }
    

        // 3) Deploy 단계: 원격 서버에서 docker-compose를 이용해 배포
        stage('Deploy') {
            when { expression { return env.SKIP_BACKEND_BUILD != 'true' || env.SKIP_LANDING_BUILD != 'true' || env.SKIP_DJANGO_BUILD != 'true' || env.SKIP_FLASK_BUILD != 'true' } }
            steps {
                echo "[Deploy] Deploying to ${EC2_HOST} as ${EC2_USER}"
                sshagent(credentials: ['SSH_CREDENTIALS']) {
                sh """
                    ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} /bin/bash <<EOS
    cd "${DOCKER_COMPOSE_PATH}"
    docker compose pull
    docker compose up -d --force-recreate
    docker system prune -f
    sleep 5
    docker ps || echo "Container check failed"
EOS
                """
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // 빌드 후 알림 설정
    /////////////////////////////////////////////////////////////////////////
    post {
        success {
            script {
                def Author_ID    = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
                def Author_Email = sh(script: "git log -1 --pretty=%ae", returnStdout: true).trim()
                mattermostSend(
                    color: 'good',
                    message: """\
✅ 빌드 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER}
작성자: ${Author_ID} (${Author_Email})
(&lt;${env.BUILD_URL}|상세보기&gt;)
""",
                    endpoint: 'https://meeting.ssafy.com/hooks/awheadganfnkjqc33r48z8xceo',
                    channel: '[🔔알림] 꼼대[🔔알림] 꼼대'
                )
            }
        }
        failure {
            script {
                def Author_ID    = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
                def Author_Email = sh(script: "git log -1 --pretty=%ae", returnStdout: true).trim()
                mattermostSend(
                    color: 'danger',
                    message: """\
❌ 빌드 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER}
작성자: ${Author_ID} (${Author_Email})
(&lt;${env.BUILD_URL}|상세보기&gt;)
""",
                    endpoint: 'https://meeting.ssafy.com/hooks/awheadganfnkjqc33r48z8xceo',
                    channel: '[🔔알림] 꼼대[🔔알림] 꼼대'
                )
            }
        }
    }
}
