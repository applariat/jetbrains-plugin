
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.eclipse.swt.widgets.Shell;

// Written by Mazda Marvasti: AppLariat Corp.

import java.io.Serializable;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;

public class RedeployData implements Serializable{

	private static final long serialVersionUID = 1L;	

	private DeployData deployData;
	private ArtifactData artifactData;
	private String repositoryOwner;
	private String repositoryName;
    private String repositoryBranch;
    private String authToken;
    private String jwtToken;
    private String apiUrl;
    
	public RedeployData() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public DeployData getDeployData() {
		return deployData;
	}

	public void setDeployData(DeployData deployData) {
		this.deployData = deployData;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getRepositoryOwner() {
		return repositoryOwner;
	}
	public void setRepositoryOwner(String repositoryOwner) {
		this.repositoryOwner = repositoryOwner;
	}
	
	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public String getRepositoryBranch() {
		return repositoryBranch;
	}
	public void setRepositoryBranch(String repositoryBranch) {
		this.repositoryBranch = repositoryBranch;
	}
	
	public String getNewBuildHash() {
		long time = (new java.util.Date()).getTime();
		return Long.toHexString(time);
	}
	
	public ArtifactData getArtifactData() {
		return artifactData;
	}

	public void setArtifactData(ArtifactData artifactData) {
		this.artifactData = artifactData;
	}

	public static String getNewLocUrl(String location, String owner, String name, String branch) {
		if (location.equalsIgnoreCase("github")) {
			return "https://github.com/"+owner+"/"+name+"/archive/"+branch+".zip";
		} else if (location.equalsIgnoreCase("bitbucket")) {
			return "https://bitbucket.org/"+owner+"/"+name+"/branch/"+branch+".zip";
		} else { // default to GitHub
			return "https://github.com/"+owner+"/"+name+"/archive/"+branch+".zip";
		}
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	
	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public static void initToken(RedeployData rdd) {
		String apiUrl = UrlCalls.replaceApiHostPlaceholder("$APL_API_BASE_URL");
		rdd.setApiUrl(apiUrl);
		rdd.setJwtToken(UrlCalls.urlConnectRequestToken(apiUrl, rdd.getAuthToken()));
	}
	
	public static RedeployData readRedeployDataFromFileForAuth(String projectPath) { // only for determining if Auth data exists
		File configFile = new File(projectPath+"/applariat_config");
		RedeployData rdd=null;
		if (configFile.exists()) {
			try(
				InputStream file = new FileInputStream(configFile);
			    InputStream buffer = new BufferedInputStream(file);
			    ObjectInput input = new ObjectInputStream (buffer);
			    ){
			      //deserialize the List
			      rdd = (RedeployData)input.readObject();
			      //display its data
                input.close();
			} catch(Exception e){				
				return null;
			}
		} else {
			return null;			
		}

		return rdd;
	}
	
	public static RedeployData readRedeployDataFromFile(Shell shell, String projectPath) {
		// read config information
		File configFile = new File(projectPath+"/applariat_config");
		RedeployData rdd=null;
		if (configFile.exists()) {
			try(
				InputStream file = new FileInputStream(configFile);
			    InputStream buffer = new BufferedInputStream(file);
			    ObjectInput input = new ObjectInputStream (buffer);
			    ){
			      //deserialize the List
			      rdd = (RedeployData)input.readObject();
			      //display its data
                input.close();
			} catch(Exception e){
				MessageDialog.openInformation(shell,"appLariat","Error reading config information. "+e.toString());
				return null;
			}
		} else {
			MessageDialog.openInformation(shell,"appLariat","Please configure deployment first through the Config menu. ");
			return null;			
		}
		
		// now validate that the authToken is available and good.
		
		if (rdd.getAuthToken()==null || rdd.getAuthToken().length()<2) { // No credentials on File. Must go to Config first.
			MessageDialog.openInformation(
					shell,
					"appLariat",
					"No login information available. Please go to Configure menu items.");		
			return null;
		}
		String apiUrl = UrlCalls.replaceApiHostPlaceholder("$APL_API_BASE_URL");
		rdd.setApiUrl(apiUrl);
		String jwtToken = UrlCalls.urlConnectRequestToken(apiUrl, rdd.getAuthToken());
		// if jwtToken is null we need to have a different credentials as it is no longer valid.
		if (jwtToken ==null) {
			rdd.setAuthToken(null);
			RedeployData.writeRedeployDataToFile(rdd, shell, projectPath); // this is so that from config we know to ask for username and password again.
			MessageDialog.openInformation(
					shell,
					"appLariat",
					"Invalid Credentials. Please go to Configure menu and re-enter username and password.");		
			return null;
		}
		rdd.setJwtToken(jwtToken);
		return rdd;
	}
	
	public static void writeRedeployDataToFile(RedeployData redeployData, Shell shell, String projectPath) {
		File configFile = new File(projectPath+"/applariat_config");
		String fullPath = configFile.getAbsolutePath();
		try (
		    OutputStream file = new FileOutputStream(configFile);
		    OutputStream buffer = new BufferedOutputStream(file);
		    ObjectOutput output = new ObjectOutputStream(buffer)
        ){
		    output.writeObject(redeployData);
		    output.close();
		}  catch(IOException ex){
			StackTraceElement[] st = ex.getStackTrace();
			StringBuffer sb = new StringBuffer("");
			for (StackTraceElement ste: st) {
				sb.append(ste.toString()+"\n");
			}
			MessageDialog.openInformation(
					shell,
					"appLariat",
					"Unable to write config data :  "+fullPath+"    "+ex.toString());
		}

	}
	
	public static void deleteRedeployDataFile() {
		File configFile = new File("./applariat_config");
		
		configFile.delete();
	}
}
