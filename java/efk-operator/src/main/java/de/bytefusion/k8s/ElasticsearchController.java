package de.bytefusion.k8s;

import de.bytefusion.k8s.customresource.ElasticsarchStatus;

public class ElasticsearchController {

    private ElasticsarchStatus status;

    public ElasticsearchController(ElasticsarchStatus status ) {
        this.status = status;
    }

    public void reconcile() {

    }
}
