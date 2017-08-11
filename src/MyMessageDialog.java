
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class MyMessageDialog extends IconAndMessageDialog {
	
	Link linkLabel;	
    public final static int NONE = 0;
    public final static int ERROR = 1;
    public final static int INFORMATION = 2;
    public final static int QUESTION = 3;
    public final static int WARNING = 4;
    public final static int CONFIRM = 5;
    public final static int QUESTION_WITH_CANCEL = 6;
    private String[] buttonLabels;
    private Button[] buttons;
    private int defaultButtonIndex;
    private String title;
    private Image titleImage;
    private Image image = null;

    private Control customArea;

     public MyMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
        super(parentShell);
        this.title = dialogTitle;
        this.titleImage = dialogTitleImage;
        this.message = dialogMessage;

        switch (dialogImageType) {
        case ERROR: {
            this.image = getErrorImage();
            break;
        }
        case INFORMATION: {
            this.image = getInfoImage();
            break;
        }
        case QUESTION:
        case QUESTION_WITH_CANCEL:
        case CONFIRM: {
            this.image = getQuestionImage();
            break;
        }
        case WARNING: {
            this.image = getWarningImage();
            break;
        }
        }
        this.buttonLabels = dialogButtonLabels;
        this.defaultButtonIndex = defaultIndex;
    }

    protected void buttonPressed(int buttonId) {
        setReturnCode(buttonId);
        close();
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
        if (titleImage != null) {
			shell.setImage(titleImage);
		}
    }

    protected void createButtonsForButtonBar(Composite parent) {
        buttons = new Button[buttonLabels.length];
        for (int i = 0; i < buttonLabels.length; i++) {
            String label = buttonLabels[i];
            Button button = createButton(parent, i, label,
                    defaultButtonIndex == i);
            buttons[i] = button;
        }
    }

    protected Control createCustomArea(Composite parent) {
        return null;
    }

    protected Control createDialogArea(Composite parent) {
        // create message area
        createMessageArea(parent);
        // create the top level composite for the dialog area
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        composite.setLayoutData(data);
        // allow subclasses to add custom controls
        customArea = createCustomArea(composite);
        //If it is null create a dummy label for spacing purposes
        if (customArea == null) {
			customArea = new Label(composite, SWT.NULL);
		}
        return composite;
    }

    protected Button getButton(int index) {
        if (buttons == null || index < 0 || index >= buttons.length)
            return null;
        return buttons[index];
    }

    protected int getMinimumMessageWidth() {
        return convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
    }
    protected void handleShellCloseEvent() {
        super.handleShellCloseEvent();
        setReturnCode(SWT.DEFAULT);
    }

    public int open() {
    	return super.open();
    }

	public static boolean open(int kind, Shell parent, String title,
			String message, int style) {
		MyMessageDialog dialog = new MyMessageDialog(parent, title, null, message,
				kind, getButtonLabels(kind), 0);
		style &= SWT.SHEET;
		dialog.setShellStyle(dialog.getShellStyle() | style);
		return dialog.open() == 0;
	}

	static String[] getButtonLabels(int kind) {
		String[] dialogButtonLabels;
		switch (kind) {
		case ERROR:
		case INFORMATION:
		case WARNING: {
			dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL };
			break;
		}
		case CONFIRM: {
			dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL,
					IDialogConstants.CANCEL_LABEL };
			break;
		}
		case QUESTION: {
			dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL };
			break;
		}
		case QUESTION_WITH_CANCEL: {
			dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
                    IDialogConstants.NO_LABEL,
                    IDialogConstants.CANCEL_LABEL };
			break;
		}
		default: {
			throw new IllegalArgumentException(
					"Illegal value for kind in MessageDialog.open()"); //$NON-NLS-1$
		}
		}
		return dialogButtonLabels;
	}
    
    public static boolean openConfirm(Shell parent, String title, String message) {
        return open(CONFIRM, parent, title, message, SWT.NONE);
    }

    public static void openError(Shell parent, String title, String message) {
        open(ERROR, parent, title, message, SWT.NONE);
    }

    public static void openInformation(Shell parent, String title,
            String message) {
        open(INFORMATION, parent, title, message, SWT.NONE);
    }
    public static boolean openQuestion(Shell parent, String title,
            String message) {
        return open(QUESTION, parent, title, message, SWT.NONE);
    }

    public static void openWarning(Shell parent, String title, String message) {
        open(WARNING, parent, title, message, SWT.NONE);
    }

    protected Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
        if (defaultButton && !customShouldTakeFocus()) {
			button.setFocus();
		}
        return button;
    }
    
	protected Control createMessageArea(Composite composite) {		
		Image image = getImage();
		if (image != null) {
			imageLabel = new Label(composite, SWT.NULL);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING)
					.applyTo(imageLabel);
		}
		// create message
		if (message != null) {
			linkLabel = new Link(composite, getMessageLabelStyle());
			linkLabel.setText(message);
			
			linkLabel.addSelectionListener(new SelectionAdapter(){
		        @Override
		        public void widgetSelected(SelectionEvent e) {
		               System.out.println("You have selected: "+e.text);
		               try {
		                //  Open default external browser
                           org.eclipse.swt.program.Program.launch(e.text);
		                //PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(e.text));
		              } 
		             catch (Exception ex) {
                         // TODO Auto-generated catch block
                         ex.printStackTrace();
                     }
		        }
		    });
			
			GridDataFactory
					.fillDefaults()
					.align(SWT.FILL, SWT.BEGINNING)
					.grab(true, false)
					.hint(
							convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
							SWT.DEFAULT).applyTo(linkLabel);
		}
		return composite;
	}
    

    protected boolean customShouldTakeFocus() {
        if (customArea instanceof Label) {
			return false;
		}
        if (customArea instanceof CLabel) {
			return (customArea.getStyle() & SWT.NO_FOCUS) > 0;
		}
        return true;
    }

    public Image getImage() {
        return image;
    }

    protected String[] getButtonLabels() {
        return buttonLabels;
    }

    protected int getDefaultButtonIndex() {
        return defaultButtonIndex;
    }
    protected void setButtons(Button[] buttons) {
        if (buttons == null) {
            throw new NullPointerException(
                    "The array of buttons cannot be null.");} 
        this.buttons = buttons;
    }

    protected void setButtonLabels(String[] buttonLabels) {
        if (buttonLabels == null) {
            throw new NullPointerException(
                    "The array of button labels cannot be null.");}
        this.buttonLabels = buttonLabels;
    }
}
