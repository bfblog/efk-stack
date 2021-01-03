package de.bytefusion.k8s;

import de.bytefusion.k8s.customresource.ElasticsarchStatus;
import de.bytefusion.k8s.customresource.LoggingOperator;

import java.util.logging.Logger;

public class ElasticsearchController {

    private Logger log = Logger.getLogger(ElasticsearchController.class.getName());

    private ElasticsarchStatus status;

    public ElasticsearchController(ElasticsarchStatus status ) {
        this.status = status;
    }

    public void reconcile(LoggingOperator entry) {
        log.info("> reconcile()");
    }
}
