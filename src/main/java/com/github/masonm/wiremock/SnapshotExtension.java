package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;
import org.apache.commons.lang3.ArrayUtils;

import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;

public class SnapshotExtension implements AdminApiExtension {

    @Override
    public String getName() {
        return "snapshot";
    }

    @Override
    public void contributeAdminApiRoutes(Router router) {
        router.add(POST, "/snapshot", SnapshotTask.class);
        //router.add(POST, "/snapshot/persist", SnapshotTask.class);
    }

    public static void main(String... args) {
        args = ArrayUtils.add(args, "--extensions=com.github.masonm.wiremock.SnapshotExtension");
        new WireMockServerRunner().run(args);
    }
}
