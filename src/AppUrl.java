import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class AppUrl extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = project.getBasePath();

        Shell shell = new Shell();
        shell.forceActive();
        // read config information
        RedeployData rdd = RedeployData.readRedeployDataFromFile(shell, projectPath);
        if (rdd==null) { return; }

        ProgressBarUrl pb = new ProgressBarUrl(rdd);
        ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
        try {
            pmd.run(true, true, pb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!pb.isError()) {
            if (pb.getIps().keySet().size()>0) {
                StringBuffer ipMessage = new StringBuffer("Application last updated "+pb.getLastUpdateTimeLocal()+" ago.\n");
                if (pb.getIps().keySet().size()>1) {
                    for (String key: pb.getIps().keySet()) {
                        ipMessage.append("Click <a href=\"http://"+pb.getIps().get(key)+"\">here</a> to access the service "+ key+".<br>");
                    }
                } else {
                    ipMessage.append("Click <a href=\"http://"+pb.getIps().values().toArray()[0]+"\">here</a> to access your application.");
                }
                MyMessageDialog.openInformation(
                        shell,
                        "appLariat",
                        ipMessage.toString());
            } else {
                MessageDialog.openInformation(
                        shell,
                        "appLariat",
                        "Artifact is now deployed.");
            }
        } else {
            MessageDialog.openInformation(
                    shell,
                    "appLariat",
                    "Error retrieving application URL: \n"+pb.getErrorMessage());
        }

        return;
    }
}
