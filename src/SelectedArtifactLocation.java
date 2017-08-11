
public class SelectedArtifactLocation {
	private String id;
	private String name;
	private String owner;
	private String repoName;
	private String repoBranch;
	
	public SelectedArtifactLocation(String id, String name, String owner, String repoName, String repoBranch) {
		super();
		this.id = id;
		this.name=name;
		this.owner = owner;
		this.repoName = repoName;
		this.repoBranch = repoBranch;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getRepoBranch() {
		return repoBranch;
	}

	public void setRepoBranch(String repoBranch) {
		this.repoBranch = repoBranch;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getArtifactLocationName() {
		return "owner/"+this.getOwner()+"/repository/"+this.getRepoName()+"/branch/"+this.getRepoBranch();
	}
}
