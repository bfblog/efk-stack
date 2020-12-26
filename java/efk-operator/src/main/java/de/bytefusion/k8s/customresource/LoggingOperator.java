package de.bytefusion.k8s.customresource;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

public class LoggingOperator extends CustomResource {

    private LoggingOperatorSpec spec;

    private LoggingOperatorStatus status;

    @Override
    public ObjectMeta getMetadata() {
        return super.getMetadata();
    }

    public LoggingOperatorSpec getSpec() {
        return spec;
    }

    public void setSpec(LoggingOperatorSpec spec) {
        this.spec = spec;
    }

    public LoggingOperatorStatus getStatus() {
        return status;
    }

    public void setStatus(LoggingOperatorStatus status) {
        this.status = status;
    }
}
