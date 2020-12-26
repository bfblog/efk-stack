package de.bytefusion.k8s.customresource;

import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.function.Predicate;

@ApplicationScoped
public class LoggingOperatorInstaller {

    @Inject
    private KubernetesClient client;

    @Inject
    private LoggingOperatorCache cache;

    void onStartup(@Observes StartupEvent _ev) {
        new Thread(this::runWatch).start();
    }

    private void runWatch() {
        cache.listThenWatch(this::handleEvent);
    }

    private void handleEvent(Watcher.Action action, String uid) {
        try {
            LoggingOperator resource = cache.get(uid);
            if (resource == null) {
                return;
            }

            Predicate<StatefulSet> ownerRefMatches = daemonSet -> daemonSet.getMetadata().getOwnerReferences().stream()
                    .anyMatch(ownerReference -> ownerReference.getUid().equals(uid));

            if (client
                    .apps()
                    .statefulSets()
                    .list()
                    .getItems()
                    .stream()
                    .noneMatch(ownerRefMatches)) {

                client
                    .apps()
                    .statefulSets()
                    .create(newStatefulSet(resource));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private StatefulSet newStatefulSet(LoggingOperator resource) {
        StatefulSet statefulSet = client.apps().statefulSets()
                .load(getClass().getResourceAsStream("/statefulset.yaml")).get();
        statefulSet.getMetadata().getOwnerReferences().get(0).setUid(resource.getMetadata().getUid());
        statefulSet.getMetadata().getOwnerReferences().get(0).setName(resource.getMetadata().getName());
        return statefulSet;
    }

}
