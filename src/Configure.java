import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class Configure extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = project.getBasePath();

        Shell shell = new Shell();
        shell.forceActive();

        RedeployData rdd = RedeployData.readRedeployDataFromFileForAuth(projectPath);
        boolean needCredentials = false;
        if (rdd!=null && rdd.getAuthToken()!=null) { // now get the token for this auth
            RedeployData.initToken(rdd);
            if (rdd.getJwtToken()==null) { // username/password is invalid
                needCredentials = true;
            }
        } else { // need to ask for username and password
            needCredentials=true;
        }

        if (needCredentials) {
            do {
                MyLoginAreaDialog lDialog = new MyLoginAreaDialog(shell);
                lDialog.create();
                lDialog.open();
                if (lDialog.isCancelPressed()) { return; }
                rdd = lDialog.getRedeployData();
            } while(rdd.getJwtToken()==null);
        }

        RetrieveConfigData rcd = new RetrieveConfigData(rdd);
        ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
        try {
            pmd.run(true, true, rcd);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (!rcd.isError()) {
            MyComboAreaDialog dialog = new MyComboAreaDialog(shell, rcd.getReleases(), rcd.getDeployLocList(), rcd.getArtifactLocSelectionList(), rdd.getApiUrl(), rdd.getJwtToken());
            dialog.create();
            dialog.open();

            if (dialog.isCancelPressed()) { return; }

            if (dialog.getErrorMessage()!=null && dialog.getErrorMessage().length()>1) {
                MessageDialog.openInformation(
                        shell,
                        "appLariat",
                        "Error In Input Data:  "+dialog.getErrorMessage());
                return;
            }

            // now do the first deploy of the application with the override
            ConfigValidateProgressBar cvpb = new ConfigValidateProgressBar(dialog.getInitReleaseData(), rdd.getApiUrl(), rdd.getJwtToken());
            ProgressMonitorDialog pmd1 = new ProgressMonitorDialog(shell);
            try {
                pmd1.run(true, true, cvpb);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            RedeployData redeployData = cvpb.getRedeployData();
            redeployData.setAuthToken(rdd.getAuthToken());
            redeployData.setJwtToken(rdd.getJwtToken());
            redeployData.setApiUrl(rdd.getApiUrl());
            RedeployData.writeRedeployDataToFile(redeployData, shell, projectPath);

            MessageDialog.openInformation(
                    shell,
                    "appLariat",
                    "Currently deploying the application. Check it's status by going to the 'App URL' menu.");

        } else {
            MessageDialog.openInformation(
                    shell,
                    "appLariat",
                    "Error retrieving info:  " + rcd.getErrorMessage());
        }

    }
}
