# WebStarter

What is this?
-
This is is an open source and modular java web start application deployment management system. It is based on the Oracle
 [JnlpDownloadServlet](https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/downloadservletguide.html) 
 and provides additional deployment management and authorization capabilities.

 `WebStarter` allows you to host and organize your application and resources on different backend storage sources such as 
 local files and cloud storage providers. 

How it works?
-
 `WebStarter` manages contents with entries called `Artifact`. which are analogous to objects in a filesystem. An
 artifact provides downloadable content to the clients and access to individual artifacts can be restricted through 
 authorization control which is provided with security module. This allows restricted access for different clients. 
 
 There are 3 types of artifacts
 
 + **Component:**
 Components are analogous to folders in a file system and hence they are usually used to group other artifacts together. 
 A component's jurisdiction spans all of its sub directories but excludes the jurisdiction of its sub artifacts if any exists. 
 Any content within a component's jurisdiction is serviced as a downloadable entry to the client if the client is authorized for this component..
 + **Executable:**
 Represents a web start application which consists of a jnlp file and some deployment artifacts like jar files and other resources. 
 Executables are specialized `Component`s and hence all the rules for components apply to executables. For example an executable artifact 
 may have sub artifacts like other executables or any other type of artifacts.
 + **Resource:**
 Represents a downloadable resource such as a document. Resources are individual files and therefore may not have other artifacts as its sub artifacts. 
 
 Artifacts are uniquely identified with a URI which is based on its location on the backend storage and they are organized 
 in a hierarchical manner as a tree structure similar to files and folders in a file system. 
  
