package de.bytefusion.k8s;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.reinert.jjschema.v1.JsonSchemaV4Factory;
import de.bytefusion.k8s.customresource.LoggingOperator;
import de.bytefusion.k8s.customresource.LoggingOperatorDoneable;
import de.bytefusion.k8s.customresource.LoggingOperatorList;
import de.bytefusion.k8s.customresource.LoggingOperatorSpec;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

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

        KubernetesDeserializer.registerCustomKind("bytefusion.de/v1", "logging-stack", LoggingOperator.class);

        CustomResourceDefinition crd = defaultClient
                .apiextensions()
                .v1()
                .customResourceDefinitions()
                .load(ClientProvider.class.getResourceAsStream("/logging-operator-crd.yaml"))
                .get();

        CustomResourceDefinitionContext crdContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("apiextensions.k8s.io/v1")
                .withKind("CustomResourceDefinition")
                .withGroup(crd.getSpec().getGroup())
                .withVersion(crd.getSpec().getVersions().get(0).getName())
                .withScope(crd.getSpec().getScope())
                .withName(crd.getMetadata().getName())
                .withPlural(crd.getSpec().getNames().getPlural())
                .withKind(crd.getSpec().getNames().getKind())
                .build();

        try {
            JsonSchemaV4Factory schemaFactory = new JsonSchemaV4Factory();
            //schemaFactory.setAutoPutDollarSchema(true);
            JsonNode productSchema = schemaFactory.createSchema(LoggingOperatorSpec.class);
            System.out.println("--- start ---");
            System.out.println(productSchema);
            System.out.println("--- end ---");
            System.out.println( new YAMLMapper().writeValueAsString(productSchema) );

            System.out.println( Serialization.asJson( new LoggingOperator() ) );
            System.out.println( SerializationUtils.dumpAsYaml(crd) );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        MixedOperation<LoggingOperator, LoggingOperatorList, LoggingOperatorDoneable, Resource<LoggingOperator, LoggingOperatorDoneable>> cronTabClient = defaultClient
                .customResources(crdContext, LoggingOperator.class, LoggingOperatorList.class, LoggingOperatorDoneable.class);

        return defaultClient
                .customResources(crdContext, LoggingOperator.class, LoggingOperatorList.class, LoggingOperatorDoneable.class)
                .inNamespace(namespace);
    }
}
