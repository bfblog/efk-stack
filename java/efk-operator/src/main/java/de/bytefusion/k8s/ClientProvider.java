package de.bytefusion.k8s;

import de.bytefusion.k8s.customresource.LoggingOperator;
import de.bytefusion.k8s.customresource.LoggingOperatorDoneable;
import de.bytefusion.k8s.customresource.LoggingOperatorList;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.DoneableCustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ApiextensionsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Singleton
public class ClientProvider {

    @Produces
    @Singleton
    @Named("namespace")
    private String findNamespace() throws IOException {
        //return new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace")));
        return "default";
    }

    @Produces
    @Singleton
    KubernetesClient newClient(@Named("namespace") String namespace) {
        return new DefaultKubernetesClient().inNamespace(namespace);
    }

    @Produces
    @Singleton
    NonNamespaceOperation<LoggingOperator, LoggingOperatorList, LoggingOperatorDoneable, Resource<LoggingOperator, LoggingOperatorDoneable>> makeCustomResourceClient(KubernetesClient defaultClient, @Named("namespace") String namespace) {

        KubernetesDeserializer.registerCustomKind("instana.com/v1alpha1", "Example", LoggingOperator.class);

        CustomResourceDefinition crd1 = defaultClient.customResourceDefinitions()
                .createNew()
                .withApiVersion("apiextensions.k8s.io/v1beta1")
                .withKind("CustomResourceDefinition")
                // meta
                .withNewMetadata()
                .withName("logging-operators.bytefusion.de")
                .endMetadata()
                // spec
                .withNewSpec()
                .withGroup("bytefusion.de")
                // names
                .withNewNames()
                .withNewPlural("logging-operators")
                .withNewSingular("logging-operator")
                .withKind("Logging-Operator")
                .withListKind("logging-operator-list")
                .endNames()
                .withScope("Namespaced")
                .endSpec()
                .done();

        defaultClient.customResourceDefinitions().create(crd1);

        CustomResourceDefinition crd = defaultClient
                .customResourceDefinitions()
                .list()
                .getItems()
                .stream()
                .filter(d -> "examples.instana.com".equals(d.getMetadata().getName()))
                .findAny()
                .orElseThrow(
                        () -> new RuntimeException("Deployment error: Custom resource definition examples.instana.com not found."));

        return defaultClient
                .customResources(crd, LoggingOperator.class, LoggingOperatorList.class, LoggingOperatorDoneable.class)
                .inNamespace(namespace);
    }
}
