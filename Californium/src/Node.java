import java.util.ArrayList;
import java.util.List;

public class Node {
    private String nodeName;
    private String nodeIP;
    private String nodeType;
    private String nodeResource;
    private List<ResourceValue> values = new ArrayList<>();

    public Node(String nodeName, String nodeType, String nodeResource, String nodeIP) {
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.nodeResource = nodeResource;
    	this.nodeIP = nodeIP;
    }

    public String getNodeIP() {
            return nodeIP;
    }

    public void setNodeIP(String nodeIP) {
            this.nodeIP = nodeIP;
    }

    public String getNodeType() {
            return nodeType;
    }

    public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
    }

    public String getNodeResource() {
            return nodeResource;
    }

    public void setNodeResource(String nodeResource) {
            this.nodeResource = nodeResource;
    }

    public String getNodeName() {
            return nodeName;
    }

    public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
    }
    public void setValues(String val) { this.values.add(new ResourceValue(val)); }
}
