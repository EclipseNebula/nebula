pipeline {
  agent {
    label "centos-latest"
  }

  options {
    timeout(time: 60, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '10'))
    disableConcurrentBuilds(abortPrevious: true)
  }

  tools {
    maven 'apache-maven-latest'
    jdk 'temurin-jdk21-latest'
  }

  environment {
    BUILD_TIMESTAMP = sh(returnStdout: true, script: 'date +%Y%m%d%H%M').trim()
    MAIN_BRANCH = 'master'
  }

  parameters {
    choice(
      name: 'BUILD_TYPE',
      choices: ['nightly', 'milestone', 'release'],
      description: '''
        Choose the type of build.
        Note that a release build will <b>not</b> promote the build, but rather will promote the most recent milestone build.
        '''
    )

    booleanParam(
      name: 'ECLIPSE_SIGN',
      defaultValue: true,
      description: '''
        Choose whether or not the bundles will be signed.
        This is relevant only for nightly and milestone builds.
      '''
    )

    booleanParam(
      name: 'PROMOTE',
      defaultValue: true,
      description: 'Whether to promote the build to the download server.'
    )
  }

  stages {
    stage('Display Parameters') {
      steps {
        script {
          env.BUILD_TYPE = params.BUILD_TYPE
          if (env.BRANCH_NAME == env.MAIN_BRANCH) {
            // Only sign the master branch.
            //
            env.ECLIPSE_SIGN = params.ECLIPSE_SIGN
          } else {
            // Do not sign PR builds.
            env.ECLIPSE_SIGN =  false
          }

          // Only promote signed builds, i.e., do not sign or promote PR builds.
          //
          env.PROMOTE = params.PROMOTE && (env.ECLIPSE_SIGN == 'true')

          def description = """
BUILD_TIMESTAMP=${env.BUILD_TIMESTAMP}
BUILD_TYPE=${env.BUILD_TYPE}
ECLIPSE_SIGN=${env.ECLIPSE_SIGN}
PROMOTE=${env.PROMOTE}
BRANCH_NAME=${env.BRANCH_NAME}
""".trim()
          echo description
          currentBuild.description = description.replace("\n", "<br/>")
        }
      }
    }

    stage('Build Nebula') {
      steps {
        script {
          if (env.PROMOTE == 'true') {
            // Only provide an agent context, which allows uploading to download.eclipse.org if we are promoting.
            // PR builds are not permitted to promote.
            //
            sshagent(['projects-storage.eclipse.org-bot-ssh']) {
              mvn()
            }
          } else {
            mvn()
            archiveArtifacts 'releng/org.eclipse.nebula.site/target/repository/**'
          }
        }
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
      recordIssues publishAllIssues: true, tools: [java(), mavenConsole(), javaDoc()]
    }

    failure {
      archiveArtifacts '**'
      mail to: 'ed.merks@gmail.com',
      subject: "[Nebula CI] Build Failure ${currentBuild.fullDisplayName}",
      mimeType: 'text/html',
      body: "Project: ${env.JOB_NAME}<br/>Build Number: ${env.BUILD_NUMBER}<br/>Build URL: ${env.BUILD_URL}<br/>Console: ${env.BUILD_URL}/console"
    }

    fixed {
      mail to: 'ed.merks@gmail.com',
      subject: "[Nebula CI] Back to normal ${currentBuild.fullDisplayName}",
      mimeType: 'text/html',
      body: "Project: ${env.JOB_NAME}<br/>Build Number: ${env.BUILD_NUMBER}<br/>Build URL: ${env.BUILD_URL}<br/>Console: ${env.BUILD_URL}/console"
    }

    cleanup {
      deleteDir()
    }
  }
}

def void mvn() {
  wrap([$class: 'Xvnc', takeScreenshot: false, useXauthority: true]) {
    // Only promoted builds will be signed.
    //
    sh '''
      if [[ $PROMOTE == true ]]; then
        promotion_argument='-Ppromote -Pbuild-server'
      fi
      mvn \
        $promotion_argument \
        --no-transfer-progress \
        -Dproject.build.sourceEncoding=UTF-8 \
        -Dbuild.id=${BUILD_TIMESTAMP} \
        -Dgit.commit=$GIT_COMMIT \
        -Dbuild.type=$BUILD_TYPE \
        -Dorg.eclipse.justj.p2.manager.build.url=$JOB_URL \
        clean \
        verify
      '''
  }
}
