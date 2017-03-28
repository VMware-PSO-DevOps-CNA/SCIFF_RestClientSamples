# SCIF_RestClientSamples
Rest Client example calls to vRO for VM provisioning

### Pre-Setup
* Modify the URL in the ```vmware_repo``` repository in the maven build file ```/rest-sample/pom.xml``` to point to a vRO Appliance or repository that has the vRO libraries. NOTE: After the first build the \*.jar files will be stored locally in the maven repo.
```
<repository>
  <id>vmware_repo</id>
    <url>https://<vRO_Appliance>:8281/vco-repo</url>
</repository>
```
* Import the workflows from ```/vroWorkflows``` into your vRO instance that you will be invoking workflows on. NOTE: some of these workflows come by default with the built in vRA plugin so you will not need to import them.
* Update all `TODO's` in ```/rest-sample/src/main/java/com/vmware/pso/samples/controller/VroController.java```
```
// TODO - CHANGE these to workflow UUIDs in your vRO!!!
private static final String GET_CATALOG_BY_NAME_WF = "ff840fb0-b931-42f6-ba63-fa0521e24bd5";
private static final String REQ_CATALOG_ITEM_WF = "50c6ad5a-f861-4dd1-8c8a-44d0cd2c613a";
private static final String GET_CATALOG_RESOURCE_BY_REQUEST_ID = "adfbb640-78ec-44e8-842d-72c0a4bde7e2";
private static final String GET_CATALOG_RESOURCE_ACTION_BY_NAME_WF = "1851c6b8-5822-4f4e-b8ac-5becf677e49a";
private static final String REQ_CATALOG_RESOURCE_ACTION_WF = "c54d08db-6538-4b26-96a6-897dad113e73";
```
```
// TODO - CHANGE this to your url, username, and password for vRO!!!
//start a new session to Orchestrator by using specified credentials
final VcoSession session = DefaultVcoSessionFactory.newLdapSession(
        new URI("https://<URL_TO_VRO>:8281/vco/api/"), "<Service_Acct_Username>", "<Service_Acct_Password>");
```

### To Build: (from ```/rest-sample```)
* mvn clean install

### To Run: (from ```/rest-sample```)
* mvn spring-boot:run

### To view Swagger UI with Rest Call Samples
* Swagger UI `http://localhost:8080/swagger-ui.html` 
* Sample Rest Calls `http://localhost:8080/swagger-ui.html#/vro-controller`
