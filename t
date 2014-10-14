[1mdiff --git a/src/no_de/inf5090/visualizingsensordata/persistency/DataCollector.java b/src/no_de/inf5090/visualizingsensordata/persistency/DataCollector.java[m
[1mindex 470e061..3251d29 100644[m
[1m--- a/src/no_de/inf5090/visualizingsensordata/persistency/DataCollector.java[m
[1m+++ b/src/no_de/inf5090/visualizingsensordata/persistency/DataCollector.java[m
[36m@@ -38,6 +38,8 @@[m [mpublic abstract class DataCollector implements Observer {[m
      */[m
     private boolean mIsRecording = false;[m
 [m
[32m+[m	[32mprivate Object mUniqueId;[m
[32m+[m
     /**[m
      * Notification of a change in one of the sensors[m
      * Changes are collected[m
[36m@@ -62,6 +64,12 @@[m [mpublic abstract class DataCollector implements Observer {[m
     public void stopRecording() {[m
         mIsRecording = false;[m
     }[m
[32m+[m[41m    [m
[32m+[m[32m    private String getUniqueId() {[m
[32m+[m[41m    [m	[32mif (mUniqueId == null)[m
[32m+[m[41m    [m		[32mmUniqueId = UUID.randomUUID();[m
[32m+[m[41m    [m	[32mreturn mUniqueId;[m
[32m+[m[41m    [m	[32m}[m
 [m
     /**[m
      * Generate XML-DOM for the data[m
[36m@@ -80,6 +88,9 @@[m [mpublic abstract class DataCollector implements Observer {[m
             elm = doc.createElement("appName");[m
             elm.appendChild(doc.createTextNode("VisualizingSensorData"));[m
             rootElement.appendChild(elm);[m
[32m+[m[41m            [m
[32m+[m[32m            // unique id[m
[32m+[m[41m            [m
 [m
             // datetime[m
             rootElement.setAttribute("dateTime", Utils.getDateString(new Date()));[m
