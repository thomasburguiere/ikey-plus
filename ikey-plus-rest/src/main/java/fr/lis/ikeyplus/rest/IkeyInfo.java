package fr.lis.ikeyplus.rest;

public class IkeyInfo {
    public String getVersion() {
        return version;
    }

    private final String version;

    public IkeyInfo(){
        this(null);
    }

    public IkeyInfo(String version) {
        this.version = version;
    }
}
