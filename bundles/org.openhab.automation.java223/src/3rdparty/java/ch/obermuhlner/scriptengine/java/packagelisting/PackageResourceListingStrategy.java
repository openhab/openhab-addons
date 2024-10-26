package ch.obermuhlner.scriptengine.java.packagelisting;

import java.util.Collection;

/**
 * The strategy used to list the Java classes in a package
 */
public interface PackageResourceListingStrategy {
	
	Collection<String> listResources(String packageName);
}
