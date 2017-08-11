
//Written by Mazda Marvasti: AppLariat Corp.

import java.io.Serializable;

public class ArtifactData implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String id;
	String name;
	String stackComponentId;
	String componentName;
	String componentServiceId;
	public ArtifactData(String id, String name, String stackComponentId, String componentServiceId, String componentName) {
		super();
		this.id = id;
		this.name = name;
		this.componentName = componentName;
		this.componentServiceId = componentServiceId;
		this.stackComponentId = stackComponentId;
	}
	public ArtifactData() {
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
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	public String getStackComponentId() {
		return stackComponentId;
	}
	public void setStackComponentId(String stackComponentId) {
		this.stackComponentId = stackComponentId;
	}
	public String getComponentServiceId() {
		return componentServiceId;
	}
	public void setComponentServiceId(String componentServiceId) {
		this.componentServiceId = componentServiceId;
	}
}
