
// Written by: Mazda Marvasti, AppLariat Corp.

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor; 
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser; 

public class ConfigValidateProgressBar implements IRunnableWithProgress { 
    
	private InitialReleaseData rd;
	private boolean error=false;
	private String errorMessage;
	private String jwtToken;
	private String apiUrl;
	
	private RedeployData redeployData = new RedeployData();
	
	public ConfigValidateProgressBar(InitialReleaseData rd, String apiUrl, String jwtToken) {
		this.rd=rd;
		this.jwtToken=jwtToken;
		this.apiUrl=apiUrl;
	} 
	
	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public InitialReleaseData getRd() {
		return rd;
	}

	public void setRd(InitialReleaseData rd) {
		this.rd = rd;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
	}

	public RedeployData getRedeployData() {
		return redeployData;
	}

	public void setRedeployData(RedeployData redeployData) {
		this.redeployData = redeployData;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {	    
        try { 
        	JSONParser parser = new JSONParser();
        	
        	monitor.beginTask("Initializing Application", 100); 
        	
        	monitor.subTask("Registering Artifacts ...");
        	// Now register the new artifact and get its ID.
//      		String artUrl = RedeployData.getNewLocUrl(rd.getNewArtifactLocation(), rd.getRepositoryOwner(), rd.getRepositoryName(), rd.getRepositoryBranch());
        	String query = "{\"data\":{\"name\":\""+rd.getArtifactLoc().getArtifactLocationName()+"\", \"version\":\"1.0\", \"artifact_name\":\""+rd.getArtifactLoc().getArtifactLocationName()+"\", \"stack_id\":\""+rd.getRelease().getStackId()+"\", \"stack_artifact_type\":\"code\", \"loc_artifact_id\":\""+rd.getArtifactLoc().getId()+"\", \"metaData\": {\"display_name\":\""+rd.getArtifactLoc().getRepoBranch()+"\"}}}";
        	String registerArtifactData = UrlCalls.urlConnectPost(getApiUrl()+"/stack_artifacts", query, getJwtToken());
        	if (registerArtifactData==null) {
        		setError(true);
        		setErrorMessage("Unable to register new artifact.");
        		monitor.done(); 
        		return;
        	}
        	ArtifactData ad = new ArtifactData();
        	JSONObject jo1 = (JSONObject)parser.parse(registerArtifactData);
        	String artifactId = null;
        	if (jo1.get("data")!=null) {
        		artifactId = (String)jo1.get("data");
        		ad.setId(artifactId);
        		ad.setName(rd.getArtifactLoc().getRepoBranch());
        		ad.setStackComponentId(rd.getArtifact().getStackComponentId());
        		ad.setComponentName(rd.getArtifact().getComponentName());
        		ad.setComponentServiceId(rd.getArtifact().getComponentServiceId());
        	} else {
        		setError(true);
        		setErrorMessage("Unable to register new artifacts.");
        		monitor.done(); 
        		return;
        	}
        	monitor.worked(50);       
        	
        	
        	DeployData dd = null;
        	monitor.subTask("Delete Previous Deployment ...");
        	// first delete any existing deployments with the same name
        	query = "name="+rd.getArtifactLoc().getRepoBranch()+"-deploy";
          	String deployData = UrlCalls.urlConnectGet(getApiUrl()+"/deployments", query, getJwtToken());
          	jo1 = (JSONObject)parser.parse(deployData);
          	if (jo1.get("data")!=null ) { // that means there are deployments there with exactly the same name. Need to delete those deployments. In reality there should only be one with this same exact name.
          		JSONArray ja = (JSONArray)jo1.get("data");
          		if (ja!=null && ja.size()>0) {
          			monitor.subTask("Delete Previous Deployment (may take some time)...");
          			for (int i=0; i<ja.size(); i++) {
          				String id = (String)((JSONObject)ja.get(i)).get("id");
          				String deleteData = UrlCalls.urlConnectDelete(getApiUrl()+"/deployments/"+id, "", getJwtToken());
          				// now get confirmation that the job has been deleted before moving forward
          				int count=0;
          				boolean finished=false;
          				do {
          					Thread.sleep(20000);
          					count++;
          					deleteData = UrlCalls.urlConnectGet(getApiUrl()+"/deployments/"+id, "is_deleted=true", getJwtToken());
          					JSONObject tmpjo1 = (JSONObject)parser.parse(deleteData);
          					if (tmpjo1.get("data")!=null) {
          						String status = (String)((JSONObject)((JSONObject)tmpjo1.get("data")).get("status")).get("state");
          						if (status.equalsIgnoreCase("stopped")) {
          							finished=true;
          						}
          					} else {
          						finished=true;
          					}
          					
          				} while (!finished && count<=10);
          				if (!finished) {
          					setError(true);
          	          		setErrorMessage("Unable to confirm deletion of previous deployment. Please try configuring again in a few minutes.");
          	          		monitor.done(); 
          	          		return;
          				}
          			}
          		}
          	}
          	monitor.worked(25);
          	
          	monitor.subTask("Initial Deployment ...");
        	// do the initial app deploy and get the deployId
        	query = "{ \"data\": {\"release_id\": \""+rd.getRelease().getId()+"\", \"name\": \""+rd.getArtifactLoc().getRepoBranch().toLowerCase()+"-deploy\", \"loc_deploy_id\": \""+rd.getDeployLoc().getId()+"\",\"components\": [{\"services\": [{\"name\": \""+rd.getArtifact().getComponentName()+"\",\"component_service_id\":\""+rd.getArtifact().getComponentServiceId()+"\",\"overrides\": {\"build\": {\"artifacts\": {\"code\": \""+artifactId+"\"}}}}],\"stack_component_id\": \""+rd.getArtifact().getStackComponentId()+"\"}]}}";
          	deployData = UrlCalls.urlConnectPost(getApiUrl()+"/deployments", query, getJwtToken());
          	jo1 = (JSONObject)parser.parse(deployData);
          	String deployId;          	
          	String deployName;
          	String stackId;
          	if (jo1.get("data")!=null ) {
          		JSONObject ja = ((JSONObject)jo1.get("data"));
          		deployId = (String)ja.get("deployment_id");

            	query = "";
              	deployData = UrlCalls.urlConnectGet(getApiUrl()+"/deployments/"+deployId, query, getJwtToken());
              	jo1 = (JSONObject)parser.parse(deployData);
              	ja = ((JSONObject)jo1.get("data"));
              	
          		deployName = (String)ja.get("name");
          		stackId = (String)((JSONObject)ja.get("stack")).get("id");
          		dd = new DeployData(deployId, deployName, stackId);
          		
           	} else {
          		setError(true);
          		setErrorMessage("Release Name "+rd.getRelease().getName()+" not found.");
          		monitor.done(); 
          		return;
          	}

          	monitor.worked(25);
          	      	
          	redeployData.setDeployData(dd);
          	redeployData.setArtifactData(ad);
          	redeployData.setRepositoryOwner(rd.getArtifactLoc().getOwner());
          	redeployData.setRepositoryName(rd.getArtifactLoc().getRepoName());
          	redeployData.setRepositoryBranch(rd.getArtifactLoc().getRepoBranch());
      		monitor.done(); 
      		
        } catch (Exception e) { 
        	setError(true);
        	StringBuffer sb = new StringBuffer("");
        	StackTraceElement[] ste = e.getStackTrace();
        	for (int i=0; i<ste.length; i++) {sb.append(ste[i]+"\n"); }
        	setErrorMessage("An error occured during deployment: "+sb.toString());
        	return;
        	//throw new InvocationTargetException(e); 
        } 
  } 
  
}
