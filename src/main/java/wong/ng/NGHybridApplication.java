package wong.ng;

import ng.appserver.NGApplication;
import ng.plugins.Routes;
import wong.ng.components.NGPage;

/**
 * The ng-objects side of the hybrid.
 *
 * A regular NGApplication, except that it is never "run": wong.Application creates an instance of it and mounts it in
 * the shared Jetty server (see Application.createJettyServer()). It only ever sees requests WO had no route for.
 *
 * It lives in its own package on purpose. ng-objects looks for a session class named "Session" next to its
 * application class, and WO's wong.Session must not be the one it finds.
 */
public class NGHybridApplication extends NGApplication {

	/**
	 * ng-objects' routes. Remember that WO gets first pick, so a URL mapped here is only reachable if WO doesn't claim it too.
	 */
	@Override
	public Routes routes() {
		return super.routes()
				.map( "/ng/", NGPage.class );
	}
}
