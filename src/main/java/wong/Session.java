package wong;

import er.extensions.appserver.ERXSession;

/**
 * The WO session. IDs go in cookies rather than URLs so page URLs stay clean.
 *
 * WO locates this class by its simple name, "Session" (see the registration in Application's constructor). ng-objects
 * uses the same convention but looks in its own application class's package (wong.ng), which is why the ng-objects
 * application lives in a separate package: otherwise ng-objects would find this WO session and fail to cast it.
 */
public class Session extends ERXSession {

	public Session() {
		setStoresIDsInCookies( true );
		setStoresIDsInURLs( false );
	}

	@Override
	public String domainForIDCookies() {
		return "/";
	}
}
