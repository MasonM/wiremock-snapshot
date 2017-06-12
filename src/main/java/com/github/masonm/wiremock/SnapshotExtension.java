package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;
import org.apache.commons.lang3.ArrayUtils;

import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;

/**
 * Main extension class
 */
public class SnapshotExtension implements AdminApiExtension {

    @Override
    public String getName() {
        return "snapshot";
    }

    @Override
    public void contributeAdminApiRoutes(Router router) {
        router.add(POST, "/recordings/snapshot", SnapshotTask.class);
    }

    // For standalone mode. When WireMock is run in standalone mode, WireMockServerRunner.run() is the entry point,
    // so we just delegate to that, passing this class as an extension.
    public static void main(String... args) {
        args = ArrayUtils.add(args, "--extensions=com.github.masonm.wiremock.SnapshotExtension");
        new WireMockServerRunner().run(args);
    }
}
