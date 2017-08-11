
// Written by Mazda Marvasti: AppLariat Corp.

public class InitialReleaseData {

	private ReleaseData release;
	private ArtifactData artifact;
	private DeployLocationData deployLoc;
	private SelectedArtifactLocation artifactLoc;
    
	public InitialReleaseData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReleaseData getRelease() {
		return release;
	}

	public void setRelease(ReleaseData release) {
		this.release = release;
	}

	public ArtifactData getArtifact() {
		return artifact;
	}

	public void setArtifact(ArtifactData artifact) {
		this.artifact = artifact;
	}

	public DeployLocationData getDeployLoc() {
		return deployLoc;
	}

	public void setDeployLoc(DeployLocationData deployLoc) {
		this.deployLoc = deployLoc;
	}

	public SelectedArtifactLocation getArtifactLoc() {
		return artifactLoc;
	}

	public void setArtifactLoc(SelectedArtifactLocation artifactLoc) {
		this.artifactLoc = artifactLoc;
	}	
}
