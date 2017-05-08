package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;

public class SnapshotExtension implements AdminApiExtension {

    @Override
    public String getName() {
        return "snapshot";
    }

    @Override
    public void contributeAdminApiRoutes(Router router) {
        router.add(GET, "/snapshot", SnapshotTask.class);
        router.add(POST, "/snapshot/persist", SnapshotTask.class);
    }
}
