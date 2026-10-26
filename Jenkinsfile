pipeline {
    agent any
    
    tools {
        jdk 'JDK21'
        maven 'Maven-3.8.4'
    }
    
    stages {   
        stage('Compile') {
            steps {
            sh 'mvn compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        
stage('SonarQube Analysis') {
    steps {
        
withSonarQubeEnv('SonarQube-Project5') {
    sh '''
        mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.2.0.4988:sonar \
          -Dsonar.projectKey=project-board \
          -Dsonar.projectName=Project-board
    '''
}

    }
}

        stage('Build') {
            steps {
                sh 'mvn package'
            }
        }

stage('Publish to Nexus') {
    steps {
        withCredentials([usernamePassword(
            credentialsId: 'nexus-credentials',
            usernameVariable: 'NEXUS_USER',
            passwordVariable: 'NEXUS_PASS'
        )]) {
            sh '''
                mvn deploy \
                  -DskipTests \
                  -Dnexus.username="$NEXUS_USER" \
                  -Dnexus.password="$NEXUS_PASS"
            '''
        }
    }
}

        
        stage('Trivy Filesystem Scan') {
            steps {
                sh '''
                    trivy fs \
                      --scanners vuln \
                      --exit-code 1 \
                      --severity HIGH,CRITICAL \
                      .
                '''
            }
        }
    }
}
    
