import java.util.ArrayList;
import java.util.List;

public class Node {
    private String nodeIP;
    private String nodeType;
    private String nodeResource;
    private String nodeName;
    //private String associatednodeName;

    //private List<SensorValue> values = new ArrayList<SensorValue>();
    
    public Node(String nodeName, String nodeType, String nodeResource, String nodeIP) {
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.nodeResource = nodeResource;
    	this.nodeIP = nodeIP;
    }

    public String getNodeIP() {
            return nodeIP;
    }

    public void setnodeIP(String nodeIP) {
            this.nodeIP = nodeIP;
    }

    public String getnodeType() {
            return nodeType;
    }

    public void setnodeType(String nodeType) {
            this.nodeType = nodeType;
    }

    public String getnodeResource() {
            return nodeResource;
    }

    public void setnodeResource(String nodeResource) {
            this.nodeResource = nodeResource;
    }

    public String getnodeName() {
            return nodeName;
    }

    public void setnodeName(String nodeName) {
            this.nodeName = nodeName;
    }

    /*public String getAssociatednodeName() {
            return associatednodeName;
    }

    public void setAssociatednodeName(String associatednodeName) {
            this.associatednodeName = associatednodeName;
    }

    public List<SensorValue> getValues() {
            return values;
    }

    public void setValues(List<SensorValue> values) {
            this.values = values;
    }*/
    
    
    
}
