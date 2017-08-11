
// Written by Mazda Marvasti: AppLariat Corp.

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class MyTitleAreaDialog extends TitleAreaDialog {

    private Combo comboDeployType;    
    private List<String> deployTypeList= new ArrayList<String>();
    
    boolean silentDeploy=true;
    boolean cancelPressed=false;
    
    public MyTitleAreaDialog(Shell parentShell) {
        super(parentShell);
        deployTypeList.add("Silent Deploy (get app URL Later)");
        deployTypeList.add("Active Deploy (wait to get app URL)");
    }
    
    public void create() {
        super.create();
        setTitle("appLariat Application Deployment");
        setMessage("Deploy your latest code...", IMessageProvider.INFORMATION);
    }

    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        createDeployType(container);

        return area;
    }
   
    private void createDeployType(Composite container) {
        Label lbtDeployType = new Label(container, SWT.NONE);
        lbtDeployType.setText("Deploy Type");

        GridData dataDeployType = new GridData();
        dataDeployType.grabExcessHorizontalSpace = true;
        dataDeployType.horizontalAlignment = GridData.FILL;
        comboDeployType = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboDeployType.setLayoutData(dataDeployType);
        
        // at some point I should sort these values first before displaying them
        for (String dt: deployTypeList) {
        	comboDeployType.add(dt);
        }
        comboDeployType.select(0);        
    }
    
    protected boolean isResizable() {
        return true;
    }

    private void saveInput() {
        String deployType = comboDeployType.getText();
        if (deployType.indexOf("Active")>=0) {
        	silentDeploy=false;
        }
    }

    protected void okPressed() {
        saveInput();
        super.okPressed();
    }
    protected void cancelPressed() {
        cancelPressed=true;
        super.cancelPressed();
    }

	public boolean isSilentDeploy() {
		return silentDeploy;
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}
	
}
