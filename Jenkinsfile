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
                mvn sonar:sonar \
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
    }
}
