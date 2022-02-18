package de.bytefusion.k8s;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.reinert.jjschema.v1.JsonSchemaV4Factory;
import de.bytefusion.k8s.customresource.LoggingOperatorSpec;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LoggingOperatorTest {

    @Test
    void verifyLoggingOperatorSpecSchema() throws JsonProcessingException {
        JsonSchemaV4Factory schemaFactory = new JsonSchemaV4Factory();
        //schemaFactory.setAutoPutDollarSchema(true);
        JsonNode schema = schemaFactory.createSchema(LoggingOperatorSpec.class);
        System.out.println("schema (json):");
        System.out.println(schema);
        System.out.println("schema (yaml):");
        System.out.println( new YAMLMapper().writeValueAsString(schema) );
    }

}
