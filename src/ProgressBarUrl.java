
// Written by: Mazda Marvasti, AppLariat Corp.

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ProgressBarUrl implements IRunnableWithProgress { 
    
	RedeployData rdd;
	
	public ProgressBarUrl(RedeployData rdd) {
		this.rdd=rdd;
	} 
	
	private String lastUpdateTime;
	private HashMap<String,String> ips = new HashMap<String,String>(); // key = service name, value=url
	boolean error = false;
	String errorMessage = null;
	
	
	public HashMap<String, String> getIps() {
		return ips;
	}

	public void setIps(HashMap<String, String> ips) {
		this.ips = ips;
	}

	public void addIp(String serviceName, String url) {
		this.ips.put(serviceName,  url);
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

	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public String getLastUpdateTimeLocal() {
//		System.out.println(getLastUpdateTime());
		int idx = this.lastUpdateTime.trim().indexOf(" ");
		String subStr = this.lastUpdateTime.trim().substring(0, idx)+"T"+this.lastUpdateTime.trim().substring(idx+1);
		ZonedDateTime localTime = ZonedDateTime.parse(subStr); 
		long lastUpdateEpoch = localTime.toEpochSecond();
		Instant utcInstant = new java.util.Date().toInstant();
		ZonedDateTime here = ZonedDateTime.ofInstant(utcInstant, ZoneId.of("UTC"));
		long currentTime = here.toEpochSecond();
		long diff = currentTime-lastUpdateEpoch;
		StringBuffer sb = new StringBuffer("");
		if (diff < 60L) {
			sb.append(diff+" Sec.");
		} else if (diff < 3600L) {
			int min = (int)(diff/60);
			int sec = (int)diff - min*60;
			sb.append(min+" Min. ,"+sec+" Sec.");
		} else if (diff < 86400L) {
			int hours = (int)(diff/3600);
			int min = (int)(((int)diff-hours*3600)/60);
			int sec = (int)(diff - min*60-hours*3600);
			sb.append(hours+(hours>1? " Hours, ":" Hour, ")+min+" Min., "+sec+" Sec.");
		} else {
			int days = (int)(diff/86400);
			int hours = (int)((diff-days*86400)/3600);
			int min = (int)(((int)diff-days*86400-hours*3600)/60);
			int sec = (int)(diff - min*60-hours*3600-days*86400);
			sb.append(days+(days>1? " days, ":" day, ")+hours+(hours>1? " Hours, ":" Hour, ")+min+" Min. ,"+sec+" Sec.");
		}
		return sb.toString();
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	    
        try { 
        	JSONParser parser = new JSONParser();
        	
        	monitor.beginTask("Retrieving Application Information", 100); 
        	monitor.worked(25);
        	
        	monitor.subTask("Retrieving deployment information ...");
        	String query = "";
          	String deployData = UrlCalls.urlConnectGet(rdd.getApiUrl()+"/deployments/"+rdd.getDeployData().getId(), query, rdd.getJwtToken());
          	JSONObject jo1 = (JSONObject)parser.parse(deployData);
          	String state = null;
          	if (jo1.get("data")!=null ) {
          		JSONObject ja = ((JSONObject)jo1.get("data"));
          		setLastUpdateTime ((String)ja.get("last_modified"));
          		// if the state is not "running" then we would have to do something else
          		state = (String)((JSONObject)ja.get("status")).get("state");
          		if (state.equalsIgnoreCase("updating")) {
          			setError(true);
              		setErrorMessage("Deployment "+rdd.getDeployData().getName()+" is being updated. Try again in a few minutes.");
              		monitor.done(); 
              		return;
          		} else if (state.equalsIgnoreCase("deploying")) {
              			setError(true);
                  		setErrorMessage("Application "+rdd.getDeployData().getName()+" is being deployed. Try again in a few minutes.");
                  		monitor.done(); 
                  		return;	
          		} else if (state.equalsIgnoreCase("stopped")) {
              		setError(true);
                  	setErrorMessage("Deployment "+rdd.getDeployData().getName()+" is currently stopped. Try reconfiguring.");
                  	monitor.done(); 
                  	return;
          		} else if (!(state.equalsIgnoreCase("running") || state.equalsIgnoreCase("deployed"))) {
          			setError(true);
              		setErrorMessage("Deployment "+rdd.getDeployData().getName()+" is not running.");
              		monitor.done(); 
              		return;
          		}
           	} else {
          		setError(true);
          		setErrorMessage("Deploy Name "+rdd.getDeployData().getName()+" not found.");
          		monitor.done(); 
          		return;
          	}

          	monitor.worked(30);
       	
      		monitor.subTask("Retrieving application URL ..."); 

      		JSONArray components = (JSONArray)((JSONObject)((JSONObject)jo1.get("data")).get("status")).get("components");
      		for (int i=0; i<components.size(); i++) {
      			JSONArray services = (JSONArray)((JSONObject)components.get(i)).get("services");
      			for (int j=0; j<services.size(); j++) {
      				if (((JSONObject)services.get(j)).get("ips")!=null) {
      					JSONArray ipArray = (JSONArray)((JSONObject)services.get(j)).get("ips");
      					String serviceName = (String)((JSONObject)services.get(j)).get("name");
      					if (ipArray.size()>1) {
      						for (int k=0; k<ipArray.size(); k++) {
      							addIp(serviceName+"-"+k, (String)(ipArray.get(k)));
      						}
      					} else {
      						addIp(serviceName, (String)(ipArray.get(0)));
      					}
      				}
      			}
      		}
          	
      		monitor.worked(45); 
      		
      		monitor.done(); 
      		
        } catch (Exception e) { 
        	setError(true);
        	StringBuffer sb = new StringBuffer("");
        	StackTraceElement[] ste = e.getStackTrace();
        	for (int i=0; i<ste.length; i++) {sb.append(ste[i]+"\n"); }
        	setErrorMessage("An error occured during deployment: "+sb.toString());
        	throw new InvocationTargetException(e); 
        } 
  } 
}
