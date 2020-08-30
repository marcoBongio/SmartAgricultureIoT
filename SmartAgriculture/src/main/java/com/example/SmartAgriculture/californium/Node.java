package com.example.SmartAgriculture.californium;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String nodeName;
    private String nodeIP;
    private String nodeType;
    private String nodeResource;
    private String currentValue;
    private List<ResourceValue> values = new ArrayList<>();
    private List<String> linkedNodes = new ArrayList<>();

    public Node(String nodeName, String nodeType, String nodeResource, String nodeIP) {
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.nodeResource = nodeResource;
    	this.nodeIP = nodeIP;
    }

    public String getNodeIP() {
            return nodeIP;
    }
    public String getNodeType() {
        return nodeType;
    }
    public String getNodeResource() {
        return nodeResource;
    }
    public String getCurrentValue() {return currentValue;}
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeIP(String nodeIP) {
            this.nodeIP = nodeIP;
    }
    public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
    }
    public void setNodeResource(String nodeResource) {
            this.nodeResource = nodeResource;
    }
    public void setCurrentValue(String val) { this.currentValue = val; }
    public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
    }
    public void setValues(String val) { this.values.add(new ResourceValue(val)); setCurrentValue(val); }

    public List<String> getLinkedNodes() {
        return linkedNodes;
    }

    public void addLinkedNode(String name){
        this.linkedNodes.add(name);
    }
}
