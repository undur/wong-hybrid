package wong.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

/**
 * The WO page served at "/". A click counter driven by a classic WO component action.
 */
public class WOPage extends ERXComponent {

	public int clickCount;
	public String lastClicked;

	public WOPage( WOContext context ) {
		super( context );
	}

	/**
	 * Component action bound to the link in the template. Returning null re-renders this page instance.
	 */
	public WOActionResults bump() {
		clickCount++;
		lastClicked = now();
		return null;
	}

	public boolean hasBeenClicked() {
		return clickCount > 0;
	}

	public String clickWord() {
		return clickCount == 1 ? "time" : "times";
	}

	public String renderedAt() {
		return now();
	}

	public String componentName() {
		return getClass().getSimpleName();
	}

	public String requestMethod() {
		return context().request().method();
	}

	public String requestURI() {
		return context().request().uri();
	}

	private static String now() {
		return LocalDateTime.now().format( DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ) );
	}
}
