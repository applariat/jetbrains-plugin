
import java.awt.Event;

// Written by: Mazda Marvasti, AppLariat Corp.

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MyComboAreaDialog extends TitleAreaDialog {

    private Combo comboArtifactName;
    private Combo comboReleaseName;
    private Combo comboDeployLocationName;
    private Combo comboArtifactLocationName;    
    private Combo comboRepositoryOwner;
    private Combo comboRepositoryName;
    private Combo comboRepositoryBranch;

    private InitialReleaseData initReleaseData = new InitialReleaseData();
    private boolean cancelPressed = false;
    private String artLocId; 
    
    private String apiUrl;
    private String jwtToken;

    private List<ReleaseData> comboData;
    private List<DeployLocationData> deployLocationList;
    private List<ArtifactLocationSelectionData> artLocationList;

    public MyComboAreaDialog(Shell parentShell, List<ReleaseData> releaseData, List<DeployLocationData> dll, List<ArtifactLocationSelectionData> lalsd, String apiUrl, String jwtToken) {
        super(parentShell);
        this.comboData = releaseData;
        this.deployLocationList=dll;
        this.artLocationList=lalsd;
        this.apiUrl=apiUrl;
        this.jwtToken=jwtToken;
    }
    
    public void create() {
        super.create();
        setTitle("appLariat Application Configuration");
        setMessage("Configure your app for continous deployment...", IMessageProvider.INFORMATION);
    }

    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        createReleaseName(container);
        createArtifactName(container);
        createDeployLocationName(container);        
        createArtifactLocName(container);
        
        createRepositoryOwner(container);
        createRepositoryName(container);
        createRepositoryBranch(container);
        
        return area;
    }

    private void createArtifactLocName(Composite container) {
        Label lbtArtifactLocName = new Label(container, SWT.NONE);
        lbtArtifactLocName.setText("Location of New Code:");

        GridData dataArtifactLocName = new GridData();
        dataArtifactLocName.grabExcessHorizontalSpace = true;
        dataArtifactLocName.horizontalAlignment = GridData.FILL;
        comboArtifactLocationName = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboArtifactLocationName.setLayoutData(dataArtifactLocName);
        
        for (ArtifactLocationSelectionData alsd: artLocationList) {
        	comboArtifactLocationName.add(alsd.getName());
        }
//        comboArtifactLocationName.select(0);
        
        comboArtifactLocationName.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		String firstSelection = comboArtifactLocationName.getText();
        		for(ArtifactLocationSelectionData dld: artLocationList) {
                	if (dld.getName().equals(comboArtifactLocationName.getText())) {
                		artLocId = dld.getId();
                		break;
                	}
                }
        		List<String> ownerNameList = null;
        		for (ArtifactLocationSelectionData alsd: artLocationList) {
        			if (alsd.getName().equals(firstSelection)) {
        				ownerNameList=alsd.getOwner();
        				
        				break;
        			}
        		}
        	            		
                comboRepositoryOwner.removeAll();
                for (String repoOwnerName: ownerNameList) {
                	comboRepositoryOwner.add(repoOwnerName);
                }
//                comboRepositoryOwner.select(0);
        	}
        });
    }
    private void createRepositoryOwner(Composite container) {
    	Label lbtRepOwnerName = new Label(container, SWT.NONE);
        lbtRepOwnerName.setText("Repository Owner");

        GridData dataRepOwner = new GridData();
        dataRepOwner.grabExcessHorizontalSpace = true;
        dataRepOwner.horizontalAlignment = GridData.FILL;

        comboRepositoryOwner = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboRepositoryOwner.setLayoutData(dataRepOwner);
        
        comboRepositoryOwner.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {        		
        		comboRepositoryName.removeAll(); 
        	    // call API to get list of available repo names
        		try {
        			JSONParser parser = new JSONParser();
        			String query="browse="+"owner/"+comboRepositoryOwner.getText()+"/repository";
        			String rData = UrlCalls.urlConnectGet(apiUrl+"/loc_artifacts/"+artLocId, query, jwtToken);
        			JSONObject jo1 = (JSONObject)parser.parse(rData);
        			JSONArray ja1 = (JSONArray)((JSONObject)((JSONObject)jo1.get("data")).get("browse")).get("list");
        			for (int i=0; i<ja1.size(); i++) {
        				comboRepositoryName.add((String)((JSONObject)ja1.get(i)).get("value"));
        			}
        		} catch (Exception e) {
        			e.printStackTrace();
        			System.out.println("Unable to get repository name from API: "+e.toString());
        			comboRepositoryName.add("Error: Unable to get data from API");
        		}
        		                                             
//                comboRepositoryName.select(0);
        	}
        });
    }
/*
    private void createRepositoryName(Composite container) {
        Label lbtRepName = new Label(container, SWT.NONE);
        lbtRepName.setText("Repository Name");

        GridData dataRepName = new GridData();
        dataRepName.grabExcessHorizontalSpace = true;
        dataRepName.horizontalAlignment = GridData.FILL;

        txtRepositoryName = new Text(container, SWT.BORDER);
        txtRepositoryName.setLayoutData(dataRepName);
    }
*/    
    private void createRepositoryName(Composite container) {
    	Label lbtRepName = new Label(container, SWT.NONE);
        lbtRepName.setText("Repository Name");

        GridData dataRepName = new GridData();
        dataRepName.grabExcessHorizontalSpace = true;
        dataRepName.horizontalAlignment = GridData.FILL;

        comboRepositoryName = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboRepositoryName.setLayoutData(dataRepName);
        
        comboRepositoryName.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		comboRepositoryBranch.removeAll(); 
        	    // call API to get list of available Branches
        		try {
        			JSONParser parser = new JSONParser();
        			String query="browse="+"owner/"+comboRepositoryOwner.getText()+"/repository/"+comboRepositoryName.getText()+"/branch";
        			String rData = UrlCalls.urlConnectGet(apiUrl+"/loc_artifacts/"+artLocId, query, jwtToken);
        			JSONObject jo1 = (JSONObject)parser.parse(rData);
        			JSONArray ja1 = (JSONArray)((JSONObject)((JSONObject)jo1.get("data")).get("browse")).get("list");
        			for (int i=0; i<ja1.size(); i++) {
        				comboRepositoryBranch.add((String)((JSONObject)ja1.get(i)).get("value"));
        			}

        		} catch (Exception e) {
        			e.printStackTrace();
        			System.out.println("Unable to get repository Branch from API: "+e.toString());
        			comboRepositoryBranch.add("Error: Unable to get data from API");
        		}
        		                                             
//                comboRepositoryBranch.select(0);
        	}
        });
    }

    /*
    private void createRepositoryBranch(Composite container) {
        Label lbtRepBranchName = new Label(container, SWT.NONE);
        lbtRepBranchName.setText("Repository Branch");

        GridData dataRepBranch = new GridData();
        dataRepBranch.grabExcessHorizontalSpace = true;
        dataRepBranch.horizontalAlignment = GridData.FILL;

        txtRepositoryBranch = new Text(container, SWT.BORDER);
        txtRepositoryBranch.setLayoutData(dataRepBranch);
    }
*/    
    private void createRepositoryBranch(Composite container) {
    	Label lbtRepBranchName = new Label(container, SWT.NONE);
        lbtRepBranchName.setText("Repository Branch");

        GridData dataRepBranch = new GridData();
        dataRepBranch.grabExcessHorizontalSpace = true;
        dataRepBranch.horizontalAlignment = GridData.FILL;
        
        comboRepositoryBranch = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboRepositoryBranch.setLayoutData(dataRepBranch);
        
      
    }
    private void createReleaseName(Composite container) {
        Label lbtDeployName = new Label(container, SWT.NONE);
        lbtDeployName.setText("App. Release Name");

        GridData dataDeployName = new GridData();
        dataDeployName.grabExcessHorizontalSpace = true;
        dataDeployName.horizontalAlignment = GridData.FILL;

        comboReleaseName = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboReleaseName.setLayoutData(dataDeployName);
        // at some point I should sort these values first before displaying them
        for (ReleaseData dd: comboData) {
        	comboReleaseName.add(dd.getName());
        }
        comboReleaseName.select(0);
        
        comboReleaseName.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		String firstSelection = comboReleaseName.getText();
        		ReleaseData ddSelected = null;
        	        
        	    for (ReleaseData st : comboData) {
        	    	if (st.getName().equals(firstSelection)) {
        	    		ddSelected = st;
        	     	break;
        	       	}
        	    }
                comboArtifactName.removeAll();
                for (ArtifactData ad: ddSelected.getArtifacts()) {
                	comboArtifactName.add(ad.getComponentName()+" ; "+ad.getName());
                }
                comboArtifactName.select(0);
        	}
        });
    }
    private void createArtifactName(Composite container) {
        Label lbtArtifactName = new Label(container, SWT.NONE);
        lbtArtifactName.setText("Artifact to Replace");

        GridData dataArtifactName = new GridData();
        dataArtifactName.grabExcessHorizontalSpace = true;
        dataArtifactName.horizontalAlignment = GridData.FILL;
        comboArtifactName = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboArtifactName.setLayoutData(dataArtifactName);
        
        List<ArtifactData> artifacts=null;
        String firstSelection = comboReleaseName.getText();
        for (ReleaseData dd : comboData) {
        	if (dd.getName().equals(firstSelection)) {
        		artifacts = dd.getArtifacts();
        		break;
        	}
        }
        for (ArtifactData ad: artifacts) {
        	comboArtifactName.add(ad.getComponentName()+" ; "+ad.getName());
        }
        comboArtifactName.select(0);
    }

    private void createDeployLocationName(Composite container) {
        Label lbtDeployLocName = new Label(container, SWT.NONE);
        lbtDeployLocName.setText("Deploy Location");

        GridData dataDeployLocName = new GridData();
        dataDeployLocName.grabExcessHorizontalSpace = true;
        dataDeployLocName.horizontalAlignment = GridData.FILL;
        comboDeployLocationName = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboDeployLocationName.setLayoutData(dataDeployLocName);
        
        // at some point I should sort these values first before displaying them
        for (DeployLocationData dld: deployLocationList) {
        	comboDeployLocationName.add(dld.getName());
        }
        comboDeployLocationName.select(0);        
    }
    
    private void saveInput() {
        String tmpReleaseName = comboReleaseName.getText();
        String tmpComponentName = comboArtifactName.getText().split(";")[0].trim();
        String tmpArtifactName = comboArtifactName.getText().split(";")[1].trim(); 
        String tmpDeployLocName = comboDeployLocationName.getText();  
        
        for (ReleaseData dd: comboData) {
        	if (dd.getName().equals(tmpReleaseName)) {
        		for (ArtifactData ad: dd.getArtifacts()) {
        			if (ad.getComponentName().equals(tmpComponentName) && ad.getName().equals(tmpArtifactName)) {
        				initReleaseData.setRelease(dd);        
        				initReleaseData.setArtifact(ad);
        				break;
        			}
        		}
        		break;
        	}
        }
        
        for(DeployLocationData dld: deployLocationList) {
        	if (dld.getName().equals(tmpDeployLocName)) {
        		initReleaseData.setDeployLoc(dld);
        		break;
        	}
        }
        SelectedArtifactLocation sal = new SelectedArtifactLocation(artLocId, comboArtifactLocationName.getText(), comboRepositoryOwner.getText(), comboRepositoryName.getText(), comboRepositoryBranch.getText());
        initReleaseData.setArtifactLoc(sal);
    }

    protected void okPressed() {
        saveInput();
        super.okPressed();
    }
    protected void cancelPressed() {
        cancelPressed=true;
        super.cancelPressed();
    }

	public InitialReleaseData getInitReleaseData() {
		return initReleaseData;
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

}
