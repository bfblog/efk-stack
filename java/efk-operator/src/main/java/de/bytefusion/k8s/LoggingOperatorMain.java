package de.bytefusion.k8s;

import de.bytefusion.k8s.customresource.LoggingOperator;
import de.bytefusion.k8s.customresource.LoggingOperatorDoneable;
import de.bytefusion.k8s.customresource.LoggingOperatorList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LoggingOperatorMain {

    private Logger log = Logger.getLogger( LoggingOperatorMain.class.getName() );

    @Inject
    private KubernetesClient client;

    @Inject
    private NonNamespaceOperation<LoggingOperator, LoggingOperatorList, LoggingOperatorDoneable, Resource<LoggingOperator, LoggingOperatorDoneable>> loggingOperatorClient;

    void onStartup(@Observes StartupEvent _ev) {
        log.info("starting");
        new Thread(this::runWatch).start();
    }

    void onShutdown(@Observes ShutdownEvent _ev) {
        log.info("shutdown");
    }

    private void runWatch() {
        try {
            List<String> allNodes = IntStream.range(1, 10).mapToObj(xx -> "odfe-node" + String.valueOf(xx)).collect(Collectors.toList());
            log.info("> runWatch()");
            List<Pod> existingPods = client.pods()
                    .list()
                    .getItems()
                    .stream()
                    .filter( pod -> pod.getMetadata() != null)
                    .filter( pod -> pod.getMetadata().getLabels() != null )
                    .filter(pod -> allNodes.contains(pod.getMetadata().getLabels().get("name")))
                    .collect(Collectors.toList());
            Pod pod = client.pods().load(getClass().getResourceAsStream("/elasticsearch-pod.yaml")).get();
            pod.getSpec();
            client.pods().create(pod);
        } catch( Exception e ) {
            log.log(Level.SEVERE, e.getMessage(),e  );
        } finally {
            log.info("< runWatch()");
        }
    }
}
