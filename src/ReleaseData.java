
//Written by Mazda Marvasti: AppLariat Corp.

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReleaseData implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String id;
	String name;
	String stackId;
	List<ArtifactData> artifacts;
	
	public ReleaseData(String id, String name, String stackId) {
		super();
		this.id = id;
		this.name = name;
		this.stackId = stackId;
		this.artifacts = new ArrayList<ArtifactData>();
	}
	public ReleaseData() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStackId() {
		return stackId;
	}
	public void setStackId(String stackId) {
		this.stackId = stackId;
	}
	public List<ArtifactData> getArtifacts() {
		return artifacts;
	}
	public void setArtifacts(List<ArtifactData> artifacts) {
		this.artifacts = artifacts;
	}
	public void addArtifact(String artifactId, String artifactName, String stackComponentId, String componentServiceId, String componentName) {
		this.artifacts.add(new ArtifactData(artifactId, artifactName, stackComponentId, componentServiceId, componentName));
	}
}
