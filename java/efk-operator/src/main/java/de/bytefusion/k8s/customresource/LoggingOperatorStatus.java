package de.bytefusion.k8s.customresource;

public class LoggingOperatorStatus {

    private ElasticsarchStatus esStatus;

    public ElasticsarchStatus getEsStatus() {
        return esStatus;
    }

    public void setEsStatus(ElasticsarchStatus esStatus) {
        this.esStatus = esStatus;
    }
}
