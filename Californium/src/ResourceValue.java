import java.sql.Timestamp;

public class ResourceValue {
    Double value;
    Timestamp timestamp;

    public ResourceValue(String value){
        this.value = Double.parseDouble(value);
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Double getValue() { return value; }
    public Timestamp getTimestamp() { return timestamp;}

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp;}
    public void setValue(Double value) {this.value = value;}
}