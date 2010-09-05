================================================
Edition for Android - dependencies between JAR files
================================================


Below is a list of the dependencies between Restlet libraries. You need to ensure 
that all the dependencies of the libraries that you are using are on the classpath
of your Restlet program, otherwise ClassNotFound exceptions will be thrown.

A minimal Restlet application requires the org.restlet JAR.

To configure connectors such as HTTP server or HTTP client connectors, please refer
to the Restlet User Guide: http://wiki.restlet.org/docs_2.0/

org.restlet.ext.atom (Restlet Extension - Atom)
--------------------
 - nothing beside org.restlet JAR.

org.restlet (Restlet Core)
-----------
 - J2SE 5.0

org.restlet.ext.crypto (Restlet Extension - Crypto)
----------------------
 - nothing beside org.restlet JAR.

org.restlet.ext.httpclient (Restlet Extension - Apache HTTP Client)
--------------------------
 - org.apache.commons.codec_1.4
 - org.apache.httpclient_4.0
 - org.apache.httpcore_4.0
 - org.apache.httpmime_4.0
 - net.jcip.annotations_1.0
 - org.apache.commons.logging_1.1
 - org.apache.james.mime4j_0.6

org.restlet.ext.jaas (Restlet Extension - JAAS)
--------------------
 - nothing beside org.restlet JAR.

org.restlet.ext.jackson (Restlet Extension - Jackson)
-----------------------
 - jackson-core-asl_1.4
 - jackson-mapper-asl_1.4

org.restlet.ext.json (Restlet Extension - JSON)
--------------------
 - org.json_2.0

org.restlet.ext.net (Restlet Extension - Net)
-------------------
 - nothing beside org.restlet JAR.

org.restlet.ext.odata (Restlet Extension - OData service)
---------------------
 - nothing beside org.restlet JAR.

org.restlet.ext.rdf (Restlet Extension - RDF)
-------------------
 - nothing beside org.restlet JAR.

org.restlet.ext.xml (Restlet Extension - XML)
-------------------
 - nothing beside org.restlet JAR.
