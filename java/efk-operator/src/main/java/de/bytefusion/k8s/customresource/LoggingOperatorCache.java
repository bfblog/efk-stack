package de.bytefusion.k8s.customresource;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.util.logging.Level;
import java.util.logging.Logger;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@ApplicationScoped
public class LoggingOperatorCache {

    private final Logger log = Logger.getLogger(LoggingOperatorCache.class.getName());

    private final Map<String, LoggingOperator> cache = new ConcurrentHashMap<>();

    private final Map<String, Pod> podCache = new ConcurrentHashMap<>();

    @Inject
    private NonNamespaceOperation<LoggingOperator, LoggingOperatorList,LoggingOperatorDoneable, Resource<LoggingOperator, LoggingOperatorDoneable>> crClient;

    @Inject
    KubernetesClient client;

    private Executor executor = Executors.newSingleThreadExecutor();

    public LoggingOperator get(String uid) {
        return cache.get(uid);
    }

    public void listThenWatch(BiConsumer<Watcher.Action, String> callback) {

        try {
            // list
            crClient
                    .list()
                    .getItems()
                    .forEach(resource -> {
                                cache.put(resource.getMetadata().getUid(), resource);
                                String uid = resource.getMetadata().getUid();
                                executor.execute(() -> callback.accept(Watcher.Action.ADDED, uid));
                            }
                    );

            // watch
            crClient.watch(new Watcher<LoggingOperator>() {
                @Override
                public void eventReceived(Action action, LoggingOperator resource) {
                    log.info("action: " + action + " uid: " + resource.getMetadata().getUid() );
                    try {
                        String uid = resource.getMetadata().getUid();
                        if (cache.containsKey(uid)) {
                            int knownResourceVersion = Integer.parseInt(cache.get(uid).getMetadata().getResourceVersion());
                            int receivedResourceVersion = Integer.parseInt(resource.getMetadata().getResourceVersion());
                            if (knownResourceVersion > receivedResourceVersion) {
                                return;
                            }
                        }
                        log.log(Level.INFO, "received " + action + " for resource " + resource);
                        if (action == Watcher.Action.ADDED || action == Watcher.Action.MODIFIED) {
                            cache.put(uid, resource);
                        } else if (action == Watcher.Action.DELETED) {
                            cache.remove(uid);
                        } else {
                            log.log(Level.SEVERE, "Received unexpected " + action + " event for " + resource);
                            System.exit(-1);
                        }
                        executor.execute(() -> callback.accept(action, uid));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }

                @Override
                public void onClose(KubernetesClientException cause) {
                    log.log(Level.INFO, "onClose", cause);
                    System.exit(-1);
                }

            });

        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            System.exit(-1);
        }
    }

    public void listThenWatchPods(BiConsumer<Watcher.Action, String> callback) {
        try {
            client.pods()
                    .list()
                    .getItems()
                    .stream()
                    .forEach( resource -> {
                        String uid = resource.getMetadata().getUid();
                        podCache.put( uid, resource );
                        executor.execute(() -> callback.accept(Watcher.Action.ADDED, uid));
                    } );

        } catch( Exception e ) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
