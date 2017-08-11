import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.eclipse.swt.widgets.Shell;

public class Logout extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = project.getBasePath();

        Shell shell = new Shell();

        // read config information
        RedeployData rdd = RedeployData.readRedeployDataFromFile(shell, projectPath);
        if (rdd==null) { return; }

        rdd.setAuthToken(null);

        RedeployData.writeRedeployDataToFile(rdd, shell, projectPath);
    }
}
