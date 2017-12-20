# Jade Web Start

## What is this?
This is is an open source and free software to help host and manage Java Web Start based applications easily. It is based on the Oracle
 [JnlpDownloadServlet](https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/downloadservletguide.html) 
 and provides additional deployment management, authentication and authorization capabilities.

 *Jade* allows you to deploy, host and organize your Java Web Start based applications and other related downloadable resources like images and/or documentations on different backend storage sources such as local files and cloud storage providers. 

## How it works
You simply lay out your applications and resources on a hierarchical file structure, (i.e. organize them in a directory on your servers or use Google Cloud Storage) and *JWeSt* discovers applications and resources and makes them available with the same hierarchical structure. See *Organization* section for more details

## Organization 
JWeSt uses concept of `Artifact`s to represent and organize hosted resources. Artifacts are identified by a URI which is derived from its location on the backend storage and are organized and presented in a hierarchical manner. This hierarchy reflects the deployment structure on the backend storage. *JWeSt* scans the backend and creates the following artifacts to reflect the hosted content:
+ **Executable:**
Represents a Java Web Start application located in a dedicated folder, which consists of exactly one jnlp file located directly under this folder describing the application and some other downloadable resource files (i.e. jar files) located anywhere within the application folder including its sub folders. Any application folder may also contain other artifacts. For example you can deploy two applications like `/main-app/` and `/main-app/client/`. If this is the case `client` app is presented as the child application of the `main-app`. Or you can deploy `/main-app/client/` and `/main-app/client/manual.pdf` and `manual.pdf` will be presented as a document for the main app.
+ **Resource:**
Represents a downloadable resource such as a document. Resources are individual files and therefore may not have other artifacts as their sub artifacts. All **pdf** files are represented as `resource`s and made available for download.
+ **Artifact:**
Any folder which is not an Executable but contains documents or executables somewhere reachable within it is represented as a plain `Artifact`. They are used to group other artifacts together. An Artifact's jurisdiction spans all of its sub directories but excludes the jurisdiction of its sub artifacts if any exists. Meaning, any content within an artifact's jurisdiction is serviced as a downloadable entry to the client if the client is authorized for this Artifact.

## Modules
*JWeSt* is modular, so you can bring the components you need.

### Backend
In order to deploy and host your Java Web Start based applications, first you need a backend module to store the files. The following backend modules are supported for now:

#### jws-file
Allows you to store and manage your files on a local filesystem under a certain directory. By default `/webstart` is taken to be the root directory for this purpose, however you can change this either through `WEB_START_ARTIFACT_ROOT` system property or environment variable. You simply copy and organize your application and resources under this directory and *JWeSt* will automatically detect and make them available. Alternatively you can `PUT` them through the `webstart/resources/artifact/backend/local/<path/to/your/app>` service url. You will need admin privileges to use this service url.
#### jws-gcs
Allows you to store and manage your files on Google Cloud Storage. When using this module, you need to provide the name of your Google Cloud Storage bucket name. You can do this bye setting either of `WEB_START_ARTIFACT_ROOT` system property or environment variable. You also need to have access privileges. If you are executing within Google Compute engine or Google Kubernetes Engine you already have through the system account. Just make sure you grant the rea/write permissions to cloud storage through your service account. If you are executing elsewhere, download your system account file and use `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to that file.

To deploy, you can upload your files `PUT` them through the `webstart/resources/artifact/backend/gcs/<path/to/your/app>` service url. You will need admin privileges to use this service url.

### Security
If you need authentication and authorization to filter certain applications for some users than you will need the security module. This module does not handle authentication itself, but enforces a non-anonymous user account to access hosted artifacts. You can use Java EE built in authentication mechanism, or delegate to an external SSO like *JBoss Keycloak*. 

Authorization can easily be enforced with role definitions. When role name interpreted as URI, user will be granted access to any any artifacts located under the role name. For example, if a user is assigned a role named `/all/restrictied/` this user will ave permission to access artifacts `/all/restrictied/main-app` and `/all/restrictied/main-app/client` but not the `/all/my-other-app`