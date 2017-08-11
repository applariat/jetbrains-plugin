
import java.util.ArrayList;
import java.util.List;

public class ArtifactLocationSelectionData {
	String name;
	String id;
	List<String> owner;
	public ArtifactLocationSelectionData(String name, String id) {
		super();
		this.name=name;
		this.id=id;
		this.owner=new ArrayList<String>();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getOwner() {
		return owner;
	}
	public void setOwner(List<String> owner) {
		this.owner = owner;
	}
	public void addOwner(String oneOwner) {
		this.owner.add(oneOwner);
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
