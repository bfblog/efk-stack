package de.bytefusion.k8s;

import de.bytefusion.k8s.customresource.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class LoggingOperatorMain {

    private Logger log = Logger.getLogger( LoggingOperatorMain.class.getName() );

    @Inject
    private KubernetesClient client;

    @Inject
    private NonNamespaceOperation<LoggingOperator, LoggingOperatorList, LoggingOperatorDoneable, Resource<LoggingOperator, LoggingOperatorDoneable>> loggingOperatorClient;

    @Inject
    private LoggingOperatorCache cache;

    @Inject
    private ElasticsearchController esController;

    void onStartup(@Observes StartupEvent _ev) {
        log.info("starting");
        new Thread(this::runWatch).start();
    }

    void onShutdown(@Observes ShutdownEvent _ev) {
        log.info("shutdown");
    }

    private void runWatch() {
        try {
            log.info("> runWatch()");
            cache.listThenWatch(this::onEvent);
        } catch( Exception e ) {
            log.log(Level.SEVERE, e.getMessage(),e  );
        } finally {
            log.info("< runWatch()");
        }
    }

    public void onEvent(Watcher.Action action, String uid ) {
        log.info("onEvent() action: " + action + " uid: " +uid );
        LoggingOperator entry = cache.get(uid);
        esController.reconcile(entry);
    }
}
