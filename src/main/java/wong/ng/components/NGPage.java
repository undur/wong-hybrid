package wong.ng.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;

/**
 * The ng-objects page served at "/ng/". The same click counter as the WO front page, but updated over Ajax: the link is
 * an AjaxUpdateLink and only the AjaxUpdateContainer around the counter is re-rendered.
 */
public class NGPage extends NGComponent {

	public int clickCount;
	public String lastClicked;

	public NGPage( NGContext context ) {
		super( context );
	}

	/**
	 * Component action bound to the link in the template. Returning null re-renders this page instance.
	 */
	public NGActionResults bump() {
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
