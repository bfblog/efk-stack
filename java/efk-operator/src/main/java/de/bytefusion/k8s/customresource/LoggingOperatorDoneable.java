package de.bytefusion.k8s.customresource;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class LoggingOperatorDoneable extends CustomResourceDoneable<LoggingOperator> {

    public LoggingOperatorDoneable(LoggingOperator resource, Function<LoggingOperator, LoggingOperator> function) {
        super(resource, function);
    }
}
