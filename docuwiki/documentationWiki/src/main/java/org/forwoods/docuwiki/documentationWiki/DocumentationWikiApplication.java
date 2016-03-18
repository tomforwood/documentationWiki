package org.forwoods.docuwiki.documentationWiki;

import org.forwoods.docuwiki.documentationWiki.resources.ClassRepResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DocumentationWikiApplication extends Application<DocumentationWikiConfiguration> {

    public static void main(final String[] args) throws Exception {
        new DocumentationWikiApplication().run(args);
    }

    @Override
    public String getName() {
        return "Documentation Wiki";
    }

    @Override
    public void initialize(final Bootstrap<DocumentationWikiConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final DocumentationWikiConfiguration configuration,
                    final Environment environment) {
        ClassRepResource resource = new ClassRepResource();
        environment.jersey().register(resource);
    }

}
