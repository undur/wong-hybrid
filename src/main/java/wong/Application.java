package wong;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import com.webobjects.appserver.WOAdaptorJetty;
import com.webobjects.appserver.WOAdaptorJetty.JettyServerProvider;
import com.webobjects.foundation._NSUtilities;

import er.extensions.appserver.ERXApplication;
import er.extensions.routes.RouteTable;
import ng.adaptor.jetty.NGAdaptorJetty;
import ng.appserver.NGApplication;
import ng.appserver.properties.NGProperties;
import ng.appserver.properties.StandardDeploymentMode;
import wong.components.WOPage;
import wong.ng.NGHybridApplication;

/**
 * The WebObjects side of the hybrid, and the entry point of the whole process.
 *
 * This is a regular ERXApplication with one twist: it also builds the Jetty server that serves it, and puts an ng-objects
 * handler in that server right after WO's own handler. The result is one process, one port, two frameworks.
 *
 * How a request travels:
 *
 *   1. Jetty accepts the request and hands it to the handler sequence built in createJettyServer() below.
 *   2. The WO handler goes first. It converts the request to a WORequest and dispatches it through WO as usual.
 *      If WO has an answer (a registered request handler key such as /wo/ or /wr/, or a mapped route like "/"),
 *      the WO response is sent and we're done.
 *   3. If WO's route table has no route for the URL, its NotFoundRouteHandler marks the 404 response as
 *      "unhandled" (see RouteTable.UNHANDLED_RESPONSE_KEY). The WO handler recognises the mark, discards the
 *      response and returns false to Jetty, which means "I didn't handle this, try the next handler".
 *   4. The ng-objects handler gets the request next and dispatches it through NGHybridApplication's routes.
 *
 * So the rule of thumb is: WO answers everything it knows about, ng-objects answers what is left.
 *
 * To make WO use this server, the application must be started with the Jetty adaptor:
 *
 *   -WOAdaptor WOAdaptorJetty
 *
 * The adaptor notices that the application implements JettyServerProvider and asks it for the server to use instead of
 * building its own.
 */
public class Application extends ERXApplication implements JettyServerProvider {

	public static void main( String[] argv ) {
		ERXApplication.main( argv, Application.class );
	}

	public Application() {
		// WO looks up its session class by the simple name "Session", across every jar on the classpath. Jetty happens to
		// ship an interface with that name (org.eclipse.jetty.server.Session), and depending on how the app is launched
		// WO may find that one first and fail. Registering our class under the name settles it.
		_NSUtilities.setClassForName( Session.class, "Session" );

		// WO's routes. Anything not mapped here (and not a registered WO request handler key) falls through to ng-objects.
		RouteTable.defaultRouteTable().map( "/", WOPage.class );
	}

	/**
	 * Builds the Jetty server that serves both frameworks. Invoked once by WOAdaptorJetty at startup.
	 *
	 * @param port The port WO was told to listen on (-WOPort, default 1200)
	 */
	@Override
	public Server createJettyServer( int port ) {

		// A plain Jetty server with a single HTTP connector on WO's port. Nothing hybrid-specific yet.
		final Server server = new Server();

		final HttpConfiguration config = new HttpConfiguration();
		config.setSendServerVersion( false );

		final ServerConnector connector = new ServerConnector( server, new HttpConnectionFactory( config ) );
		connector.setPort( port );
		server.addConnector( connector );

		// The WO handler. It serves the ERXApplication this method was invoked on, so it needs no reference to it.
		final Handler woHandler = new WOAdaptorJetty.WOJettyHandler();

		// The ng-objects handler. Unlike WO, ng-objects has no global "start me up" step here: we create the
		// NGApplication instance ourselves and hand it to the handler. NGApplication.run() would also start
		// ng-objects' own adaptor and listen on a second port, which is exactly what we don't want.
		final NGApplication ngApplication = createNGApplication();
		final Handler ngHandler = new NGAdaptorJetty.NGJettyHandler( ngApplication );

		// The heart of the hybrid: a handler sequence. Jetty tries the handlers in order and stops at the first one
		// that reports it handled the request. WO goes first and declines requests it has no route for, so
		// ng-objects only ever sees what WO passed on. See the class comment for the full request flow.
		final Handler.Sequence sequence = new Handler.Sequence();
		sequence.addHandler( woHandler );
		sequence.addHandler( ngHandler );
		server.setHandler( sequence );

		return server;
	}

	/**
	 * @return The ng-objects application instance, created but without an adaptor of its own. Requests reach it
	 *         through the NGJettyHandler in the server built above.
	 */
	private static NGApplication createNGApplication() {
		try {
			return NGApplication.create( NGHybridApplication.class, new NGProperties(), StandardDeploymentMode.Development, true );
		}
		catch( Exception e ) {
			throw new RuntimeException( "Failed to create the ng-objects application", e );
		}
	}
}
