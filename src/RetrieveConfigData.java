
// Written by Mazda Marvasti: AppLariat Corp.

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RetrieveConfigData implements IRunnableWithProgress {

	List<ReleaseData> releases = new ArrayList<ReleaseData>();
	List<DeployLocationData> deployLocList = new ArrayList<DeployLocationData>();
	List<ArtifactLocationSelectionData> artifactLocSelectionList = new ArrayList<ArtifactLocationSelectionData>();

	RedeployData rdd; 
	
	String errorMessage=null;
	boolean error=false;
	
	public RetrieveConfigData(RedeployData rdd) {
		super();
		this.rdd = rdd;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public RetrieveConfigData() {
		super();
	}

	public List<ReleaseData> getReleases() {
		return releases;
	}

	public void setReleases(List<ReleaseData> releases) {
		this.releases = releases;
	}

	public void addRelease(ReleaseData dd) {
		this.releases.add(dd);
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public List<DeployLocationData> getDeployLocList() {
		return deployLocList;
	}

	public void setDeployLocList(List<DeployLocationData> deployLocList) {
		this.deployLocList = deployLocList;
	}

	public void addDeployLocation (String id, String name) {
		this.deployLocList.add(new DeployLocationData(id, name));
	}
	
	
	public List<ArtifactLocationSelectionData> getArtifactLocSelectionList() {
		return artifactLocSelectionList;
	}

	public void setArtifactLocSelectionList(List<ArtifactLocationSelectionData> artifactLocSelectionList) {
		this.artifactLocSelectionList = artifactLocSelectionList;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	    
        try { 
        	monitor.beginTask("Retrieving Application Information", 100); 
        	monitor.subTask("Connecting to appLariat API Service ...");
        	JSONParser parser = new JSONParser();        	
        	
        	monitor.subTask("Retrieving Application information ...");        	
        	// now grab all available releases 
        	String query = "metadata=true";
          	String deployData = UrlCalls.urlConnectGet(rdd.getApiUrl()+"/releases", query, rdd.getJwtToken());
          	JSONObject jo1 = (JSONObject)parser.parse(deployData);
          	
          	String releaseId;
          	String releaseName;
          	String stackId;
          	String releaseTag;
          	if (jo1.get("data")!=null && ((JSONArray)jo1.get("data")).size()>0) { // if these were null we would have to exit gracefully. Not doing here now.
          		JSONArray ja = ((JSONArray)jo1.get("data"));
          		for (int j=0; j<ja.size(); j++) {
	          		releaseId = (String)((JSONObject)ja.get(j)).get("id");
	          		releaseTag = (String)((JSONObject)ja.get(j)).get("release_tag");
	          		stackId=(String)((JSONObject)ja.get(j)).get("stack_id");
	          		// get stack name
	          		query = "metadata=true";
  		          	String stackData = UrlCalls.urlConnectGet(rdd.getApiUrl()+"/stacks/"+stackId, query, rdd.getJwtToken());
  		          	JSONObject jo8 = (JSONObject)parser.parse(stackData);
  		          	String stackName;
  		          	if (((JSONObject)jo8.get("data")).get("meta_data")!=null) {
  		          		stackName = (String)((JSONObject)((JSONObject)jo8.get("data")).get("meta_data")).get("display_name");
  		          	} else {
  		          		stackName = (String)((JSONObject)jo8.get("data")).get("name");
  		          	}
  		          	
	          		if (((JSONObject)ja.get(j)).get("meta_data")!=null) {
	          			releaseName= stackName+" ; "+(String)((JSONObject)((JSONObject)ja.get(j)).get("meta_data")).get("display_name")+" ; v"+releaseTag;
	          		} else {
	          			releaseName = stackName+" ; "+releaseId+" ; v"+releaseTag;
	          		}
	          		JSONArray ja2 = (JSONArray)((JSONObject)ja.get(j)).get("components");
	          		// loop through each component to see which one has an artifact.
	          		ReleaseData dd = new ReleaseData(releaseId, releaseName, stackId);
	          		for (int i=0; i<ja2.size(); i++) {
	          			JSONArray ja3 = (JSONArray)((JSONObject)ja2.get(i)).get("services");
	          			String stackComponentId=(String)((JSONObject)ja2.get(i)).get("stack_component_id");
	          			for (int k=0; k<ja3.size(); k++) {
	          				JSONObject build = (JSONObject)((JSONObject)ja3.get(k)).get("build");
	          				String componentName = (String)((JSONObject)ja3.get(k)).get("name");
	          				String componentServiceId = (String)((JSONObject)ja3.get(k)).get("component_service_id");
	          				JSONObject artifacts = (JSONObject)build.get("artifacts");
	          				if (artifacts.get("code")!=null) {
	          					String stackArtifactId = (String)artifacts.get("code");
	          					// now look up this stack artifact ID to get its name
	          					query = "";
	          		          	String artifactData = UrlCalls.urlConnectGet(rdd.getApiUrl()+"/stack_artifacts/"+stackArtifactId, query, rdd.getJwtToken());
	          		          	JSONObject jo9 = (JSONObject)parser.parse(artifactData);	
	          		          	String artifactName = (String)((JSONObject)jo9.get("data")).get("artifact_name");
	          					dd.addArtifact(stackArtifactId, artifactName, stackComponentId, componentServiceId, componentName);
	          				}
	          			}
	          		}
	          		this.addRelease(dd);
          		}
          	}
          	
      		if (this.getReleases().size()<=0) {
      			setError(true);
            	setErrorMessage("There are no Applications avaliable for release.");
            	return;
      		}
          	monitor.worked(50);
          	
          	monitor.subTask("Retrieving Deploy Locations ...");        
           	query = "status.state=available&metadata=true";
          	String deployLoc = UrlCalls.urlConnectGet(rdd.getApiUrl()+"/loc_deploys", query, rdd.getJwtToken());
          	jo1 = (JSONObject)parser.parse(deployLoc);
          	if (jo1.get("data")!=null && ((JSONArray)jo1.get("data")).size()>0) { 
          		JSONArray ja = ((JSONArray)jo1.get("data"));
          		for (int j=0; j<ja.size(); j++) {
	          		String deployLocId = (String)((JSONObject)ja.get(j)).get("id");
	          		String deployLocName = (String)((JSONObject)((JSONObject)ja.get(j)).get("meta_data")).get("display_name");
	          		this.addDeployLocation(deployLocId, deployLocName);
          		} 
          	} else {
          		setError(true);
          		setErrorMessage("Unable to retrieve list of available deploy locations.");
          	}
          	
      		monitor.worked(25);
      		
      		// retrieve artifact locations
          	monitor.subTask("Retrieving Artifact Locations ...");        
           	query = "loc_artifacts_type=github&metadata=true";
          	String artifactLoc = UrlCalls.urlConnectGet(rdd.getApiUrl()+"/loc_artifacts", query, rdd.getJwtToken());
          	jo1 = (JSONObject)parser.parse(artifactLoc);
          	if (jo1.get("data")!=null && ((JSONArray)jo1.get("data")).size()>0) { 
          		JSONArray ja = ((JSONArray)jo1.get("data"));
          		for (int j=0; j<ja.size(); j++) {
	          		String displayName = (String)((JSONObject)ja.get(j)).get("name");
	          		String id = (String)((JSONObject)ja.get(j)).get("id");
	          		ArtifactLocationSelectionData alsd = new ArtifactLocationSelectionData(displayName, id);
	          		
	          		// now get all the branches of this id
	               	query = "browse=owner";
	              	String oneArtifactLoc = UrlCalls.urlConnectGet(rdd.getApiUrl()+"/loc_artifacts/"+id, query, rdd.getJwtToken());
	              	JSONObject jo2 = (JSONObject)parser.parse(oneArtifactLoc);
	              	JSONArray ja3 = (JSONArray)((JSONObject)((JSONObject)jo2.get("data")).get("browse")).get("list");
	                for(int k=0; k<ja3.size(); k++) {
	                	String val = (String)((JSONObject)ja3.get(k)).get("value");
	                	alsd.addOwner(val);
	                }
	              	this.artifactLocSelectionList.add(alsd);
          		} 
          	} else {
          		setError(true);
          		setErrorMessage("Unable to retrieve list of available deploy locations.");
          	}
          	monitor.worked(25);
      		monitor.done(); 
        } catch (Exception e) {
        	setError(true);
        	setErrorMessage("An error occured during configuration: "+e.getStackTrace());
        	e.printStackTrace();
        	throw new InvocationTargetException(e); 
        }
        	
        return;
	}	
}
