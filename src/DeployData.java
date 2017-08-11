
//Written by Mazda Marvasti: AppLariat Corp.

import java.io.Serializable;

public class DeployData implements Serializable{

	private static final long serialVersionUID = 1L;

	String id;
	String name;
	String stackId;
	public DeployData(String id, String name, String stackId) {
		super();
		this.id = id;
		this.name = name;
		this.stackId = stackId;
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
}
