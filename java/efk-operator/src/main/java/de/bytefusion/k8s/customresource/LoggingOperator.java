package de.bytefusion.k8s.customresource;

import io.fabric8.kubernetes.client.CustomResource;

public class LoggingOperator extends CustomResource {

    private LoggingOperatorSpec spec;

    public LoggingOperatorSpec getSpec() {
        return spec;
    }

    public void setSpec(LoggingOperatorSpec spec) {
        this.spec = spec;
    }

}
